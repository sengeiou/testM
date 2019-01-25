package com.app.common.extensions

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import com.app.common.logger.Logger
import java.io.File
import java.nio.charset.Charset
import java.util.regex.Pattern


fun String.writeTo(target: String) {
    val file = File(target)
    if (!file.exists()) file.createNewFile()
    file.writeText(this, Charset.defaultCharset())
}

fun String?.isEmailExt(): Boolean = !this.isNullOrBlank() && Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*").matcher(this).matches()

//密码半角（所有英文字符英文符号）
fun String.isPasswordExtHalfAngle(): Boolean = Pattern.compile("^[\u0000-\u00FF]+$").matcher(this).matches()

fun String.toSpannableStringExt(color: Int): SpannableString =
        SpannableString(this).apply { setSpan(ForegroundColorSpan(color), 0, this.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) }

//获取String里面的数字转int
fun String.getNumExt(): Int {
    try {
        return Pattern.compile("[^0-9]").matcher(this).replaceAll("").trim().toInt()
    } catch (e: Exception) {
        Logger.d("getNumExt#${this} to Int error")
        return -1
    }
}

//失败返回-1
fun String.toIntExt(): Int {
    try {
        return this.toInt()
    } catch (e: Exception) {
        return -1
    }
}


