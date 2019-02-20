package com.lemo.emojcenter.utils

import android.util.Log

/**
 * Description : 日志工具类
 * Author : lauren
 * Email  : lauren.liuling@gmail.com
 * Blog   : http://www.liuling123.com
 * Date   : 15/12/14
 */
object MyLogUtils {
    var DEBUG = true

    fun setDebug(bool: Boolean) {
        DEBUG = bool
    }

    fun v(tag: String, message: String) {
        if (DEBUG) {
            Log.v(tag, message)
        }
    }

    fun d(tag: String, message: String) {
        if (DEBUG) {
            Log.d(tag, message)
        }
    }

    fun i(tag: String, message: String) {
        if (DEBUG) {
            Log.i(tag, message)
        }
    }

    fun i(tag: String, message: String, tr: Throwable) {
        if (DEBUG) {
            Log.i(tag, message, tr)
        }
    }

    fun w(tag: String, message: String) {
        if (DEBUG) {
            Log.w(tag, message)
        }
    }

    fun e(tag: String, message: String) {
        if (DEBUG) {
            Log.e(tag, message)
        }
    }

    fun e(tag: String, message: String, e: Exception) {
        if (DEBUG) {
            Log.e(tag, message, e)
        }
    }

    fun e(message: String) {
        if (DEBUG) {
            Log.e("TAG", message)
        }
    }
}
