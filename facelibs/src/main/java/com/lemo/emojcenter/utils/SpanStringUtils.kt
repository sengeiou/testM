/**
 *
 */

package com.lemo.emojcenter.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.widget.TextView
import java.util.regex.Pattern

/**
 * @description :文本中的emojb字符处理为表情图片
 */
object SpanStringUtils {

    fun getEmotionContent(emotion_map_type: Int, context: Context, source: String, tv: TextView? = null, bigger: Boolean = false): SpannableString {
        val spannableString = SpannableString(source)
        val res = context.resources

        val regexEmotion = "\\[([\u4e00-\u9fa5\\w])+\\]"
        val patternEmotion = Pattern.compile(regexEmotion)
        val matcherEmotion = patternEmotion.matcher(spannableString)

        while (matcherEmotion.find()) {
            // 获取匹配到的具体字符
            val key = matcherEmotion.group()
            // 匹配字符串的开始位置
            val start = matcherEmotion.start()
            // 利用表情名字获取到对应的图片
            val imgRes = EmotionUtils.getImgByName(emotion_map_type, key)
            if (imgRes != -1) {
                // 压缩表情图片
                val size = if (bigger) {
                    (tv?.textSize?.toInt() ?: 42) * 16 / 10
                } else {
                    (tv?.textSize?.toInt() ?: 42) * 13 / 10
                }
                val bitmap = BitmapFactory.decodeResource(res, imgRes)
                val scaleBitmap = Bitmap.createScaledBitmap(bitmap, size, size, true)

                val span = ImageSpan(context, scaleBitmap)
                spannableString.setSpan(span, start, start + key.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        return spannableString
    }
}
