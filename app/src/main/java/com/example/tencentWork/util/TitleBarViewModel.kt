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

class TitleBarViewModel(application: Application) : AndroidViewModel(application) {

    private val _activeFragment = MutableLiveData<String>()

    val activeFragment: LiveData<String> = _activeFragment


    fun setActiveFragment(fragmentTag: String) {
        _activeFragment.value = fragmentTag
    }

    // 功能按钮-模式切换的静态对象
    companion object {
        const val AddMode: String = "ADD"
        const val RestoreMode: String = "RESTORE"
        // 当前状态
        var status: String = AddMode
    }

}

