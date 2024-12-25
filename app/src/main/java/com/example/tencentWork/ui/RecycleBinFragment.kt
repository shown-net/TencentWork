package com.example.tencentWork.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager

import androidx.recyclerview.widget.RecyclerView
import com.example.tencentWork.adapter.MyAdapter
import com.example.tencentWork.databinding.FragRecycleBinBinding

import com.example.tencentWork.util.ActiveEmojiViewModel

import com.example.tencentWork.observer.SelectionModeManager
import com.example.tencentWork.observer.SelectionModeObserver
import com.example.tencentWork.util.Emoji
import com.example.tencentWork.util.ExpiredEmojiViewModel
import com.example.tencentWork.util.TitleBarViewModel

import kotlin.math.ceil


class RecycleBinFragment : Fragment() {

    private var _binding: FragRecycleBinBinding? = null
    private val binding get() = _binding!!

    private lateinit var emojiViewModel: ExpiredEmojiViewModel
    private lateinit var titleBarViewModel: TitleBarViewModel
    private lateinit var adapter: MyAdapter


    private var currentPage = 1 // 当前页数
    private var totalPage = 0 // 总页数

    override fun onResume() {
        super.onResume()
        // 当前 Fragment 处于活跃状态时，注册为观察者
        SelectionModeManager.setActive(this)
        // 每次刚开始处于活跃状态，设置选择模式为false
        SelectionModeManager.setSelectionMode(false)
        // 设置功能按钮为:从回收站恢复待删除图片
        titleBarViewModel.setActiveFragment(TitleBarViewModel.RestoreMode)
    }

    override fun onPause() {
        super.onPause()
        // 当前 Fragment 不再活跃时,设置状态
        SelectionModeManager.setInactive(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        emojiViewModel = ViewModelProvider(requireActivity())[ExpiredEmojiViewModel::class.java]
        titleBarViewModel = ViewModelProvider(requireActivity())[TitleBarViewModel::class.java]

        _binding = FragRecycleBinBinding.inflate(inflater, container, false)

        // 注册选择模式观察者
        SelectionModeManager.addObserver(this, object : SelectionModeObserver {
            // 观察者回调：选择模式改变时调用
            override fun onSelectionModeChanged(isSelectionMode: Boolean) {
                // 通知 Adapter 更新选中状态
                adapter.onSelectionModeChanged(isSelectionMode)
            }

            // 观察者回调：恢复行为（不应在相册fragment活跃时执行）
            override fun restoreItems() {
                val selectedItems = adapter.getSelectedItems()
                if (selectedItems.isNotEmpty()) {
                    emojiViewModel.restoreByList(selectedItems.map { emoji: Emoji -> emoji.id })
                }
            }

            // 观察者回调：删除选中项目时调用
            override fun removeItems() {
                val selectedItems = adapter.getSelectedItems()
                if (selectedItems.isNotEmpty()) {
                    emojiViewModel.deleteByList(selectedItems)
                }
            }
        })

        // 设置返回键监听
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // 如果当前处于选择模式，退出选择模式
                    if (SelectionModeManager.isSelectionMode()) {
                        SelectionModeManager.setSelectionMode(false)
                    } else {
                        // 否则执行默认的返回操作
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            })

        /*        for (i in 1..50) {
                    ///storage/emulated/0/Android/data/com.example.tencentWork/files/EmojiFiles
                    emojiViewModel.insert(
                        Emoji(
                            "/storage/emulated/0/Android/data/com.example.tencentWork/files/EmojiFiles",
                            "IMG_20241205_220022_5625.jpg",
                            DateUtils.localDateTimeToSeconds(LocalDateTime.now())
                        )
                    )
                }*/


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        // 加载第一页数据
        loadPage(0)
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = GridLayoutManager(context, 4)
        adapter = MyAdapter()
        binding.recyclerView.adapter = adapter

        // 更新总页数
        emojiViewModel.totalItemCount.observe(viewLifecycleOwner) { totalCount ->
            totalPage = ceil(totalCount.toDouble() / emojiViewModel.pageSize).toInt()
        }

        emojiViewModel.emojiList.observe(viewLifecycleOwner) { emojiList ->
            adapter.submitList(emojiList)
        }

        // 添加滚动监听器，检测是否滚动到底部并加载更多
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                // 判断是否需要加载更多
                if (lastVisibleItemPosition >= adapter.itemCount - 1 && currentPage < totalPage) {
                    //加载下一页
                    loadPage(currentPage + 1)
                }
            }
        })

    }

    private fun loadPage(page: Int) {
        //查询有过期时间的待删除图片
        emojiViewModel.loadPage(page)
        // 更新当前页码
        currentPage = page

    }


    override fun onDestroyView() {
        super.onDestroyView()
        // 移除观察者，避免内存泄漏
        SelectionModeManager.removeObserver(this)
        _binding = null
    }
}

