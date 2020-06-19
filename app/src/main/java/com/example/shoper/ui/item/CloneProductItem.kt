package com.example.shoper.ui.item

import android.view.View
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.databinding.ObservableBoolean
import com.example.shoper.R
import com.example.shoper.databinding.ItemCloneProductBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

/**
 *  Item wykorzystywany do klonowania przedmiotu
 */
class CloneProductItem (
    val name: String,
    val amount: Double,
    val productWeightType: String,
    val values: List<String>,
    val checked: ObservableBoolean,
    val onAmountChange: (CloneProductItem, Int) -> Unit,
    val onChecked: (CloneProductItem) -> Unit,
    val onUnchecked: (CloneProductItem) -> Unit
) : AbstractItem<CloneProductItem.ViewHolder>() {

    /** defines the type defining this item. must be unique. preferably an id */
    override val type: Int
    get() = R.layout.item_clone_product.toInt()

    /** defines the layout which will be used for this item in the list */
    override val layoutRes: Int
    get() = R.layout.item_clone_product

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    class ViewHolder(view: View) : FastAdapter.ViewHolder<CloneProductItem>(view) {

        private val binding = ItemCloneProductBinding.bind(itemView)

        override fun bindView(item: CloneProductItem, payloads: List<Any>) {
            binding.item = item
            binding.amountPicker.progress = item.amount.toInt()
            binding.amountPicker.max = item.values.size - 1
            binding.amountPicker.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(bar: SeekBar?, value: Int, fromUser: Boolean) {
                    val str = item.values[value]

                    binding.amountValue.text = str
                    item.onAmountChange(item, value)
                }

                override fun onStartTrackingTouch(p0: SeekBar?) { }

                override fun onStopTrackingTouch(p0: SeekBar?) { }
            })
            binding.name.setOnClickListener {
                binding.checkbox.isChecked = !binding.checkbox.isChecked
            }

            binding.amountValue.text = item.values[binding.amountPicker.progress]
            binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                binding.amountPicker.isEnabled = isChecked
                item.checked.set(isChecked)

                if( isChecked ) {
                    item.onChecked(item)
                } else {
                    item.onUnchecked(item)
                }
            }

            binding.amountPicker.isEnabled = item.checked.get()
        }

        override fun unbindView(item: CloneProductItem) { }
    }
}