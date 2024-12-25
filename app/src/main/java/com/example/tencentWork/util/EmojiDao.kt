package com.example.tencentWork.util

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete

@Dao
interface EmojiDao {

    @Query(
        """
    SELECT * FROM emojis 
    WHERE 
        (:showExpired = 1 AND expirationTime > 0) 
        OR (:showExpired = 0 AND expirationTime IS NULL) 
    ORDER BY id 
    LIMIT :limit OFFSET :offset
"""
    )
    fun getEmojis(showExpired: Boolean, limit: Int, offset: Int): List<Emoji>

    @Query(
        """
        SELECT Count(id) FROM emojis 
    WHERE 
        (:beingExpire = 1 AND expirationTime > 0) 
        OR (:beingExpire = 0 AND expirationTime IS NULL) 
"""
    )
    fun getEmojiCount(beingExpire: Boolean): LiveData<Int>

    // 查询一条数据
    @Query("SELECT * FROM emojis WHERE id = :id")
    fun getEmojiById(id: Long): Emoji?

    // 插入一条数据
    @Insert
    fun insertEmoji(emoji: Emoji): Long

    // 插入多条数据
    @Insert
    fun insertAll(emojis: List<Emoji>)

    // 查询所有表情数据
    @Query("SELECT * FROM emojis ORDER BY uploadTime DESC")
    fun getAllEmojis(): LiveData<List<Emoji>>

    //设置过期时间
    @Query("UPDATE emojis SET expirationTime = :newExpirationTime WHERE id IN (:emojiIds)")
    fun updateExpirationTime(emojiIds: List<Long>, newExpirationTime: Long)

    //取消过期状态
    @Query("UPDATE emojis SET expirationTime = null WHERE id IN (:emojiIds)")
    fun setEmojisActive(emojiIds: List<Long>,)

    // 删除指定表情
    @Delete
    fun deleteEmoji(emoji: Emoji)

    // 删除多个表情
    @Delete
    fun deleteEmojiByList(emojiList: List<Emoji>)

    // 清空表
    @Query("DELETE FROM emojis")
    fun clearTable(): Int
}
