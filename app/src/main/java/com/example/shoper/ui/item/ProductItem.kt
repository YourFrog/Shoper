package com.example.shoper.ui.item

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.view.View
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import com.example.shoper.R
import com.example.shoper.databinding.ItemProductBinding
import com.example.shoper.entity.Product
import com.example.shoper.model.ProductStatus
import com.example.shoper.model.ProductWeightType
import com.example.shoper.utils.formatAmount
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.swipe.ISwipeable


/**
 *  Item reprezentujący wyświetlenie kategorii na liście
 */
class ProductItem (
    val product: Product,
    val onButtonToggle: (ProductItem, Boolean) -> Unit,
    val onPlusClick: (ProductItem) -> Unit,
    val onMinusClick: (ProductItem) -> Unit,
    val onBoughtClick: (ProductItem) -> Unit,
    val onWaitingClick: (ProductItem) -> Unit,
    val onNotFoundClick: (ProductItem) -> Unit,
    val onEditClick: (ProductItem) -> Unit,
    val onRemoveClick: (ProductItem) -> Unit,
    val backgroundColor: ObservableInt,
    val menuVisisble: ObservableInt,
    val buttonVisible: ObservableBoolean
) : AbstractItem<ProductItem.ViewHolder>(), ISwipeable {

    /** defines the type defining this item. must be unique. preferably an id */
    override val type: Int
        get() = R.layout.item_product.toInt()

    /** defines the layout which will be used for this item in the list */
    override val layoutRes: Int
        get() = R.layout.item_product

    override val isSwipeable: Boolean
        get() = true

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    class ViewHolder(view: View) : FastAdapter.ViewHolder<ProductItem>(view) {

        val binding by lazy {
            ItemProductBinding.bind(itemView)
        }

        lateinit var productItem: ProductItem

        override fun bindView(item: ProductItem, payloads: List<Any>) {

            val currentWeightType = ProductWeightType.valueOf(item.product.weightType)
            val currentStatus = ProductStatus.valueOf(item.product.status)

            this.productItem = item

            item.backgroundColor.addOnPropertyChangedCallback(object:Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    val colorFrom: Int = itemView.context.getColor(R.color.transparent)
                    val colorTo: Int = itemView.context.getColor(item.backgroundColor.get())
                    val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
                    colorAnimation.duration = 500 // milliseconds

                    colorAnimation.addUpdateListener { animator -> binding.productContainer.setBackgroundColor(animator.animatedValue as Int) }
                    colorAnimation.start()
                }
            })
            item.menuVisisble.addOnPropertyChangedCallback(object:Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    binding.menu.visibility = item.menuVisisble.get()
                }
            })
            item.buttonVisible.addOnPropertyChangedCallback(object:Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    binding.buttonContainer.visibility = if( item.buttonVisible.get() ) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }
            })
            binding.amount.setOnClickListener {
                binding.buttonContainer.let {
                    val showing = !item.buttonVisible.get()


                    item.onButtonToggle(item, showing)
                    item.buttonVisible.set(showing)
                }
            }
            binding.plus.setOnClickListener { item.onPlusClick(item) }
            binding.minus.setOnClickListener { item.onMinusClick(item) }

            binding.name.text = item.product.name
            binding.amount.text = item.product.formatAmount()
            binding.productWeight.text = itemView.context.getString(when(currentWeightType) {
                ProductWeightType.KILOGRAMS -> R.string.weight_kilo
                ProductWeightType.GRAMS -> R.string.weight_grams
                ProductWeightType.LITR -> R.string.weight_litr
                else -> R.string.weight_piece
            })
            binding.menu.setOnClickListener {
                val popup = PopupMenu(itemView.context, it)

                popup.menuInflater.inflate(R.menu.menu_of_product_item, popup.menu)
                popup.setOnMenuItemClickListener {
                    when(it.itemId) {
                        R.id.remove -> item.onRemoveClick(item)
                        R.id.bought -> item.onBoughtClick(item)
                        R.id.waiting -> item.onWaitingClick(item)
                        R.id.no_found -> item.onNotFoundClick(item)
                        R.id.edit -> item.onEditClick(item)
                    }
                    false
                }

                when(currentStatus) {
                    ProductStatus.BOUGHT -> {
                        popup.menu.removeItem(R.id.bought)
                    }
                    ProductStatus.NO_FOUND -> {
                        popup.menu.removeItem(R.id.no_found)
                    }
                    ProductStatus.WAITING -> {
                        popup.menu.removeItem(R.id.waiting)
                    }
                }

                popup.show()
            }
        }

        override fun unbindView(item: ProductItem) { }
    }
}