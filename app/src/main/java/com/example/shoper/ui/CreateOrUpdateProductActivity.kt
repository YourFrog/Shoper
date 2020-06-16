package com.example.shoper.ui

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.shoper.R
import com.example.shoper.databinding.ActivityCreateOrUpdateProductBinding
import com.example.shoper.entity.Product
import com.example.shoper.model.Category
import com.example.shoper.model.PickerValue
import com.example.shoper.utils.popup
import com.google.gson.Gson

/**
 *  Tworzenie bądź edycja produktu
 */
class CreateOrUpdateProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateOrUpdateProductBinding

    /**
     *  Wybrana przez użytkownika jednostka miary produktu
     */
    private var productWeight: Category.Product.Weight? = null

    /* Produkt przekazany do aktywności w celu edycji. W przypadku null'a tworzymy nowy produkt */
    private val product: Product? by lazy {
        intent.getStringExtra(INTENT_PRODUCT)?.let { serialize ->
            Gson().fromJson(serialize, Product::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = getString(R.string.product_title_create)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_or_update_product)
        product?.let {
            title = getString(R.string.product_title_edit)
            productWeight = Category.Product.Weight.valueOf(it.weightType)

            binding.name.setText(it.name)
            binding.amount.setText(it.amount)
            binding.productWeight.setText(getString(productWeight!!.displayName))
        }

        binding.productWeight.setOnClickListener {
            PickerActivity.launchForResult(this, REQUEST_SELECT_PRODUCT_WEIGHT, getString(R.string.picker_select_weight), Category.Product.Weight.values().map {
                PickerValue(
                    getString(it.displayName),
                    it
                )
            })
        }
        binding.buttonCancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
        binding.buttonSave.setOnClickListener {
            val hasError = checkErrors()

            if( !hasError ) {
                val gson = Gson()
                val intentOfResult = Intent(this, CreateOrUpdateProductActivity::class.java)
                val product = product?.copy(
                    name = binding.name.text.toString(),
                    amount = binding.amount.text.toString(),
                    weightType = productWeight!!.toString()
                ) ?: Product(
                    name = binding.name.text.toString(),
                    weightType = productWeight!!.toString(),
                    amount = binding.amount.text.toString(),
                    categoryID = intent.getLongExtra(INTENT_CATEGORY_ID, 0)
                )

                intentOfResult.putExtra(INTENT_RESULT, gson.toJson(product))
                setResult(Activity.RESULT_OK, intentOfResult)
                finish()
            }
        }
    }

    /**
     *  Sprawdzenie czy wprowadzone informacje o produkcie są kompletne
     */
    private fun checkErrors(): Boolean {
        if( productWeight == null ) {
            popup().showMessageOK(R.string.product_error_no_product_weight)
            return true
        }

        if( binding.name.text.toString().isEmpty() ) {
            popup().showMessageOK(R.string.product_error_no_product_name)
            return true
        }

        if( binding.amount.text.toString().isEmpty() ) {
            popup().showMessageOK(R.string.product_error_no_amount)
            return true
        }

        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if( resultCode != Activity.RESULT_OK ) {
            return
        }

        when(requestCode) {
            REQUEST_SELECT_PRODUCT_WEIGHT -> {
                data?.let {
                    val pickerValue = PickerActivity.getSelected(it)
                    productWeight = Category.Product.Weight.valueOf(pickerValue.value as String)

                    binding.productWeight.text = getString(productWeight!!.displayName)
                }
            }
        }
    }

    companion object {
        const val REQUEST_SELECT_PRODUCT_WEIGHT = 10
        const val INTENT_RESULT = "result"
        const val INTENT_PRODUCT = "product"
        const val INTENT_CATEGORY_ID = "id_category"

        /* Uruchomienie aktywności dla nowego produktu */
        fun launchForResult(context: Activity, code: Int, category: com.example.shoper.entity.Category) {
            val intent = Intent(context, CreateOrUpdateProductActivity::class.java)

            intent.putExtra(INTENT_CATEGORY_ID, category.id)

            context.startActivityForResult(intent, code)
        }

        /* Uruchomienie aktywności dla edytowanego produktu */
        fun launchForResult(context: Activity, code: Int, product: Product) {
            val gson = Gson()
            val intent = Intent(context, CreateOrUpdateProductActivity::class.java)

            intent.putExtra(INTENT_PRODUCT, gson.toJson(product))
            intent.putExtra(INTENT_CATEGORY_ID, product.categoryID)

            context.startActivityForResult(intent, code)
        }

        /* Wyciąga produkt z intent'a */
        fun getProduct(intent: Intent): Product {
            val serialize = intent.getStringExtra(INTENT_RESULT)
            return Gson().fromJson(serialize, Product::class.java)
        }
    }
}