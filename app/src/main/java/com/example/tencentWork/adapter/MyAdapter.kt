package com.example.tencentWork.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.tencentWork.R
import com.example.tencentWork.databinding.ItemViewGridBinding
import com.example.tencentWork.observer.SelectionModeManager
import com.example.tencentWork.util.Emoji
import com.example.tencentWork.util.EmojiDiffCallback

class MyAdapter : ListAdapter<Emoji, MyAdapter.MyViewHolder>(EmojiDiffCallback()) {

    inner class MyViewHolder(val binding: ItemViewGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Emoji) {
            Glide.with(binding.itemImage.context)
                .load("${item.path}/${item.name}")
                .override(150, 150)  // 缩放图片至150x150
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_load)
                .diskCacheStrategy(DiskCacheStrategy.ALL)  // 缓存策略
                .into(binding.itemImage)

            binding.selectButton.visibility =
                if (SelectionModeManager.isSelectionMode()) View.VISIBLE else View.GONE
            binding.selectButton.isChecked = item.isSelected
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemViewGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = currentList[position]
        holder.bind(item)

        // 单击切换选中状态 图片本身
        holder.itemView.setOnClickListener {
            if (SelectionModeManager.isSelectionMode()) {
                item.isSelected = !item.isSelected
                holder.binding.selectButton.isChecked = item.isSelected
            }
        }

        // 单击切换选中状态 选择按钮
        holder.binding.selectButton.setOnClickListener {
            if (SelectionModeManager.isSelectionMode()) {
                item.isSelected = !item.isSelected
                holder.binding.selectButton.isChecked = item.isSelected
            }
        }


        // 长按时触发选择模式进入
        holder.itemView.setOnLongClickListener {
            if (!SelectionModeManager.isSelectionMode()) {
                SelectionModeManager.setSelectionMode(true) // 通知管理器进入选择模式
            }
            true
        }
    }

    override fun getItemCount(): Int = currentList.size


    // 获取选中的项
    fun getSelectedItems(): List<Emoji> {
        return currentList.filter { it.isSelected }
    }

    // Adapter 接收选择模式变化通知
    fun onSelectionModeChanged(isSelectionMode: Boolean) {
        if (!isSelectionMode) {
            currentList.forEach { emoji -> emoji.isSelected = false }
        }
        notifyDataSetChanged()
    }
}
