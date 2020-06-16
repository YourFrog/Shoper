package com.example.shoper.ui

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.view.WindowManager
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.databinding.DataBindingUtil
import com.example.shoper.AppDatabase
import com.example.shoper.R
import com.example.shoper.databinding.ActivityGenerateCategoryQRCodeBinding
import com.example.shoper.entity.Category
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers


class GenerateCategoryQRCodeActivity : BaseActivity() {
    private lateinit var binding: ActivityGenerateCategoryQRCodeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_generate_category_q_r_code)


        val categoryID = intent.getLongExtra(ProductsActivity.INTENT_CATEGORY_ID, 0)

        AppDatabase.getInstance(applicationContext).categoryDao().one(categoryID).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe { shopList ->
            val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = manager.defaultDisplay
            val point = Point()

            display.getSize(point)

            val width = point.x
            val height = point.y
            val smallerDimension = (if (width < height) width else height) * 3 / 4

            val encoder = QRGEncoder(
                Gson().toJson(shopList),
                null,
                QRGContents.Type.TEXT,
                smallerDimension
            )

            title = getString(R.string.qrcode_generate_title, shopList.category.name)
            binding.qrImage.setImageBitmap(encoder.encodeAsBitmap())
        }.addTo(disposable)
    }

    companion object {
        const val INTENT_CATEGORY_ID = "id_category"

        fun launch(context: Context, category: Category) {
            val intent = Intent(context, GenerateCategoryQRCodeActivity::class.java)

            intent.putExtra(INTENT_CATEGORY_ID, category.id)

            context.startActivity(intent)
        }
    }
}