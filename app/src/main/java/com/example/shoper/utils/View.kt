package com.example.shoper.utils

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.example.shoper.R
import com.example.shoper.ui.ProductsActivity

/**
 *  Wysuwa element do góry
 */
fun View.slideUp(onComplete: (View) -> Unit = {}) {
    if( visibility == View.VISIBLE ) {
        onComplete(this)
        return
    }

    visibility = View.VISIBLE
    slideView(this, Slide.UP) { view ->
        val constraintSet = ConstraintSet()
        val layout = this.parent as ConstraintLayout

        constraintSet.clone(layout)
        constraintSet.clear(view.id, ConstraintSet.TOP)
        constraintSet.connect(
            view.id, ConstraintSet.BOTTOM,
            ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            view.id, ConstraintSet.START,
            ConstraintSet.PARENT_ID, ConstraintSet.START,
            0
        )
        constraintSet.applyTo(layout)
        onComplete(this)
    }
}

fun View.slideDown(onComplete: (View) -> Unit = {}) {
    if( visibility != View.VISIBLE ) {
        onComplete(this)
        return
    }

    visibility = View.VISIBLE
    slideView(this, Slide.DOWN) { view ->
        val constraintSet = ConstraintSet()
        val layout = this.parent as ConstraintLayout

        constraintSet.clone(layout)
        constraintSet.clear(view.id, ConstraintSet.TOP)
        constraintSet.connect(
            view.id, ConstraintSet.TOP,
            ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            view.id, ConstraintSet.START,
            ConstraintSet.PARENT_ID, ConstraintSet.START,
            0
        )

        constraintSet.applyTo(layout)
        view.visibility = View.GONE
        onComplete(this)
    }
}

enum class Slide {
    UP, DOWN
}

private fun slideView(view: View, direction: Slide, onComplete: (View) -> Unit = {}) {
    val animationLayout = when(direction) {
        Slide.UP -> R.anim.slide_up
        else -> R.anim.slide_down
    }

    AnimationUtils.loadAnimation(view.context, animationLayout).apply {
        setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) { }

            override fun onAnimationEnd(animation: Animation?) {
                onComplete(view)
            }

            override fun onAnimationStart(animation: Animation?) { }
        })
    }.apply {
        view.startAnimation(this)
    }

}