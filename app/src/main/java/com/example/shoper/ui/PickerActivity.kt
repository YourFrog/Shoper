package com.example.shoper.ui

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.shoper.R
import com.example.shoper.databinding.ActivityPickerBinding
import com.example.shoper.databinding.ActivityProductsBinding
import com.example.shoper.model.PickerValue
import com.example.shoper.ui.item.PickerItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import java.lang.reflect.Type

/**
 *  Aktywność wykorzystywana do wybierania elementów z listy
 */
class PickerActivity : AppCompatActivity() {

    /**
     *  Elementy do wyboru
     */
    private val items: List<PickerValue> by lazy {
        Gson().fromJson(intent.getStringExtra(INTENT_ITEMS), Array<PickerValue>::class.java).toMutableList()
    }

    private var binding: ActivityPickerBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_picker)

        val adapter = ItemAdapter<PickerItem>()

        adapter.add(items.map {
            PickerItem(
                item = it,
                onClick = { item ->
                    val gson = Gson()
                    val intent = Intent()

                    intent.putExtra(INTENT_RESULT, gson.toJson(item))

                    this@PickerActivity.setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            )
        })

        binding?.itemsRecycler?.adapter = FastAdapter.with(adapter)

        title = intent.getStringExtra(INTENT_TITLE)
    }


    companion object {
        val INTENT_ITEMS = "items"
        val INTENT_TITLE = "title"
        val INTENT_RESULT = "result"

        fun launchForResult(context: Activity, code: Int, title: String, items: List<PickerValue>) {
            val gson = Gson()
            val intent = Intent(context, PickerActivity::class.java)

            intent.putExtra(INTENT_TITLE, title)
            intent.putExtra(INTENT_ITEMS, gson.toJson(items))

            context.startActivityForResult(intent, code)
        }

        /**
         *  Pobiera wybraną wartość z picker'a
         */
        fun getSelected(intent: Intent): PickerValue {
            val serialize = intent.getStringExtra(INTENT_RESULT)
            return Gson().fromJson(serialize, PickerValue::class.java)
        }
    }
}