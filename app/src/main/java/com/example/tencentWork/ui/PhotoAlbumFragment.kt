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
import androidx.lifecycle.distinctUntilChanged
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tencentWork.adapter.MyAdapter
import com.example.tencentWork.databinding.FragPhotoAlbumBinding
import com.example.tencentWork.util.ActiveEmojiViewModel
import com.example.tencentWork.observer.SelectionModeManager
import com.example.tencentWork.observer.SelectionModeObserver
import com.example.tencentWork.util.DateUtils
import com.example.tencentWork.util.EmojiDiffCallback
import com.example.tencentWork.util.TitleBarViewModel
import java.time.LocalDateTime
import kotlin.math.ceil


class PhotoAlbumFragment : Fragment() {

    private var _binding: FragPhotoAlbumBinding? = null
    private val binding get() = _binding!!

    private lateinit var emojiViewModel: ActiveEmojiViewModel
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
        // 设置功能按钮为:添加新图片
        titleBarViewModel.setActiveFragment(TitleBarViewModel.AddMode)
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
        _binding = FragPhotoAlbumBinding.inflate(inflater, container, false)

        // 共享同一个相册viewModel (和titleBarFragment)
        emojiViewModel = ViewModelProvider(requireActivity())[ActiveEmojiViewModel::class.java]

        titleBarViewModel = ViewModelProvider(requireActivity())[TitleBarViewModel::class.java]

        // 注册选择模式观察者
        SelectionModeManager.addObserver(this, object : SelectionModeObserver {
            // 观察者回调：选择模式改变时调用
            override fun onSelectionModeChanged(isSelectionMode: Boolean) {
                // 通知 Adapter 更新选中状态
                adapter.onSelectionModeChanged(isSelectionMode)
            }

            // 观察者回调：恢复行为（不应在相册fragment活跃时执行）
            override fun restoreItems() {
                Log.d("wrongAction", "相册fragment不支持恢复行为")
            }

            // 观察者回调：删除选中项目时调用
            override fun removeItems() {
                val selectedIdList = adapter.getSelectedItems().map { emoji -> emoji.id }
                if (selectedIdList.isNotEmpty()) {
                    // 设置过期时间(默认为7天后)
                    val expiredTime = DateUtils.localDateTimeToSeconds(
                        LocalDateTime.now()
                            .plusDays(DateUtils.expireDay)
                    )
                    emojiViewModel.updateExpirationTime(selectedIdList, expiredTime)
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
        loadPage(1)
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
            Log.d("observeAction", emojiList.size.toString())
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
        //查询没有过期时间的活跃图片
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


