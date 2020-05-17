package com.zrj.png.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import com.zrj.png.App

/**
 * Author：mengyuan
 * Date  : 2017/7/27下午6:12
 * E-Mail:mengyuanzz@126.com
 * Desc  :
 */
object UriUtil {
    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     */
    fun getPath(uri: Uri?): String? { // DocumentProvider
        if ( DocumentsContract.isDocumentUri(App.context, uri)) { // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id)
                )
                return getDataColumn(App.context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                when (type) {
                    "image" -> {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }
                    "video" -> {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }
                    "audio" -> {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return getDataColumn(App.context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri?.scheme, ignoreCase = true)) {
            return getDataColumn(App.context, uri, null, null)
        } else if ("file".equals(uri?.scheme, ignoreCase = true)) {
            return uri?.path
        }
        return null
    }

    /**
     * Get the value of the returnData column for this Uri. This is useful for MediaStore Uris, and other file-based ContentProviders.
     */
    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(column_index)
            }
        } catch (e: Exception) {
            Log.e("UriUtil", "Unable to get path")
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * Whether the Uri authority is ExternalStorageProvider.
     * 检测Uri是否是外部储存提供商
     */
    fun isExternalStorageDocument(uri: Uri?): Boolean {
        return "com.android.externalstorage.documents" == uri?.authority
    }

    /**
     * Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri?): Boolean {
        return "com.android.providers.downloads.documents" == uri?.authority
    }

    /**
     * Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri?): Boolean {
        return "com.android.providers.media.documents" == uri?.authority
    }
}