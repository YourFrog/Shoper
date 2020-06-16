package com.example.shoper.ui.item

import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import com.example.shoper.R
import com.example.shoper.databinding.ItemPickerBinding
import com.example.shoper.databinding.ItemProductBinding
import com.example.shoper.model.Category
import com.example.shoper.model.PickerValue
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem


/**
 *  Item reprezentujący wyświetlenie kategorii na liście
 */
class PickerItem (
    val item: PickerValue,
    val onClick: (PickerValue) -> Unit = {}
) : AbstractItem<PickerItem.ViewHolder>() {

    /** defines the type defining this item. must be unique. preferably an id */
    override val type: Int
        get() = R.layout.item_picker.toInt()

    /** defines the layout which will be used for this item in the list */
    override val layoutRes: Int
        get() = R.layout.item_picker

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    class ViewHolder(view: View) : FastAdapter.ViewHolder<PickerItem>(view) {

        override fun bindView(item: PickerItem, payloads: List<Any>) {
            val binding = ItemPickerBinding.bind(itemView)

            binding.name.text = item.item.name
            binding.box.setOnClickListener {
                item.onClick(item.item)
            }
        }

        override fun unbindView(item: PickerItem) { }
    }
}