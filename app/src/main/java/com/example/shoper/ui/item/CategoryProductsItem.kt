package com.example.shoper.ui.item

import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.shoper.R
import com.example.shoper.databinding.ItemCategoryProductsBinding
import com.example.shoper.databinding.ItemProductBinding
import com.example.shoper.model.ProductStatus
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.ISelectionListener
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.select.SelectExtension
import com.mikepenz.fastadapter.select.getSelectExtension
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CategoryProductsItem (
    val title: String,
    val productsAdapter: ItemAdapter<ProductItem>,
    val onProductSwipe: (ProductItem, ProductStatus) -> Unit,
    val onProductSelectionChange: (SelectExtension<ProductItem>, ProductItem, Boolean) -> Unit
) : AbstractItem<CategoryProductsItem.ViewHolder>() {

    /** defines the type defining this item. must be unique. preferably an id */
    override val type: Int
        get() = R.layout.item_category_products.toInt()

    /** defines the layout which will be used for this item in the list */
    override val layoutRes: Int
        get() = R.layout.item_category_products

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    class ViewHolder(view: View) : FastAdapter.ViewHolder<CategoryProductsItem>(view) {

        val binding by lazy {
            ItemCategoryProductsBinding.bind(itemView)
        }

        override fun bindView(item: CategoryProductsItem, payloads: List<Any>) {
            binding.productsRecycler.adapter = FastAdapter.with(item.productsAdapter).apply {

                val extension = this.getSelectExtension()

                extension.apply {
                    isSelectable = true
                    multiSelect = true
                    selectOnLongClick = true
                    selectionListener = object: ISelectionListener<ProductItem> {
                        override fun onSelectionChanged(productItem: ProductItem, selected: Boolean) {
                            if( selected ) {
                                productItem.menuVisisble.set(View.INVISIBLE)
                                productItem.backgroundColor.set(R.color.product_selected)
                            } else {
                                productItem.menuVisisble.set(View.VISIBLE)
                                productItem.backgroundColor.set(R.color.transparent)
                            }

                            item.onProductSelectionChange(extension, productItem, selected)
                            item.productsAdapter.itemList.items.map {
                                it.buttonVisible.set(false)
                            }
                        }
                    }
                }
            }

            attachSwipe(item)

            binding.item = item
        }

        override fun unbindView(item: CategoryProductsItem) { }

        private fun attachSwipe(item: CategoryProductsItem) {
            ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.END or ItemTouchHelper.START
            ) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    when(viewHolder) {
                        is ProductItem.ViewHolder -> {
                            val productItem = viewHolder.productItem
                            val currentStatus = ProductStatus.valueOf(productItem.product.status)

                            when {
                                direction.and(ItemTouchHelper.END) == 0 -> when(currentStatus) {
                                    // W lewo
                                    ProductStatus.WAITING -> ProductStatus.NO_FOUND
                                    ProductStatus.BOUGHT -> ProductStatus.NO_FOUND
                                    ProductStatus.NO_FOUND -> ProductStatus.BOUGHT
                                    else -> null
                                }
                                direction.and(ItemTouchHelper.START) == 0 -> when(currentStatus) {
                                    // W prawo
                                    ProductStatus.WAITING -> ProductStatus.BOUGHT
                                    ProductStatus.BOUGHT -> ProductStatus.WAITING
                                    ProductStatus.NO_FOUND -> ProductStatus.WAITING
                                    else -> null
                                }
                                else -> null
                            }?.apply {
                                item.onProductSwipe(productItem, this)
                            }

                        }
                    }
                    val i = 0
                }

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }
            }).apply {
                attachToRecyclerView(binding.productsRecycler)
            }
        }
    }
}