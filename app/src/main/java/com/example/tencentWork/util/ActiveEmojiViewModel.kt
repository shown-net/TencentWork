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

class ActiveEmojiViewModel(application: Application) : AndroidViewModel(application) {
    private val emojiDao = EmojiDatabase.getInstance(application).emojiDao()

    private val _emojiList = MutableLiveData<List<Emoji>>(emptyList())
    val emojiList: LiveData<List<Emoji>> get() = _emojiList

    var pageSize = 10
    val totalItemCount: LiveData<Int> = emojiDao.getEmojiCount(false)

    // 用于避免重复加载
    private var isLoading = false

    // 分页加载
    fun loadPage(page: Int) {
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val emojis = emojiDao.getEmojis(false, pageSize, (page - 1) * pageSize)
                    val currentList = _emojiList.value?.toMutableList() ?: mutableListOf()
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

    // 插入一条表情数据
    fun addEmoji(newEmoji: Emoji) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val rowId = emojiDao.insertEmoji(newEmoji)
                if (rowId > 0) {
                    // 插入成功
                    val currentList = _emojiList.value?.toMutableList() ?: mutableListOf()
                    currentList.add(newEmoji) // 修改当前列表
                    _emojiList.postValue(currentList.toList())

                } else {
                    // 插入失败，可能是主键冲突
                    Log.e("DatabaseError", "Insert failed for Emoji: $newEmoji")
                }
            }
        }
    }

    // 设置过期时间
    fun updateExpirationTime(emojiIds: List<Long>, newExpirationTime: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                emojiDao.updateExpirationTime(emojiIds, newExpirationTime)
                // 从活跃列表删除哪些被选中的过期图片
                val updatedList =
                    _emojiList.value?.filterNot { emoji -> emojiIds.contains(emoji.id) }
                        ?: emptyList()
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

