package com.lemo.emojcenter

import android.content.Context
import android.util.Log
import com.lemo.emojcenter.constant.FaceLocalConstant
import com.lemo.emojcenter.manage.FaceDownloadFaceManage
import com.lemo.emojcenter.manage.FaceEmojManage
import com.lemo.emojcenter.utils.SharedPreferencesUtils
import com.lemo.emojcenter.utils.ULogToDevice

/**
 * Created by wangru
 * Date: 2018/2/28  20:54
 * mail: 1902065822@qq.com
 * describe:
 */

object FaceInitData {
    private val TAG = "EmotionInitData"
    const val USERID_DEFAULT = "0"
    /**
     * @return 是否已经取我的表情成功
     */
    var isIsRequestFaceSuc: Boolean = false
    lateinit var context: Context
    var userId: String = USERID_DEFAULT
        get() {
            if (field == USERID_DEFAULT && SharedPreferencesUtils.getinstance() != null) {
                field = SharedPreferencesUtils.getinstance().getStringValue(FaceLocalConstant.Key.USER_ID, USERID_DEFAULT)
            }
            return field
        }
        set(value) {
            SharedPreferencesUtils.getinstance().setStringValue(FaceLocalConstant.Key.USER_ID, value)
            field = value
        }

    fun init(context: Context) {
        FaceInitData.context = context
        SharedPreferencesUtils.init(context)
        isIsRequestFaceSuc = false
        Log.d(TAG, "获取所有表情数据")
        FaceDownloadFaceManage.getAllEmoj()
    }

    fun setAlias(userId: String) {
        ULogToDevice.setAdminName(userId)
        FaceInitData.userId = userId
        FaceEmojManage.instance.init()
        Log.d(TAG, "下载收藏表情和表情包")
        FaceDownloadFaceManage.downAndSaveHostFace(userId)
    }
}
