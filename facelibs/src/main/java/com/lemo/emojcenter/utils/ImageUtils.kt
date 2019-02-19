package com.lemo.emojcenter.utils

import android.graphics.BitmapFactory
import android.util.Log
import java.io.FileInputStream

/**
 * Created by wangru
 * Date: 2018/3/21  11:27
 * mail: 1902065822@qq.com
 * describe:
 */

object ImageUtils {
    val SCALE_LIMIT = 4f
    private val TAG = "ImageUtils"

    /**
     * 检测图片是否损坏
     *
     * @param filePath
     * @return
     */
    fun isImageComplete(filePath: String): Boolean {
        try {
            var options: BitmapFactory.Options? = null
            if (options == null) {
                options = BitmapFactory.Options()
            }
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(filePath, options)
            return !(options.mCancel || options.outWidth == -1 || options.outHeight == -1) && isImage(filePath) != ImageFormat.NoImage
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    fun getImageSize(filePath: String): ImageSize {
        val imageSize = ImageSize()
        try {
            var options: BitmapFactory.Options? = null
            if (options == null) {
                options = BitmapFactory.Options()
            }
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(filePath, options)
            imageSize.width = options.outWidth
            imageSize.height = options.outHeight
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return imageSize
    }

    /**
     * 是否为gif
     *
     * @param srcFileName 图片路径
     * @return
     */
    fun isImageGif(srcFileName: String): Boolean {
        return isImage(srcFileName) == ImageFormat.GIF
    }

    fun isImage(srcFileName: String): Int {
        var imgFile: FileInputStream? = null
        val b = ByteArray(10)
        var l = -1
        try {
            imgFile = FileInputStream(srcFileName)
            l = imgFile.read(b)
            imgFile.close()
        } catch (e: Exception) {
            return ImageFormat.NoImage
        }

        if (l == 10) {
            val b0 = b[0]
            val b1 = b[1]
            val b2 = b[2]
            val b3 = b[3]
            val b6 = b[6]
            val b7 = b[7]
            val b8 = b[8]
            val b9 = b[9]
            return if (b0 == 'G'.toByte() && b1 == 'I'.toByte() && b2 == 'F'.toByte()) {
                ImageFormat.GIF
            } else if (b1 == 'P'.toByte() && b2 == 'N'.toByte() && b3 == 'G'.toByte()) {
                ImageFormat.PNG
            } else if (b6 == 'J'.toByte() && b7 == 'F'.toByte() && b8 == 'I'.toByte() && b9 == 'F'.toByte()) {
                ImageFormat.JPG
            } else if (b6 == 'E'.toByte() && b7 == 'x'.toByte() && b8 == 'i'.toByte() && b9 == 'f'.toByte()) {
                ImageFormat.EXIF
            } else {
                ImageFormat.NoImage
            }
        } else {
            return ImageFormat.NoImage
        }
    }

    fun getVideoSize(width: Int, height: Int, maxWidth: Int, maxHeight: Int): ImageSize {
        val imageSize = ImageSize()
        val ratioWH = width.toFloat() / height.toFloat()
        val ratioWrap = maxWidth.toFloat() / maxHeight.toFloat()
        var resultWidth = width
        var resultHeight = height
        if (ratioWH > ratioWrap) {
            if (width > maxWidth) {
                resultWidth = maxWidth
                resultHeight = (resultWidth / ratioWH).toInt()
            }
        } else {
            if (height > maxHeight) {
                resultHeight = maxHeight
                resultWidth = (resultHeight * ratioWH).toInt()
            }
        }
        if (resultWidth % 2 != 0) {
            resultWidth = resultWidth + 1
        }
        if (resultHeight % 2 != 0) {
            resultHeight = resultHeight + 1
        }
        imageSize.width = resultWidth
        imageSize.height = resultHeight
        Log.d(TAG, "getVideoSize:  #width=$width #height=$height #resultWidth=$resultWidth #resultHeight=$resultHeight")
        return imageSize
    }

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
        Log.d(TAG, "getImageSizeWrap:  #width=$width #height=$height #resultWidth=$resultWidth #resultHeight=$resultHeight")
        return imageSize
    }

    fun getImageScale(imageWidth: Int, imageHeight: Int, viewWidth: Int, viewHeight: Int): Float {
        val imageW = imageWidth.toFloat()
        val imageH = imageHeight.toFloat()
        val viewW = viewWidth.toFloat()
        val viewH = viewHeight.toFloat()
        val scale_1 = viewW / imageW
        val scale_2 = viewH / imageH
        val scale_3 = viewW / imageH
        val scale_4 = viewH / imageW
        var zoomRate = 1f
        val scale = Math.min(scale_1, scale_2)
        if (scale_1 > 2 || scale_1 < 0.5) {
            zoomRate = Math.min(Math.max(scale_1, scale_2), Math.max(scale_3, scale_4))
            if (zoomRate > SCALE_LIMIT) {
                zoomRate = SCALE_LIMIT
            }
            if (scale >= zoomRate) {
                zoomRate = Math.min(Math.max(scale_1, scale_2), Math.max(scale_3, scale_4))
                if (zoomRate > SCALE_LIMIT) {
                    zoomRate = SCALE_LIMIT
                }
            }
        } else {
            var largerInitRate = Math.max(Math.min(scale_1, scale_2), Math.min(scale_3, scale_4))
            if (largerInitRate > SCALE_LIMIT) {
                largerInitRate = SCALE_LIMIT
            }
            zoomRate = Math.min(scale_2, largerInitRate * 2.0f)
            if (zoomRate > SCALE_LIMIT) {
                zoomRate = SCALE_LIMIT
            }
            if (scale >= zoomRate) {
                zoomRate = Math.min(scale_2, largerInitRate * 2.0f)
                if (zoomRate > SCALE_LIMIT) {
                    zoomRate = SCALE_LIMIT
                }
            }
        }
        return zoomRate
    }

    internal interface ImageFormat {
        companion object {
            val NoImage = 0
            val GIF = 1
            val PNG = 2
            val JPG = 3
            val EXIF = 4
        }
    }

    class ImageSize {
        //是否太长或太高
        var isNoWrap: Boolean = false
        var width: Int = 0
        var height: Int = 0
    }


}
