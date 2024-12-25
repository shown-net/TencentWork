package com.example.tencentWork.util

import androidx.recyclerview.widget.DiffUtil

class EmojiDiffCallback : DiffUtil.ItemCallback<Emoji>() {

    // 比较两个项是否是相同的项（唯一标识）
    override fun areItemsTheSame(oldItem: Emoji, newItem: Emoji): Boolean {
        return oldItem.id == newItem.id
    }

    // 比较两个项的内容是否相同
    override fun areContentsTheSame(oldItem: Emoji, newItem: Emoji): Boolean {
        return oldItem == newItem
                && oldItem.isSelected == newItem.isSelected
                && oldItem.expirationTime == newItem.expirationTime
    }
}

