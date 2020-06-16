package com.example.shoper.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import com.example.shoper.AppDatabase
import com.example.shoper.R
import com.example.shoper.databinding.ActivityProductsBinding
import com.example.shoper.entity.Product
import com.example.shoper.model.Category
import com.example.shoper.model.ProductStatus
import com.example.shoper.model.ShopList
import com.example.shoper.repository.merge
import com.example.shoper.ui.item.ProductItem
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers

/**
 *  Lista produktów wchodzących w skład kategorii
 */
class ProductsActivity : BaseActivity() {

    private lateinit var binding: ActivityProductsBinding

    private lateinit var shopList: ShopList

    private lateinit var productBoughtAdapter: ItemAdapter<ProductItem>

    private lateinit var productWaitingAdapter: ItemAdapter<ProductItem>

    private lateinit var productNoFoundAdapter: ItemAdapter<ProductItem>

    private lateinit var productPartAdapter: ItemAdapter<ProductItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_products)
        binding.newProductBox.setOnClickListener {
            CreateOrUpdateProductActivity.launchForResult(this@ProductsActivity, REQUEST_NEW_OR_EDIT_PRODUCT_CODE, shopList.category)
        }

        setupView()
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
        }
    }

    private fun setupView() {
        val categoryID = intent.getLongExtra(INTENT_CATEGORY_ID, 0)

        AppDatabase.getInstance(applicationContext).categoryDao().one(categoryID).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ shopList ->
                this@ProductsActivity.shopList = shopList

                title = shopList.category.name

                productBoughtAdapter = prepareAdapter(Category.Product.Status.BOUGHT)
                productWaitingAdapter = prepareAdapter(Category.Product.Status.WAITING)
                productNoFoundAdapter = prepareAdapter(Category.Product.Status.NO_FOUND)
                productPartAdapter = prepareAdapter(Category.Product.Status.PART)

                refreshView()
            }, ::handleError
        ).addTo(disposable)
    }

    private fun refreshView() {
        binding.productsWaiting.adapter = FastAdapter.with(productWaitingAdapter)
        binding.productsComplete.adapter = FastAdapter.with(productBoughtAdapter)
        binding.productsNotFound.adapter = FastAdapter.with(productNoFoundAdapter)
        binding.productsPart.adapter = FastAdapter.with(productPartAdapter)
    }

    private fun prepareAdapter(status: Category.Product.Status): ItemAdapter<ProductItem> {
        val adapter = ItemAdapter<ProductItem>()

        shopList.products.filter { it.status == status.toString() }.sortedBy { it.name }.forEach { product ->
            val item = createProductItem(product)

            adapter.add(item)
        }

        return adapter
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
    private fun createProductItem(product: Product): ProductItem {
        return ProductItem(
            product = product,
            onClick = {
                val i = 0
            },
            onBoughtClick = { item ->
                changeProductStatus(item, ProductStatus.BOUGHT)
            },
            onNotFoundClick = { item ->
                changeProductStatus(item, ProductStatus.NO_FOUND)
            },
            onPartClick = { item ->
                changeProductStatus(item, ProductStatus.PART)
            },
            onWaitingClick = { item ->
                changeProductStatus(item, ProductStatus.WAITING)
            },
            onEditClick = { item ->
                CreateOrUpdateProductActivity.launchForResult(this, REQUEST_NEW_OR_EDIT_PRODUCT_CODE, product)
            },
            onRemoveClick = { item ->
                AppDatabase.getInstance(applicationContext).productDao().remove(product).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
                    currentAdapterForProduct(item.product).apply {
                        removeByIdentifier(item.identifier)
                    }
                }, ::handleError
                ).addTo(disposable)
            }
        )
    }

    /**
     *  Zmienia status produktowi oraz przesuwa go pomiędzy adapterami aby trafił na prawidłową liste
     */
    fun changeProductStatus(item: ProductItem, newStatus: ProductStatus) {
        val product = item.product

        currentAdapterForProduct(product).apply {
            removeByIdentifier(item.identifier)

        }

        product.status = newStatus.toString()

        AppDatabase.getInstance(applicationContext).productDao().update(product).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
                currentAdapterForProduct(product).apply {
                    add(createProductItem(product))
                }
            }, ::handleError
        ).addTo(disposable)
    }

    /**
     *  Zwraca adapter pasujący do aktualnego statusu produktu
     */
    private fun currentAdapterForProduct(product: Product): ItemAdapter<ProductItem> {
        val currentStatus = ProductStatus.valueOf(product.status)

        return when(currentStatus) {
            ProductStatus.WAITING -> productWaitingAdapter
            ProductStatus.BOUGHT -> productBoughtAdapter
            ProductStatus.NO_FOUND -> productNoFoundAdapter
            ProductStatus.PART -> productPartAdapter
        }
    }

    companion object {
        const val REQUEST_NEW_OR_EDIT_PRODUCT_CODE = 10
        const val REQUEST_EDIT_CATEGORY_CODE = 20
        const val INTENT_CATEGORY_ID = "id_category"

        fun launch(context: Context, shopList: ShopList) {
            val intent = Intent(context, ProductsActivity::class.java)

            intent.putExtra(INTENT_CATEGORY_ID, shopList.category.id)

            context.startActivity(intent)
        }
    }
}