package com.example.tencentWork.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.tencentWork.R
import com.example.tencentWork.databinding.FragTitileBarBinding
import com.example.tencentWork.observer.SelectionModeManager
import com.example.tencentWork.util.DateUtils
import com.example.tencentWork.util.Emoji
import com.example.tencentWork.util.ActiveEmojiViewModel
import com.example.tencentWork.util.FileUtils
import com.example.tencentWork.util.TitleBarViewModel
import java.time.LocalDateTime

class TitleBarFragment : Fragment() {

    private var _binding: FragTitileBarBinding? = null
    private val binding get() = _binding!!

    private lateinit var emojiViewModel: ActiveEmojiViewModel
    private lateinit var titleBarViewModel: TitleBarViewModel

    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragTitileBarBinding.inflate(inflater, container, false)
        // 共享同一个相册viewModel (和photoAlbumFragment)
        emojiViewModel = ViewModelProvider(requireActivity())[ActiveEmojiViewModel::class.java]
        titleBarViewModel = ViewModelProvider(requireActivity())[TitleBarViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 注册图片选择事件
        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val context = requireContext()
                    val imageUri = result.data?.data ?: return@registerForActivityResult
                    try {
                        val fileName =
                            FileUtils.getFileNameFromUri(context, imageUri) ?: "unknown_file"
                        val targetFile = FileUtils.createTargetFile(context, fileName)
                        FileUtils.copyFile(context.contentResolver, imageUri, targetFile)
                        emojiViewModel.addEmoji(
                            Emoji(
                                targetFile.parent!!,
                                targetFile.name,
                                DateUtils.localDateTimeToSeconds(LocalDateTime.now())
                            )
                        )
                        Toast.makeText(
                            context,
                            "图片成功保存至: ${targetFile.absolutePath}",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "图片导入失败: ${e.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

        // 观察当前活跃 Fragment 状态
        titleBarViewModel.activeFragment.observe(viewLifecycleOwner) { activeFragment ->
            // 设置对应图标和当前状态
            when (activeFragment) {
                // 添加功能
                TitleBarViewModel.AddMode -> {
                    TitleBarViewModel.status = TitleBarViewModel.AddMode
                    binding.functionButton.setImageResource(R.drawable.button_add)
                }

                // 恢复功能
                TitleBarViewModel.RestoreMode -> {
                    TitleBarViewModel.status = TitleBarViewModel.RestoreMode
                    binding.functionButton.setImageResource(R.drawable.button_restore)
                }
            }
        }

        // 点击选择按钮，是否进入选择模式
        binding.selectButton.setOnClickListener {
            if (SelectionModeManager.isSelectionMode()) {
                // 退出选择模式
                binding.deleteButton.visibility = View.GONE
                SelectionModeManager.setSelectionMode(false)
            } else {
                // 进入选择模式
                binding.deleteButton.visibility = View.VISIBLE
                SelectionModeManager.setSelectionMode(true)
            }
        }


        // 点击功能按钮
        binding.functionButton.setOnClickListener {
            // 添加图片
            if (TitleBarViewModel.status == TitleBarViewModel.AddMode) {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                        type = "image/*"
                    }
                pickImageLauncher.launch(intent)
            }
            // 恢复待删除图片
            else {
                SelectionModeManager.restoreItems()
            }
            // 退出选择模式
            SelectionModeManager.setSelectionMode(false)

        }


        // 点击删除按钮
        binding.deleteButton.setOnClickListener {
            SelectionModeManager.removeItems()
            // 退出选择模式
            SelectionModeManager.setSelectionMode(false)
        }
    }

}
