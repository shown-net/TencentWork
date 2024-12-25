package com.example.tencentWork.observer

import androidx.fragment.app.Fragment

object SelectionModeManager {

    // 共享选择模式状态
    private var isSelectionMode = false

    // 存储Fragment的观察者及其活跃状态
    private val observersMap = mutableMapOf<Int, ObserverState>()

    // 获取当前选择模式状态
    fun isSelectionMode() = isSelectionMode

    // 设置选择模式状态并通知观察者
    fun setSelectionMode(isMode: Boolean) {
        // 如果模式没有变化，直接返回
        if (isSelectionMode == isMode) return

        isSelectionMode = isMode

        // 遍历所有活跃的观察者并通知
        observersMap.entries.filter { it.value.isActive }.forEach { entry ->
            entry.value.observer.onSelectionModeChanged(isSelectionMode)
        }
    }

    // 通知观察者进行删除行为
    fun removeItems() {
        // 遍历所有活跃的观察者并通知删除行为
        observersMap.entries.filter { it.value.isActive }.forEach { entry ->
            entry.value.observer.removeItems()
        }
    }

    fun restoreItems() {
        // 遍历所有活跃的观察者并通知恢复行为
        observersMap.entries.filter { it.value.isActive }.forEach { entry ->
            entry.value.observer.restoreItems()
        }
    }

    // 添加观察者
    fun addObserver(fragment: Fragment, observer: SelectionModeObserver) {
        val key = fragment.hashCode()
        observersMap[key] = ObserverState(observer, true)
    }

    // 设置观察者为活跃状态
    fun setActive(fragment: Fragment) {
        val key = fragment.hashCode()
        observersMap[key]?.isActive = true
    }

    // 设置观察者为非活跃状态
    fun setInactive(fragment: Fragment) {
        val key = fragment.hashCode()
        observersMap[key]?.isActive = false
    }

    // 移除观察者
    fun removeObserver(fragment: Fragment) {
        val key = fragment.hashCode()
        observersMap.remove(key)
    }


    // 内部数据类用于存储观察者和它的活跃状态
    private data class ObserverState(
        val observer: SelectionModeObserver, // 观察者
        var isActive: Boolean // 观察者是否活跃
    )
}

// 观察者接口
interface SelectionModeObserver {
    // 选择模式改变时通知
    fun onSelectionModeChanged(isSelectionMode: Boolean)

    // 从回收站恢复图片
    fun restoreItems()

    // 删除图片（送到回收站或者文件删除）
    fun removeItems()
}
