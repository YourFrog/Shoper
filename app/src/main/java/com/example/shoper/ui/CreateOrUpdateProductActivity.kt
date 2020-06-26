package com.example.shoper.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.shoper.R
import com.example.shoper.databinding.ActivityCreateOrUpdateProductBinding
import com.example.shoper.entity.Product
import com.example.shoper.model.Category
import com.example.shoper.model.EAN
import com.example.shoper.model.PickerValue
import com.example.shoper.utils.factor
import com.example.shoper.utils.format
import com.example.shoper.utils.popup
import com.example.shoper.utils.round
import com.google.gson.Gson


/**
 *  Tworzenie bądź edycja produktu
 */
class CreateOrUpdateProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateOrUpdateProductBinding

    /**
     *  Wybrana przez użytkownika jednostka miary produktu
     */
    private lateinit var productWeight: Category.Product.Weight

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

        setDefaultValue()

        product?.let {
            title = getString(R.string.product_title_edit)
            productWeight = Category.Product.Weight.valueOf(it.weightType)

            binding.name.setText(it.name)
            binding.amount.setText(it.amount.toString())
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

        binding.minus.setOnClickListener {
            val currAmount = binding.amount.text.toString().toDouble()

            if( currAmount > 0 ) {
                val newAmount = currAmount - productWeight.factor()
                binding.amount.setText(productWeight.format(newAmount))
            }
        }

        binding.plus.setOnClickListener {
            val currAmount = binding.amount.text.toString().toDouble()
            val newAmount = currAmount + productWeight.factor()

            binding.amount.setText(productWeight.format(newAmount))
        }

        binding.buttonSave.setOnClickListener {
            val hasError = checkErrors()

            if( !hasError ) {
                val gson = Gson()
                val intentOfResult = Intent(this, CreateOrUpdateProductActivity::class.java)
                val product = product?.copy(
                    name = binding.name.text.toString(),
                    amount = binding.amount.text.toString().toDouble(),
                    weightType = productWeight!!.toString()
                ) ?: Product(
                    name = binding.name.text.toString(),
                    weightType = productWeight!!.toString(),
                    amount = binding.amount.text.toString().toDouble(),
                    ean = intent.getStringExtra(INTENT_EAN),
                    categoryID = intent.getLongExtra(INTENT_CATEGORY_ID, 0)
                )

                intentOfResult.putExtra(INTENT_RESULT, gson.toJson(product))
                setResult(Activity.RESULT_OK, intentOfResult)
                finish()
            }
        }

        binding.name.requestFocus()
        val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.name, InputMethodManager.SHOW_IMPLICIT)
    }

    /**
     *  Ustawienie domyślnej wartości na polach
     */
    private fun setDefaultValue() {
        binding.amount.setText("1")
        Category.Product.Weight.values().first {
            it.default
        }.let {
            productWeight = it
            binding.productWeight.setText(it.displayName)
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
        const val INTENT_EAN = "ean"

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

        /* Uruchomienie aktywności dla zeskanowanego kodu EAN */
        fun launchForResult(context: Activity, code: Int, category: com.example.shoper.entity.Category, ean: EAN) {
            val gson = Gson()
            val intent = Intent(context, CreateOrUpdateProductActivity::class.java)

            intent.putExtra(INTENT_EAN, ean.value)
            intent.putExtra(INTENT_CATEGORY_ID, category.id)

            context.startActivityForResult(intent, code)
        }

        /* Wyciąga produkt z intent'a */
        fun getProduct(intent: Intent): Product {
            val serialize = intent.getStringExtra(INTENT_RESULT)
            return Gson().fromJson(serialize, Product::class.java)
        }
    }
}