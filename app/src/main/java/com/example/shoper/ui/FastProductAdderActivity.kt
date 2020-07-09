package com.example.shoper.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import com.example.shoper.AppDatabase
import com.example.shoper.R
import com.example.shoper.databinding.ActivityCreateOrUpdateProductBinding
import com.example.shoper.databinding.ActivityFastProductAdderBinding
import com.example.shoper.entity.CatalogProduct
import com.example.shoper.entity.Product
import com.example.shoper.model.ProductStatus
import com.example.shoper.model.ShopList
import com.example.shoper.repository.merge
import com.example.shoper.ui.item.ProductItem
import com.example.shoper.ui.item.SearchProductItem
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.DiffCallback
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class FastProductAdderActivity : BaseActivity() {

    private lateinit var binding: ActivityFastProductAdderBinding
    private lateinit var shopList: ShopList
    private val adapter = ItemAdapter<SearchProductItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_fast_product_adder)
        binding.itemsRecycler.adapter = FastAdapter.with(adapter)

        setupView()
    }

    private fun setupView() {

        refreshCategory {
            title = shopList.category.name

            binding.search.onActionViewExpanded()
            binding.search.setOnQueryTextListener(object:android.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let {
                        this@FastProductAdderActivity.searchProducts(it)
                    }
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let {
                        this@FastProductAdderActivity.searchProducts(it)
                    }
                    return true
                }
            })
        }
    }

    private fun refreshCategory(onComplete: () -> Unit) {
        val categoryID = intent.getLongExtra(ProductsActivity.INTENT_CATEGORY_ID, 0)

        AppDatabase.getInstance(applicationContext).categoryDao().one(categoryID).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ shopList ->
                this@FastProductAdderActivity.shopList = shopList

                onComplete()
            }, ::handleError
        ).addTo(disposable)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun searchProducts(phrase: String) {
        AppDatabase.getInstance(applicationContext).catalogProductDao()
            .searchByName(phrase)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { products ->
                    when {
                        products.isEmpty() -> {
                            binding.noResult.visibility = View.VISIBLE
                            binding.itemsRecycler.visibility = View.GONE
                        }
                        else -> {
                            binding.noResult.visibility = View.GONE
                            binding.itemsRecycler.visibility = View.VISIBLE

                            refreshQueryResult(products)
                        }
                    }
                },
                onError = this@FastProductAdderActivity::handleError
            ).addTo(disposable)
    }

    private fun refreshQueryResult(products: List<CatalogProduct>) {

        val items = products.map { catalogProduct ->
            shopList.products.firstOrNull {
                it.ean == catalogProduct.ean
            } ?: Product(
                name = catalogProduct.name,
                amount = 0.0,
                weightType = catalogProduct.weightType,
                status = ProductStatus.WAITING.toString(),
                ean = catalogProduct.ean
            )
        }.map { item ->
            SearchProductItem(
                product = item,
                onProductAdded = {
                    val product = it.product.apply {
                        categoryID = shopList.category.id
                    }

                    AppDatabase.getInstance(applicationContext).productDao().merge(product).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                        onSuccess = {
                            refreshCategory {
                                refreshQueryResult(products)
                            }
                        },
                        onError =this@FastProductAdderActivity::handleError
                    )

                },
                onProductRemove = {
                    val product = it.product

                    if( product.amount == 0.0 ) {
                        // Usuwamy
                        AppDatabase.getInstance(applicationContext).productDao().remove(product).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                            onSuccess = {
                                refreshCategory {
                                    refreshQueryResult(products)
                                }
                            },
                            onError =this@FastProductAdderActivity::handleError
                        )
                    } else {
                        // Aktualizujemy
                        AppDatabase.getInstance(applicationContext).productDao().merge(product).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                            onSuccess = {
                                refreshCategory {
                                    refreshQueryResult(products)
                                }
                            },
                            onError =this@FastProductAdderActivity::handleError
                        )
                    }
                }
            )
        }

        val diff = FastAdapterDiffUtil.calculateDiff(adapter, items, object:DiffCallback<SearchProductItem> {
            override fun areContentsTheSame(
                oldItem: SearchProductItem,
                newItem: SearchProductItem
            ): Boolean {
                return oldItem.product.ean == newItem.product.ean && oldItem.product.amount == newItem.product.amount
            }

            override fun areItemsTheSame(
                oldItem: SearchProductItem,
                newItem: SearchProductItem
            ): Boolean {
                return true
            }

            override fun getChangePayload(
                oldItem: SearchProductItem,
                oldItemPosition: Int,
                newItem: SearchProductItem,
                newItemPosition: Int
            ): Any? {
                return null
            }
        })
        FastAdapterDiffUtil.set(adapter, diff)
    }

    companion object {
        const val INTENT_CATEGORY_ID = "id_category"

        /**
         *  Uruchomienie aktywności
         */
        fun launch(context: Context, shopList: ShopList) {
            val intent = Intent(context, FastProductAdderActivity::class.java)

            intent.putExtra(INTENT_CATEGORY_ID, shopList.category.id)

            context.startActivity(intent)
        }
    }
}