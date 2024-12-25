package com.example.tencentWork.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.tencentWork.MainActivity.Companion.appContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.random.Random

object FileUtils {
    // 存放表情包文件的目录: /storage/emulated/0/Android/data/com.example.tencentWork/files/EmojiFiles
    private val EMOJI_FOLDER = "EmojiFiles"

    private val emojiFolder: File =
        File(appContext.getExternalFilesDir(null), EMOJI_FOLDER)

    init {
        // 初始化：创建存储目录
        if (!emojiFolder.exists()) {
            emojiFolder.mkdirs()
        }
    }

    // 获取文件名
    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                cursor.getString(nameIndex)
            } else {
                null
            }
        }
    }

    // 创建目标文件
    fun createTargetFile(context: Context, fileName: String): File {
        val targetDirectory = File(context.getExternalFilesDir(null), EMOJI_FOLDER)
        if (!targetDirectory.exists()) targetDirectory.mkdirs()

        // 拆分文件名和扩展名
        val dotIndex = fileName.lastIndexOf('.')
        val namePart = if (dotIndex > 0) fileName.substring(0, dotIndex) else fileName
        val extension = if (dotIndex > 0) fileName.substring(dotIndex) else ""

        // 生成随机文件名
        val targetFileName = "${namePart}_${Random.nextInt(1000, 9999)}$extension"
        return File(targetDirectory, targetFileName)
    }

    // 复制文件
    fun copyFile(contentResolver: ContentResolver, sourceUri: Uri, targetFile: File) {
        contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            FileOutputStream(targetFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw IOException("无法打开输入流")
    }

    //删除整个数据文件夹
    fun deleteFolder() {
        if (emojiFolder.exists()) {
            emojiFolder.delete()
        }
    }

    fun deleteFile(fileName: String) {
        val file = File(emojiFolder, fileName)
        if (file.exists()) {
            file.delete() // 删除文件
        }
    }

    fun deleteFileByList(fileNameList: List<String>) {
        fileNameList.forEach { fileName ->
            val file = File(emojiFolder, fileName)
            if (file.exists()) {
                val deleted = file.delete() // 尝试删除文件
                if (!deleted) {
                    Log.w("FileUtils", "文件删除失败: $fileName")
                }
            } else {
                Log.w("FileUtils", "文件不存在: $fileName")
            }
        }
    }

}