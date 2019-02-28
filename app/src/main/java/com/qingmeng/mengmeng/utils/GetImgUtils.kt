package com.qingmeng.mengmeng.utils

import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.util.*

/**
 * Description : 获取相册工具
 */
object GetImgUtils {
    private val cameraPath = "/DCIM/Camera"
    private val screentShotsPath = "/DCIM/Screenshots"
    private val screentPicturePath = "/Pictures/Screenshots"

    /**
     * 获取截图路径
     */
    private val screenshotsPath: String
        get() {
            var path = Environment.getExternalStorageDirectory().toString() + screentShotsPath
            val file = File(path)
            if (!file.exists()) {
                path = Environment.getExternalStorageDirectory().toString() + screentPicturePath
            }
            return path
        }

    data class ImgBean(
            var mTime: Long,
            var imgUrl: String
    )

    /**
     * 获取相册中最新一张图片
     */
    fun getLatestPhoto(context: Context, limit: Int = 1): List<ImgBean> {
        //拍摄照片的地址
        val CAMERA_IMAGE_BUCKET_NAME = Environment.getExternalStorageDirectory().toString() + cameraPath
        //截屏照片的地址
        val SCREENSHOTS_IMAGE_BUCKET_NAME = screenshotsPath
        //拍摄照片的地址ID
        val CAMERA_IMAGE_BUCKET_ID = getBucketId(CAMERA_IMAGE_BUCKET_NAME)
        //截屏照片的地址ID
        val SCREENSHOTS_IMAGE_BUCKET_ID = getBucketId(SCREENSHOTS_IMAGE_BUCKET_NAME)
        //查询路径和修改时间
        val projection = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_MODIFIED)
        //
        val selection = MediaStore.Images.Media.BUCKET_ID + " = ?"
        //
        val selectionArgs = arrayOf(CAMERA_IMAGE_BUCKET_ID)
        val selectionArgsForScreenshots = arrayOf(SCREENSHOTS_IMAGE_BUCKET_ID)

        //检查camera文件夹，查询并排序
        val imgBeans = ArrayList<ImgBean>()
        var cursor = context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC")
        var index = 0
        while (cursor!!.moveToNext()) {
            if (index >= limit)
                break
            val mtime = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED))
            val imgUrl = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            imgBeans.add(ImgBean(mtime, imgUrl))
            index++
        }
        if (!cursor.isClosed) {
            cursor.close()
        }
        //检查Screenshots文件夹
        //查询并排序
        cursor = context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgsForScreenshots,
                MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC")

        index = 0
        while (cursor!!.moveToNext()) {
            if (index >= limit)
                break
            val mtime = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED))
            val imgUrl = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            imgBeans.add(ImgBean(mtime, imgUrl))
            index++
        }
        if (!cursor.isClosed) {
            cursor.close()
        }
        //排序对比时间
        Collections.sort(imgBeans) { imgBean, t1 -> (t1.mTime - imgBean.mTime).toInt() }
        val imgBeans1 = ArrayList<ImgBean>()
        index = 0
        for (imgBean in imgBeans) {
            if (index >= limit)
                break
            imgBeans1.add(imgBean)
            index++
        }
        return imgBeans1
    }

    private fun getBucketId(path: String): String {
        return path.toLowerCase().hashCode().toString()
    }
}
