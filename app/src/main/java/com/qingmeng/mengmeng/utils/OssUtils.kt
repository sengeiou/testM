package com.qingmeng.mengmeng.utils

import android.text.TextUtils
import java.lang.StringBuilder

/**
 * Created by wr
 * Date: 2019/5/29  16:01
 * mail: 1902065822@qq.com
 * describe:
 */
object OssUtils {
    /**
     * 是否能添加oss样式
     *
     * @param url
     * @return true 可拼接参数?x-oss-process
     */
    fun isOssUrlStyleAble(url: String): Boolean {
        return !TextUtils.isEmpty(url) && url.startsWith("http") && !url.contains("?x-oss-process") && !url.contains("@!s") && !url.contains("@!f")
    }

    /**
     * 根据宽高缩放
     *
     * @return
     */
    fun getZoomProcess(width: Int, height: Int): String {
        return "?x-oss-process=image/resize,w_$width,h_$height"
    }

    /**
     * @param url
     * @param width
     * @param height
     * @param mfit   延伸出指定w与h的矩形框外的最小图片
     * @param round
     * @return
     */
    fun getZoomProcessCrop(url: String, width: Int, height: Int, round: Int = 0, mfit: Boolean = false): String {
        val stringBuffer = StringBuilder()
        if (!TextUtils.isEmpty(url)) {
            stringBuffer.append(url)
        }
        val isHaveValue = width > 0 || height > 0 || round > 0
        if (isOssUrlStyleAble(url) && isHaveValue) {
            stringBuffer.append("?x-oss-process=image")
            if (width > 0 || height > 0) {
                stringBuffer.append("/resize")
                if (width > 0) {
                    stringBuffer.append(",w_$width")
                }
                if (height > 0) {
                    stringBuffer.append(",h_$height")
                }
                if (mfit) {
                    stringBuffer.append(",m_fill,limit_0")
                }
            }
            if (round > 0) {
                stringBuffer.append("/rounded-corners,r_$round/format,png")
            }
        }
        return stringBuffer.toString()
    }

    /**
     * 根据宽缩放
     *
     * @return
     */
    fun wrapByWidth(url: String,width: Int): String {
        return "$url?x-oss-process=image/resize,w_$width,limit_0/format,webp"
    }

    /**
     * 根据高缩放
     *
     * @return
     */
    fun wrapByHeight(url: String, height: Int): String {
        return "$url?x-oss-process=image/resize,h_$height,limit_0/format,webp"
    }

    /**
     * 根据宽高缩放并中间裁剪
     *
     * @return
     */
    fun getZoomCropProcess(url: String, width: Int, height: Int): String {
        return "$url?x-oss-process=image/resize,w_$width,h_$height,m_fill,limit_0/format,webp"
    }

    /**
     * 圆形处理 先缩放 后裁剪
     *
     * @param width ImageView宽度  px
     * @param r     圆半径
     * @return
     */
    @JvmOverloads
    fun getCircleProcess(url: String,width: Int, r: Int = width / 2): String {
        return "$url?x-oss-process=image/resize,w_$width/circle,r_$r/format,webp"
    }

    /**
     * 圆角矩形
     *
     * @return
     */
    fun getRectRoundProcess(width: Int, round: Int): String {
        return "?x-oss-process=image/resize,w_$width,h_$width,m_fill/rounded-corners,r_$round/format,webp"
    }
}
