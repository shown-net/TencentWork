package com.example.tencentWork.util

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExpiredEmojiViewModel(application: Application) : AndroidViewModel(application) {
    private val emojiDao = EmojiDatabase.getInstance(application).emojiDao()

    private val _emojiList = MutableLiveData<List<Emoji>>()
    val emojiList: LiveData<List<Emoji>> get() = _emojiList

    var pageSize = 20
    val totalItemCount: LiveData<Int> = emojiDao.getEmojiCount(true)

    // 用于避免重复加载
    private var isLoading = false

    // 分页加载
    fun loadPage(page: Int) {
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val emojis =
                        emojiDao.getEmojis(true, pageSize, (page - 1) * pageSize)
                    val currentList = emojiList.value?.toMutableList() ?: mutableListOf()
                    val updatedList = (currentList + emojis).distinctBy { it.id }
                    _emojiList.postValue(updatedList)
                }
            } catch (e: Exception) {
                // 错误处理
                Log.e("EmojiViewModel", "Error loading page", e)
            } finally {
                isLoading = false
            }
        }
    }


    // 恢复多个图片
    fun restoreByList(idList: List<Long>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // 设置多个表情为激活状态
                emojiDao.setEmojisActive(idList)

                // 更新状态成功后，从当前 emojiList 删除这些活跃图片
                val updatedList = _emojiList.value?.filterNot { emoji ->
                    idList.contains(emoji.id)  // 通过 emoji.id 去过滤掉 idList 中的表情
                } ?: emptyList()

                // 更新列表
                _emojiList.postValue(updatedList)
            }
        }
    }


    // 删除多个表情
    fun deleteByList(emojiList: List<Emoji>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                emojiDao.deleteEmojiByList(emojiList)
                FileUtils.deleteFileByList(emojiList.map { it.name })
                // 删除成功后，更新 LiveData
                val updatedList =
                    _emojiList.value?.filterNot { emojiList.contains(it) } ?: emptyList()
                _emojiList.postValue(updatedList)
            }
        }
    }

    // 清空表情表及数据文件夹
    fun clearAll() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    emojiDao.clearTable()
                    FileUtils.deleteFolder()
                }
                _emojiList.postValue(emptyList())
            } catch (e: Exception) {
                Log.e("EmojiViewModel", "Error clearing all emojis", e)
            }
        }
    }

}

