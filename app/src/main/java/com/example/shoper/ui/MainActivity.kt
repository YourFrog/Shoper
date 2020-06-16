package com.example.shoper.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.databinding.DataBindingUtil
import com.example.shoper.AppDatabase
import com.example.shoper.R
import com.example.shoper.databinding.ActivityMainBinding
import com.example.shoper.entity.Category
import com.example.shoper.entity.Product
import com.example.shoper.model.ShopList
import com.example.shoper.repository.merge
import com.example.shoper.ui.item.CategoryItem
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Action
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    /* Adapter na kategorie */
    private var adapter: ItemAdapter<CategoryItem> = ItemAdapter()

    fun handleRemoveCategory(item: CategoryItem) {
        AppDatabase.getInstance(applicationContext).categoryDao().remove(item.shopList.category).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
                adapter.removeByIdentifier(item.identifier)
            },
            ::handleError
        ).addTo(disposable)
    }

    fun handleAddProduct(item: CategoryItem) {
        CreateOrUpdateProductActivity.launchForResult(this, REQUEST_NEW_PRODUCT_CODE, item.shopList.category)
    }

    fun handleEditCategory(item: CategoryItem) {
        CreateOrUpdateCategoryActivity.launchForResult(this, REQUEST_NEW_OR_EDIT_CATEGORY_CODE, item.shopList.category)
    }

    fun handleClickOnCategory(item: CategoryItem) {
        ProductsActivity.launch(this, item.shopList)
    }

    fun handleCloneCategory(item: CategoryItem) {
        cloneShopList(item.shopList.category, item.shopList.products)
    }

    fun cloneShopList(oldCategory: Category, oldProducts: List<Product>) {
        val newCategory = Category(
            name = getString(R.string.clone_category_name, oldCategory.name),
            description = oldCategory.description
        )

        val db = AppDatabase.getInstance(applicationContext)

        db.categoryDao().insert(newCategory).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ categoryID ->

            val newProducts = oldProducts.map {
                Product(
                    name = it.name,
                    amount = it.amount,
                    status = it.status,
                    weightType = it.weightType,
                    categoryID = categoryID
                )
            }.toTypedArray()

            Completable.fromAction {
                db.productDao().insertAll(*newProducts)
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
                setupView()
            }, ::handleError).addTo(disposable)
        }, ::handleError).addTo(disposable)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = getString(R.string.categories_title)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        setupView()
    }

    fun setupView() {
        AppDatabase.getInstance(applicationContext).categoryDao().all().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(
            { shopLists ->

                if( shopLists.isEmpty() ) {
                    // Brak zdefiniowanych kategorii
                    binding.notDefineAnyCategory.visibility = View.VISIBLE
                    binding.categories.visibility = View.GONE
                } else {
                    // Użytkownik posiada choć jedną kategorie
                    binding.notDefineAnyCategory.visibility = View.GONE
                    binding.categories.visibility = View.VISIBLE

                    adapter.clear()
                    adapter.add(shopLists.map { shopList ->
                        CategoryItem(
                            shopList = shopList,
                            onClick = ::handleClickOnCategory,
                            onRemoveClick = ::handleRemoveCategory,
                            onAddProductClick = ::handleAddProduct,
                            onEditClick = ::handleEditCategory,
                            onCloneClick = ::handleCloneCategory
                        )
                    })

                    binding.categories.adapter = FastAdapter.with(adapter)
                }
            },
            ::handleError
        ).addTo(disposable)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        if( resultCode != Activity.RESULT_OK ) {
            return
        }

        when(requestCode) {
            REQUEST_NEW_OR_EDIT_CATEGORY_CODE -> {
                data?.let {
                    val category= CreateOrUpdateCategoryActivity.getCategory(data)

                    AppDatabase.getInstance(applicationContext).categoryDao().merge(category).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
                        setupView()
                    }, ::handleError)
                }
            }
            REQUEST_NEW_PRODUCT_CODE -> {
                data?.let {
                    val product = CreateOrUpdateProductActivity.getProduct(it)

                    AppDatabase.getInstance(applicationContext).productDao().merge(product)
                        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            setupView()
                        }, ::handleError)
                }
            }
            REQUEST_QR_SCANNER_CODE -> {
                IntentIntegrator.parseActivityResult(resultCode, data)?.let {

                    if( it.contents != null ) {
                        val scanShopList = Gson().fromJson(it.contents, ShopList::class.java)
                        cloneShopList(scanShopList.category, scanShopList.products)
                    }
                }
                val xx = 0

            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        item?.let {

            when(it.itemId) {
                R.id.short_new_list,
                R.id.new_list -> {
                    CreateOrUpdateCategoryActivity.launchForResult(this, REQUEST_NEW_OR_EDIT_CATEGORY_CODE)
                }
                R.id.short_settings,
                R.id.settings -> {
                    SettingsActivity.launch(this)
                }
                R.id.scanner_qr -> {
                    IntentIntegrator(this)
                        .setBeepEnabled(false)
                        .setRequestCode(REQUEST_QR_SCANNER_CODE)
                        .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                        .initiateScan()
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val REQUEST_NEW_OR_EDIT_CATEGORY_CODE = 10
        const val REQUEST_NEW_PRODUCT_CODE = 20
        const val REQUEST_QR_SCANNER_CODE = 30

        fun launch(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }
}
