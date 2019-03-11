package com.qingmeng.mengmeng.utils

import android.util.Log

/**
 *  Description :

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/3/8
 */
object ImageUtils {
    //获取自适应宽高 比例太大 会改比例
    fun getImageSizeWrap(width: Int, height: Int, minWidth: Int, maxWidth: Int, minHeight: Int, maxHeight: Int): ImageSize {
        val imageSize = ImageSize()
        val ratioWH = width.toFloat() / height.toFloat()
        val ratioWHMax = maxWidth.toFloat() / minHeight.toFloat()
        val ratioWHMin = minWidth.toFloat() / maxHeight.toFloat()
        val ratioWrap = maxWidth.toFloat() / maxHeight.toFloat()
        var resultWidth = width
        var resultHeight = height
        if (ratioWH > ratioWHMax) {
            imageSize.isNoWrap = true
            resultWidth = maxWidth
            resultHeight = minHeight
        } else if (ratioWH < ratioWHMin) {

            resultWidth = minWidth
            resultHeight = maxHeight
        } else {
            if (ratioWH < ratioWrap) {
                if (height < minHeight) {
                    resultHeight = minHeight
                } else if (height > maxHeight) {
                    resultHeight = maxHeight
                } else {
                    resultHeight = height
                }
                resultWidth = (resultHeight * ratioWH).toInt()
            } else {
                if (width < minWidth) {
                    resultWidth = minWidth
                } else if (width > maxWidth) {
                    resultWidth = maxWidth
                } else {
                    resultWidth = width
                }
                resultHeight = (resultWidth / ratioWH).toInt()
            }
        }

        imageSize.width = resultWidth
        imageSize.height = resultHeight
        Log.i("", "getImageSizeWrap:  #width=$width #height=$height #resultWidth=$resultWidth #resultHeight=$resultHeight")
        return imageSize
    }

    class ImageSize {
        //是否太长或太高
        var isNoWrap: Boolean = false
        var width: Int = 0
        var height: Int = 0
    }
}