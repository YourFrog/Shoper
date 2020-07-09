package com.example.shoper.ui.item

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.View
import androidx.databinding.ObservableInt
import com.example.shoper.R
import com.example.shoper.databinding.ItemProductBinding
import com.example.shoper.databinding.ItemSearchProductBinding
import com.example.shoper.entity.Product
import com.example.shoper.model.ProductWeightType
import com.example.shoper.utils.factor
import com.example.shoper.utils.formatAmount
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem


/**
 *  Item reprezentujący wyświetlenie kategorii na liście
 */
class SearchProductItem (
    val product: Product,
    val onProductRemove: (SearchProductItem) -> Unit,
    val onProductAdded: (SearchProductItem) -> Unit
) : AbstractItem<SearchProductItem.ViewHolder>() {

    /** defines the type defining this item. must be unique. preferably an id */
    override val type: Int
        get() = R.layout.item_search_product.toInt()

    /** defines the layout which will be used for this item in the list */
    override val layoutRes: Int
        get() = R.layout.item_search_product

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    class ViewHolder(view: View) : FastAdapter.ViewHolder<SearchProductItem>(view) {

        val binding by lazy {
            ItemSearchProductBinding.bind(itemView)
        }

        override fun bindView(item: SearchProductItem, payloads: List<Any>) {
            binding.plusColor = ObservableInt(R.color.gray)

            binding.name.text = item.product.name
            binding.amount.text = item.product.formatAmount()
            binding.productWeight.text = itemView.context.getString(when(ProductWeightType.valueOf(item.product.weightType)) {
                ProductWeightType.KILOGRAMS -> R.string.weight_kilo
                ProductWeightType.GRAMS -> R.string.weight_grams
                ProductWeightType.LITR -> R.string.weight_litr
                else -> R.string.weight_piece
            })
            binding.plusContainer.setOnClickListener {
                addProduct(item)

                item.onProductAdded(item)
            }

            binding.minus.setOnClickListener {
                removeProduct(item)

                item.onProductRemove(item)
            }

            setAmountVisible(item)
        }

        private fun addProduct(item: SearchProductItem) {
            item.product.amount += item.product.factor()
            setAmountVisible(item)
        }

        private fun removeProduct(item: SearchProductItem) {
            item.product.amount -= item.product.factor()
            setAmountVisible(item)
        }

        private fun setAmountVisible(item: SearchProductItem) {
            if( item.product.amount == 0.0 ) {
                binding.amountContainer.visibility = View.GONE

                binding.plusColor?.set(R.color.gray)
            } else {
                binding.amount.text = item.product.formatAmount()
                binding.amountContainer.visibility = View.VISIBLE

                binding.plusColor?.set(R.color.plus)
            }
        }

        override fun unbindView(item: SearchProductItem) { }
    }
}