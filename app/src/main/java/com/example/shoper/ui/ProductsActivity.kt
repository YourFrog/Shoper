package com.example.shoper.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import com.example.shoper.AppDatabase
import com.example.shoper.R
import com.example.shoper.databinding.ActivityProductsBinding
import com.example.shoper.entity.CatalogProduct
import com.example.shoper.entity.Product
import com.example.shoper.model.Category
import com.example.shoper.model.EAN
import com.example.shoper.model.ProductStatus
import com.example.shoper.model.ShopList
import com.example.shoper.repository.merge
import com.example.shoper.ui.item.CategoryProductsItem
import com.example.shoper.ui.item.ProductItem
import com.example.shoper.utils.factor
import com.example.shoper.utils.popup
import com.example.shoper.utils.slideDown
import com.example.shoper.utils.slideUp
import com.google.android.material.tabs.TabLayoutMediator
import com.google.zxing.integration.android.IntentIntegrator
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.select.SelectExtension
import com.mikepenz.fastadapter.select.getSelectExtension
import com.mikepenz.fastadapter.select.selectExtension
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.bind
import java.util.*
import java.util.concurrent.TimeUnit


interface ProductMapper {
    fun productToItem(product: Product, adapter: ItemAdapter<ProductItem>? = null): ProductItem
}

/**
 *  Lista produktów wchodzących w skład kategorii
 */
class ProductsActivity : BaseActivity(), ProductMapper {

    private lateinit var binding: ActivityProductsBinding

    private lateinit var shopList: ShopList

    private val disposableOfShowingChangeProductStatus by lazy {
        CompositeDisposable().apply {
            addTo(disposable)
        }
    }

    class Adapter(
        private val context: Context,
        private val mapper: ProductMapper,
        val onProductSwipe: (ProductItem, ProductStatus, ItemAdapter<ProductItem>) -> Unit,
        val onProductSelectionChange: (SelectExtension<ProductItem>, ProductItem, Boolean) -> Unit
    ) : FastAdapter<CategoryProductsItem>() {

        val map: EnumMap<ProductStatus, CategoryProductsItem> = EnumMap(ProductStatus::class.java)

        val adapter: ItemAdapter<CategoryProductsItem> by lazy {
            ItemAdapter<CategoryProductsItem>().apply {
                this@Adapter.map.forEach {
                    add(it.value)
                }
            }
        }

        init {
            val values = ProductStatus.values()

            values.sortBy { it.ordinal }
            values.map { status ->
                val productAdapter = ItemAdapter<ProductItem>()

                map[status] = CategoryProductsItem(
                    title = context.getString(when(status) {
                        ProductStatus.BOUGHT -> R.string.product_status_complete
                        ProductStatus.WAITING -> R.string.product_status_waiting
                        ProductStatus.NO_FOUND -> R.string.product_status_not_found
                    }),
                    productsAdapter = productAdapter,
                    onProductSwipe = { item, newStatus ->
                        onProductSwipe(item, newStatus, productAdapter)
                    },
                    onProductSelectionChange = onProductSelectionChange
                )
            }

            addAdapter(0, adapter)
        }

        fun notifyProductChangeStatus(item: ProductItem) {
            removeProduct(item)
            addProduct(item.product)
        }

        fun removeSelectedProducts() {
            var refreshAdapter = false

            map.forEach {
                it.value.productsAdapter.let { productsAdapter ->
                    productsAdapter.fastAdapter?.getSelectExtension()?.deleteAllSelectedItems()

                    if( productsAdapter.itemList.isEmpty ) {
                        refreshAdapter = true
                    }
                }
            }

            if( refreshAdapter ) {
                adapter.fastAdapter?.notifyAdapterDataSetChanged()
            }
        }

        fun removeProduct(item: ProductItem) {
            map.filter { it.value.productsAdapter.itemList.items.contains(item) }.map {
                it.value.productsAdapter.removeByIdentifier(item.identifier)
                it.value.productsAdapter.fastAdapter?.notifyAdapterDataSetChanged()

                if( it.value.productsAdapter.itemList.isEmpty ) {
                    adapter.fastAdapter?.notifyAdapterDataSetChanged()
                }
            }
        }

        fun addProduct(product: Product) {
            val status = ProductStatus.valueOf(product.status)

            map[status]?.productsAdapter?.let { adapter ->
                val item = mapper.productToItem(product, adapter)
                adapter.add(item)
                adapter.fastAdapter?.notifyAdapterDataSetChanged()
            }
        }

        fun unSelect() {
            getSelectExtension().deselect()
        }

        fun selected(): List<ProductItem> {
            val result = ArrayList<ProductItem>()

            adapter.itemList.items.map {
                result.addAll(
                    it.productsAdapter.fastAdapter?.getSelectExtension()?.selectedItems ?: emptyList()
                )
            }

            return result
        }
    }

    private val adapter: Adapter by lazy {
        Adapter(
            context = this,
            mapper = this,
            onProductSwipe = { productItem, newStatus, itemAdapter ->
                itemAdapter.fastAdapter?.getSelectExtension()?.deselect()
                changeProductStatus(productItem, newStatus)

                binding.warningOfFastComment.communicate = getString(R.string.warning_of_fast_change_product_status, productItem.product.name)
                showWarningOrFastCommentContainer()

                this@ProductsActivity.disposableOfShowingChangeProductStatus.clear()
                Single
                    .just(1)
                    .delay(5, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        hideWarningOfFastComment()
                    },
                        ::handleError
                    ).addTo(this@ProductsActivity.disposableOfShowingChangeProductStatus)
            },
            onProductSelectionChange = { selectExtension, _, _ ->
                if( selectExtension.selectedItems.isEmpty() ) {
                    hideWarningOfRemove()
                } else {
                    showWarningOrRemove()
                }
            }
        ).apply {
            shopList.products.forEach { product ->
                addProduct(product)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_products)
        binding.warningOrRemoveContainer.let {
            it.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.slide_up)
            )
        }

        binding.warningOfRemove.acceptText = getString(R.string.yes_remove)
        binding.warningOfRemove.rejectText = getString(R.string.cancel)
        binding.warningOfRemove.communicate = getString(R.string.warning_of_remove_selected_product)

        binding.warningOfFastComment.warningContainer.setOnClickListener {
            popup().showInput(R.string.product_add_fast_comment)
        }
        binding.newProductBox.setOnClickListener {
            CreateOrUpdateProductActivity.launchForResult(this@ProductsActivity, REQUEST_NEW_OR_EDIT_PRODUCT_CODE, shopList.category)
        }

        binding.scanProduct.setOnClickListener {
            IntentIntegrator(this)
                .setBeepEnabled(false)
                .setRequestCode(REQUEST_QR_SCANNER_CODE)
                .setDesiredBarcodeFormats(IntentIntegrator.EAN_13, IntentIntegrator.EAN_8)
                .initiateScan()
        }

        binding.warningOfRemove.accept.setOnClickListener {
            removeSelectedProducts()
            hideWarningOfRemove()
        }

        binding.warningOfRemove.reject.setOnClickListener {
            adapter.unSelect()
            hideWarningOfRemove()
        }

        setupView()
    }

    fun removeSelectedProducts() {
        val products = adapter.selected().map { it.product }

        AppDatabase.getInstance(applicationContext).productDao().remove(products)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
                    adapter.removeSelectedProducts()
                }, ::handleError
            ).addTo(disposable)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if( resultCode != Activity.RESULT_OK ) {
            return
        }

        when(requestCode) {
            REQUEST_NEW_OR_EDIT_PRODUCT_CODE -> {
                data?.let {
                    val product = CreateOrUpdateProductActivity.getProduct(data)

                    AppDatabase.getInstance(applicationContext).productDao().merge(product).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
                        setupView()
                    }, ::handleError)

                    product.ean?.let {
                        // We have ean so add him to database
                        AppDatabase.getInstance(applicationContext).catalogProductDao().oneByEan(it).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                            onSuccess = {
                                AppDatabase.getInstance(applicationContext).catalogProductDao().merge(
                                    it.copy(
                                        name = product.name,
                                        weightType = product.weightType
                                    )).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({}, ::handleError)
                            },
                            onComplete = {
                                AppDatabase.getInstance(applicationContext).catalogProductDao().insert(
                                    CatalogProduct(
                                    name = product.name,
                                    ean = it,
                                    weightType = product.weightType
                                )).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({}, ::handleError)
                            },
                            onError = ::handleError
                        )
                    }
                }
            }
            REQUEST_EDIT_CATEGORY_CODE -> {
                data?.let {
                    val category = CreateOrUpdateCategoryActivity.getCategory(data)

                    AppDatabase.getInstance(applicationContext).categoryDao().merge(category)
                        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            setupView()
                        }, ::handleError)
                }
            }
            MainActivity.REQUEST_QR_SCANNER_CODE -> {
                IntentIntegrator.parseActivityResult(resultCode, data)?.let {

                    if( it.contents != null ) {
                        val eanValue = it.contents

                        AppDatabase.getInstance(applicationContext).catalogProductDao().oneByEan(eanValue).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                            onSuccess = {
                                // Open product activity to add
                                CreateOrUpdateProductActivity.launchForResult(
                                    this@ProductsActivity,
                                    REQUEST_NEW_OR_EDIT_PRODUCT_CODE,
                                    Product(
                                        name = it.name,
                                        weightType = it.weightType,
                                        categoryID = shopList.category.id,
                                        ean = it.ean,
                                        amount = 1.toDouble()
                                    )
                                )
                            },
                            onComplete = {
                                val ean = EAN(eanValue)

                                popup().showMessageYesOrNo(getString(R.string.local_ean_not_found, ean.format()), onYes = {
                                    // Open Next activity for product
                                    CreateOrUpdateProductActivity.launchForResult(
                                        this@ProductsActivity,
                                        REQUEST_NEW_OR_EDIT_PRODUCT_CODE,
                                        shopList.category,
                                        ean)
                                })
                            },
                            onError = ::handleError
                        )
                    }
                }
                val xx = 0

            }
        }
    }

    private fun setupView() {
        val categoryID = intent.getLongExtra(INTENT_CATEGORY_ID, 0)

        AppDatabase.getInstance(applicationContext).categoryDao().one(categoryID).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ shopList ->
                this@ProductsActivity.shopList = shopList

                title = shopList.category.name

                refreshView()
            }, ::handleError
        ).addTo(disposable)
    }

    private fun refreshView() {
        binding.photosViewpager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.photosViewpager) { tab, position ->

        }.attach()

        binding.tabLayout.getTabAt(1)?.select()
    }

    private fun showWarningOrRemove() {
        binding.warningOfFastCommentContainer.slideDown {
            binding.warningOrRemoveContainer.slideUp()
        }
    }

    /**
     *  Ukrycie komunikatu
     */
    private fun hideWarningOfRemove() {
        binding.warningOrRemoveContainer.slideDown()
    }

    private fun showWarningOrFastCommentContainer() {
        binding.warningOrRemoveContainer.slideDown {
            binding.warningOfFastCommentContainer.slideUp()
        }
    }

    private fun hideWarningOfFastComment() {
        binding.warningOfFastCommentContainer.slideDown()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_of_category, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            when(it.itemId) {
                R.id.short_edit_category -> {
                    CreateOrUpdateCategoryActivity.launchForResult(this, REQUEST_EDIT_CATEGORY_CODE, shopList.category)
                }
                R.id.short_add_product -> {
                    CreateOrUpdateProductActivity.launchForResult(this, REQUEST_NEW_OR_EDIT_PRODUCT_CODE, shopList.category)
                }
                R.id.short_generate_qr_code -> {
                    GenerateCategoryQRCodeActivity.launch(this, shopList.category)
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     *  Utworzenie itemka reprezentującego produkt
     */
    override fun productToItem(product: Product, adapter: ItemAdapter<ProductItem>?): ProductItem {
        return ProductItem(
            product = product,
            backgroundColor = ObservableInt(R.color.transparent),
            menuVisisble = ObservableInt(View.VISIBLE),
            buttonVisible = ObservableBoolean(false),
            onBoughtClick = { item ->
                changeProductStatus(item, ProductStatus.BOUGHT)
            },
            onNotFoundClick = { item ->
                changeProductStatus(item, ProductStatus.NO_FOUND)
            },
            onWaitingClick = { item ->
                changeProductStatus(item, ProductStatus.WAITING)
            },
            onEditClick = { item ->
                CreateOrUpdateProductActivity.launchForResult(this, REQUEST_NEW_OR_EDIT_PRODUCT_CODE, product)
            },
            onButtonToggle = { item, showing ->
                adapter?.fastAdapter?.getSelectExtension()?.deselect()
            },
            onPlusClick = { item ->
                item.product.amount += item.product.factor()
                AppDatabase.getInstance(applicationContext).productDao().update(item.product).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
                    // Nothing
                    adapter?.fastAdapter?.notifyAdapterDataSetChanged()
                }, ::handleError).addTo(disposable)
            },
            onMinusClick = { item ->
                item.product.amount -= item.product.factor()
                AppDatabase.getInstance(applicationContext).productDao().update(item.product).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
                    // Nothing
                    adapter?.fastAdapter?.notifyAdapterDataSetChanged()
                }, ::handleError).addTo(disposable)
            },
            onRemoveClick = ::questionOfRemoveProduct
        )
    }

    /**
     *  Zadaje pytanie czy usunąć produkt
     */
    private fun questionOfRemoveProduct(item: ProductItem) {
        val product = item.product

        popup().showMessageYesOrNo(getString(R.string.confirm_remove_product, product.name), onYes = {
            removeProduct(item)
        })
    }

    /**
     *  Usuwa produkt
     */
    private fun removeProduct(item: ProductItem) {
        val product = item.product

        AppDatabase.getInstance(applicationContext).productDao().remove(product)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(
                {
                    adapter.removeProduct(item)
                }, ::handleError
            ).addTo(disposable)
    }

    /**
     *  Zmienia status produktowi oraz przesuwa go pomiędzy adapterami aby trafił na prawidłową liste
     */
    private fun changeProductStatus(item: ProductItem, newStatus: ProductStatus) {
        val product = item.product

        product.status = newStatus.toString()

        AppDatabase.getInstance(applicationContext).productDao().update(product).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
                adapter.notifyProductChangeStatus(item)
            }, ::handleError
        ).addTo(disposable)
    }

    companion object {
        const val REQUEST_NEW_OR_EDIT_PRODUCT_CODE = 10
        const val REQUEST_EDIT_CATEGORY_CODE = 20
        const val REQUEST_QR_SCANNER_CODE = 30
        const val INTENT_CATEGORY_ID = "id_category"

        fun launch(context: Context, shopList: ShopList) {
            val intent = Intent(context, ProductsActivity::class.java)

            intent.putExtra(INTENT_CATEGORY_ID, shopList.category.id)

            context.startActivity(intent)
        }
    }
}