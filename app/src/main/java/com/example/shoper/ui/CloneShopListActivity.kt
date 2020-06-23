package com.example.shoper.ui

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import com.example.shoper.R
import com.example.shoper.databinding.ActivityCloneShopListBinding
import com.example.shoper.entity.Category
import com.example.shoper.entity.Product
import com.example.shoper.model.Category.Product.Weight
import com.example.shoper.model.ShopList
import com.example.shoper.ui.item.CloneProductItem
import com.example.shoper.utils.factor
import com.example.shoper.utils.formatAmount
import com.example.shoper.utils.popup
import com.example.shoper.utils.round
import com.google.gson.Gson
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import kotlin.math.ceil
import kotlin.math.floor

/**
 *  Aktywnośćwykorzystywana do klonowania list zakupowych
 */
class CloneShopListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCloneShopListBinding

    private val selectedProducts: MutableList<SelectProduct> = mutableListOf()

    private val products: List<Product> by lazy {
        val gson = Gson()
        var result : List<Product> = emptyList()

        intent.getStringExtra(INTENT_PRODUCTS)?.let { serializeProducts ->
            result = gson.fromJson(serializeProducts, Array<Product>::class.java).toList()
        }

        result
    }

    private val category: Category by lazy {
        val gson = Gson()
        val serialize = intent.getStringExtra(INTENT_CATEGORY)

        gson.fromJson(serialize, Category::class.java)

    }

    override fun onBackPressed() {
        popup().showMessageYesOrNo(R.string.exit_clone_message, R.string.exit_clone_message_yes, R.string.exit_clone_message_no, onYes = {
            finish()
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState?.let {
            val serializeProducts = it.getString(STATE_SELECTED_PRODUCTS)

            selectedProducts.addAll(
                Gson().fromJson(serializeProducts, Array<SelectProduct>::class.java).toMutableList()
            )
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        title = getString(R.string.title_clone, category.name)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_clone_shop_list)

        val adapter = ItemAdapter<CloneProductItem>()
        adapter.add(products.mapIndexed { productIndex, product ->
            val weightType = Weight.valueOf(product.weightType)
            val selectProduct = selectedProducts.firstOrNull { it.index == productIndex }

            var selectValue = 0
            var isChecked = ObservableBoolean(false)

            selectProduct?.let {
                selectValue = it.selectedProgressValue
                isChecked.set(true)
            }

            CloneProductItem(
                name = product.name,
                amount = selectValue.toDouble(),
                selectValue = selectValue,
                checked = isChecked,
                values = getPossibleValues(product).map { mapValue ->
                    product.formatAmount(mapValue) + " " + getString(weightType.displayShort)
                },
                onAmountChange = { _, selectedValue ->
                    selectedProducts.first { it.index == productIndex }.selectedProgressValue = selectedValue
                },
                onChecked = {
                    binding.warningOfCopy.warningContainer.visibility = View.GONE
                    selectedProducts.add(
                        SelectProduct(
                        productIndex,
                        0
                    ))
                },
                onUnchecked = {
                    val anyoneChecked = adapter.itemList.items.map{ it.checked.get() }.firstOrNull { it } ?: false
                    binding.warningOfCopy.warningContainer.visibility = if( anyoneChecked ) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                    selectedProducts.first {
                        it.index == productIndex
                    }.apply {
                        selectedProducts.remove(this)
                    }
                }
            )
        })

        binding.itemsRecycler.adapter = FastAdapter.with(adapter)
        binding.buttonComplete.setOnClickListener {

            if( selectedProducts.isEmpty() ) {
                popup().showMessageOK(R.string.error_clone_empty_list)
            } else {

                val intent = Intent().apply {
                    val gson = Gson()
                    putExtra(RESULT_PRODUCTS, gson.toJson(selectedProducts.map {
                        val product = products[it.index]
                        val possibleValues = getPossibleValues(product)

                        product.copy(
                            amount = possibleValues.getOrNull(it.selectedProgressValue) ?: product.amount
                        )
                    }))
                    putExtra(RESULT_CATEGORY, gson.toJson(category))
                }


                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

    fun getPossibleValues(product: Product): List<Double> {
        val weightType = Weight.valueOf(product.weightType)
        val factory = product.factor()

        val resultList = when(weightType) {
            Weight.LITR,
            Weight.PIECES -> {
                IntArray(51)
            }
            Weight.GRAMS -> {
                IntArray(101)
            }
            else -> {
                IntArray(101)
            }
        }
        .mapIndexed { index, item ->
            index * factory
        }.toMutableList()

        resultList.set(0, product.amount)

        return resultList
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(STATE_SELECTED_PRODUCTS, Gson().toJson(selectedProducts))
    }

    private class SelectProduct(
        /* Pozycja prawdziwego przedmiotu na liście */
        var index: Int,

        /* Wybrana ilość przedmiotu przez użytkownika */
        var selectedProgressValue: Int
    )

    companion object {
        val STATE_SELECTED_PRODUCTS = "selected_products"

        val INTENT_PRODUCTS = "products"
        val INTENT_CATEGORY = "category"

        val RESULT_PRODUCTS = "result_products"
        val RESULT_CATEGORY = "result_category"

        /* Uruchomienie aktywności do wygenerowania nowej listy produktó */
        fun launchForResult(activity: Activity, code: Int, shopList: ShopList) {
            val gson = Gson()
            val serialieCategory = gson.toJson(shopList.category)
            val serializeProducts = gson.toJson(shopList.products)

            val intent = Intent(activity, CloneShopListActivity::class.java).apply {
                putExtra(INTENT_CATEGORY, serialieCategory)
                putExtra(INTENT_PRODUCTS, serializeProducts)
            }

            activity.startActivityForResult(intent, code)
        }

        /* Zwrócone elementy przez aktywność */
        fun getProducts(intent: Intent): List<Product> {
            val gson = Gson()
            val serializeProducts = intent.getStringExtra(RESULT_PRODUCTS)

            return gson.fromJson(serializeProducts, Array<Product>::class.java).toList()
        }

        /* Zwrócone listy */
        fun getCategory(intent: Intent): Category {
            val gson = Gson()
            val serializeCategory = intent.getStringExtra(RESULT_CATEGORY)

            return gson.fromJson(serializeCategory, Category::class.java)
        }
    }
}
