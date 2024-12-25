package com.example.tencentWork.util

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "emojis")
data class Emoji(
    @PrimaryKey(autoGenerate = true)
    val id: Long,         // 主键，自增
    val path: String,         // 图片路径
    val name: String,        // 图片名
    val uploadTime: Long,     // 创建时间戳
    var expirationTime: Long?  //过期时间

) {
    @Ignore
    var isSelected: Boolean = false // 是否被选中，仅用于视图状态管理

    @Ignore
    constructor(path: String, name: String, uploadTime: Long) : this(
        id = 0,  // Room will auto-generate the 'id'
        path = path,
        name = name,
        uploadTime = uploadTime,
        // 过期时间初始化为null
        expirationTime = null
    )
}
