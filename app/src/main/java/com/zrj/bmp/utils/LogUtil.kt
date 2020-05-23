package com.zrj.bmp.utils

import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import com.zrj.bmp.BuildConfig

/**
 * Created by zrj 2017/6/10.
 */
object LogUtil {
    private var logEnabled = BuildConfig.DEBUG
    private var tag = "zrj"

    fun e(msg: String, customTag: String = tag) {
        log(Log.ERROR, customTag, msg)
    }

    fun json(msg: String, customTag: String = tag) {
        val json = formatJson(msg)
        log(Log.ERROR, customTag, json)
    }

    /**
     * 格式化json
     */
    private fun formatJson(json: String): String {
        return try {
            val trimJson = json.trim()
            when {
                trimJson.startsWith("{") -> JSONObject(trimJson).toString(4)
                trimJson.startsWith("[") -> JSONArray(trimJson).toString(4)
                else -> trimJson
            }
        } catch (e: JSONException) {
            e.printStackTrace().toString()
        }
    }

    /**
     * 输出日志
     * @param priority 日志级别
     */
    private fun log(priority: Int, customTag: String, msg: String) {
        if (!logEnabled) return
        val elements = Thread.currentThread().stackTrace
        val index = findIndex(elements)
        val element = elements[index]
        val tag = handleTag(element, customTag)
        val content = "(${element.fileName}:${element.lineNumber}).${element.methodName}:  $msg"
        Log.println(priority, tag, content)
    }


    /**
     * 处理tag逻辑
     */
    private fun handleTag(element: StackTraceElement, customTag: String): String = when {
        customTag.isNotBlank() -> customTag
        else -> element.className.substringAfterLast(".")
    }

    /**
     * 寻找当前调用类在[elements]中的下标
     */
    private fun findIndex(elements: Array<StackTraceElement>): Int {
        var index = 5
        while (index < elements.size) {
            val className = elements[index].className
            if (className != LogUtil::class.java.name && !elements[index].methodName.startsWith("log")) {
                return index
            }
            index++
        }
        return -1
    }
}





