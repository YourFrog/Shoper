package com.example.shoper.utils


import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableInt
import com.example.shoper.R
import com.google.android.material.button.MaterialButton

@BindingAdapter("backgroundTintBinding")
fun backgroundTintBinding(view: TextView?, value: ObservableInt) {

    view?.let { view ->
        view.compoundDrawables.forEach {
            it?.colorFilter = PorterDuffColorFilter(view.context.getColor(value.get()), PorterDuff.Mode.SRC_IN)
        }
    }

}