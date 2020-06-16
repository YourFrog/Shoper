package com.example.shoper.ui

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.shoper.R
import com.example.shoper.databinding.ActivityCreateOrUpdateCategoryBinding
import com.example.shoper.entity.Category
import com.example.shoper.entity.Product
import com.google.gson.Gson

/**
 *  Aktywność obsługująca dodawanie i edycje kategorii
 */
class CreateOrUpdateCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateOrUpdateCategoryBinding

    /* Edytowana kategoria */
    private val category: Category by lazy {
        intent.getStringExtra(INTENT_CATEGORY).let { serialize ->
            Gson().fromJson(serialize, Category::class.java)
        } ?: Category(
            name = "",
            description = ""
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = getString(if( isEdit() ) {
            R.string.category_title_edit
        } else {
            R.string.category_title_create
        })

        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_or_update_category)

        binding.name.setText(category.name)
        binding.description.setText(category.description)

        binding.buttonCancel.setOnClickListener {
            finish()
        }
        binding.buttonSave.setOnClickListener {
            val serializeCategory = Gson().toJson(category.copy(
                name = binding.name.text.toString() ?: "undefined",
                description = binding.description.text.toString()
            ))

            val intent = Intent().apply {
                putExtra(INTENT_RESULT, serializeCategory)
            }

            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun isEdit() = intent.hasExtra(INTENT_CATEGORY)

    companion object {
        const val INTENT_RESULT = "result"
        const val INTENT_CATEGORY = "category"

        fun launchForResult(context: Activity, code: Int, category: Category? = null) {
            val intent = Intent(context, CreateOrUpdateCategoryActivity::class.java)

            category?.let {
                val gson = Gson()
                intent.putExtra(INTENT_CATEGORY, gson.toJson(it))
            }

            context.startActivityForResult(intent, code)
        }

        /* Wyciąga produkt z intenta */
        fun getCategory(intent: Intent): Category {
            val serialize = intent.getStringExtra(INTENT_RESULT)
            return Gson().fromJson(serialize, Category::class.java)
        }
    }
}