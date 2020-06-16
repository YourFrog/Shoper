package com.example.shoper.ui.item

import android.view.View
import android.widget.PopupMenu
import com.example.shoper.R
import com.example.shoper.databinding.ItemProductBinding
import com.example.shoper.entity.Product
import com.example.shoper.model.Category
import com.example.shoper.model.ProductStatus
import com.example.shoper.model.ProductWeightType
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem


/**
 *  Item reprezentujący wyświetlenie kategorii na liście
 */
class ProductItem (
    val product: Product,
    val onClick: (ProductItem) -> Unit = {},
    val onBoughtClick: (ProductItem) -> Unit,
    val onWaitingClick: (ProductItem) -> Unit,
    val onPartClick: (ProductItem) -> Unit,
    val onNotFoundClick: (ProductItem) -> Unit,
    val onEditClick: (ProductItem) -> Unit,
    val onRemoveClick: (ProductItem) -> Unit
) : AbstractItem<ProductItem.ViewHolder>() {

    /** defines the type defining this item. must be unique. preferably an id */
    override val type: Int
        get() = R.layout.item_product.toInt()

    /** defines the layout which will be used for this item in the list */
    override val layoutRes: Int
        get() = R.layout.item_product

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    class ViewHolder(view: View) : FastAdapter.ViewHolder<ProductItem>(view) {

        private val binding by lazy {
            ItemProductBinding.bind(itemView)
        }

        override fun bindView(item: ProductItem, payloads: List<Any>) {

            val currentWeightType = ProductWeightType.valueOf(item.product.weightType)
            val currentStatus = ProductStatus.valueOf(item.product.status)

            binding.category.setOnClickListener {
                item.onClick(item)
            }
            binding.name.text = item.product.name
            binding.productWeight.text = itemView.context.getString(when(currentWeightType) {
                ProductWeightType.KILOGRAMS -> R.string.weight_kilo
                ProductWeightType.GRAMS -> R.string.weight_grams
                ProductWeightType.LITR -> R.string.weight_litr
                else -> R.string.weight_piece
            }, item.product.amount.toString())
            binding.menu.setOnClickListener {
                val popup = PopupMenu(itemView.context, it)

                popup.menuInflater.inflate(R.menu.menu_of_product_item, popup.menu)
                popup.setOnMenuItemClickListener {
                    when(it.itemId) {
                        R.id.remove -> item.onRemoveClick(item)
                        R.id.bought -> item.onBoughtClick(item)
                        R.id.waiting -> item.onWaitingClick(item)
                        R.id.part -> item.onPartClick(item)
                        R.id.no_found -> item.onNotFoundClick(item)
                        R.id.edit -> item.onEditClick(item)
                    }
                    false
                }

                when(currentStatus) {
                    ProductStatus.PART -> {
                        popup.menu.removeItem(R.id.part)
                    }
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