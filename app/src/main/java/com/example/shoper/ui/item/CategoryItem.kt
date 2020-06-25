package com.example.shoper.ui.item

import android.view.View
import android.widget.PopupMenu
import com.example.shoper.R
import com.example.shoper.databinding.ItemCategoryBinding
import com.example.shoper.model.ProductStatus
import com.example.shoper.model.ShopList
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem


/**
 *  Item reprezentujący wyświetlenie kategorii na liście
 */
class CategoryItem (
    val shopList: ShopList,
    val onClick: (CategoryItem) -> Unit = {},
    val onRemoveClick: (CategoryItem) -> Unit,
    val onAddProductClick: (CategoryItem) -> Unit,
    val onEditClick: (CategoryItem) -> Unit,
    val onCloneClick: (CategoryItem) -> Unit
) : AbstractItem<CategoryItem.ViewHolder>() {

    /** defines the type defining this item. must be unique. preferably an id */
    override val type: Int
        get() = R.layout.item_category.toInt()

    /** defines the layout which will be used for this item in the list */
    override val layoutRes: Int
        get() = R.layout.item_category

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    class ViewHolder(view: View) : FastAdapter.ViewHolder<CategoryItem>(view) {

        override fun bindView(item: CategoryItem, payloads: List<Any>) {
            val binding = ItemCategoryBinding.bind(itemView)

            binding.mainContainer.setOnClickListener {
                item.onClick(item)
            }
            binding.name.text = item.shopList.category.name

            item.shopList.products.let { products ->
                binding.percentOfComplete.text = (products.filter { it.status == ProductStatus.BOUGHT.toString() }.size / products.size.toDouble() * 100).toInt().toString() + "%"
                binding.amountOfElements.text =
                    itemView.context.getString(
                        R.string.category_elements,
                        products.filter { it.status == ProductStatus.WAITING.toString() }.size,
                        products.filter { it.status == ProductStatus.BOUGHT.toString() }.size,
                        products.size
                    )
            }

            binding.menu.setOnClickListener {
                val popup = PopupMenu(itemView.context, it)

                popup.menuInflater.inflate(R.menu.menu_of_category_item, popup.menu)
                popup.setOnMenuItemClickListener {
                    when(it.itemId) {
                        R.id.remove -> {
                            item.onRemoveClick(item)
                        }
                        R.id.add_product -> {
                            item.onAddProductClick(item)
                        }
                        R.id.clone -> {
                            item.onCloneClick(item)
                        }
                        R.id.edit -> {
                            item.onEditClick(item)
                        }
                    }
                    false
                }

                popup.show()
            }
        }

        override fun unbindView(item: CategoryItem) { }
    }
}