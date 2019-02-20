package com.lemo.emojcenter.api

import com.lemo.emojcenter.FaceConfigInfo
import com.lemo.emojcenter.FaceInitData
import com.lemo.emojcenter.constant.FaceIConstants
import com.lemo.emojcenter.constant.FaceNetConst
import com.zhy.http.okhttp.OkHttpUtils
import com.zhy.http.okhttp.callback.Callback

/**
 * 网络请求
 * Created by wangru
 * Date: 2018/4/19  13:06
 * mail: 1902065822@qq.com
 * describe:
 */

object FaceNetRequestApi {
    /**
     * 表情包下载成功后告诉后台
     *
     * @param userid 用户id
     * @param faceid 表情包id
     */

    fun downloadSuccess(userid: String, faceid: String, callback: Callback<*>) {
        OkHttpUtils
                .post()
                .url(FaceIConstants.DOWNLOADFACE)
                .addHeader(FaceIConstants.APP_KEY, FaceConfigInfo.appKeyValue)
                .addParams(FaceNetConst.USERID, userid)
                .addParams(FaceNetConst.FACEID, faceid)
                .build()
                .execute(callback)
    }

    /**
     * 获取下载表情包地址
     *
     * @param resourse
     * @param callback
     */
    fun getDownEmojZipUrl(resourse: String, callback: Callback<*>) {
        OkHttpUtils
                .get()
                .url(FaceIConstants.UPLOAD + "?key=" + resourse)
                .build()
                .execute(callback)
    }

    /**
     * 获取所有表情内容
     *
     * @param version
     * @param callback
     */
    fun getAllEmojInfo(version: Int, callback: Callback<*>) {
        OkHttpUtils
                .get()
                .url(FaceIConstants.ALL_EMOJ + "?version=" + version.toString())
                .addHeader(FaceIConstants.APP_KEY, FaceConfigInfo.appKeyValue)
                .build()
                .execute(callback)
    }


    /**
     * 获取某个表情信息内容
     *
     * @param faceId
     * @param callback
     */
    fun getEmojInfo(faceId: String, callback: Callback<*>) {
        OkHttpUtils
                .get()
                .url(FaceIConstants.DETAIL + "?faceId=" + faceId)
                .addHeader(FaceIConstants.APP_KEY, FaceConfigInfo.appKeyValue)
                .build()
                .execute(callback)
    }


    /**
     * 获取我的表情（表情包和收藏表情）
     *
     * @param userID
     * @param callback
     */
    fun getMyFaceAndCollect(userId: String, callback: Callback<*>) {
        OkHttpUtils
                .get()
                .url(FaceIConstants.HOSTFACE + "?userId=" + userId)
                .addHeader(FaceIConstants.APP_KEY, FaceConfigInfo.appKeyValue)
                .build()
                .execute(callback)
    }

    /**
     * 获取我的表情包
     * @param userId
     * @param callback
     */
    fun getMyEmoj(userId: String, callback: Callback<*>) {
        OkHttpUtils.post()
                .url(FaceIConstants.MY_EMOJ)
                .addHeader(FaceIConstants.APP_KEY, FaceConfigInfo.appKeyValue)
                .addParams(FaceNetConst.USERID, userId)
                .addParams(FaceNetConst.SHOWTYPE, "0")
                .addParams(FaceNetConst.PAGE, "100")
                .build()
                .execute(callback)
    }

    /**
     * 获取收藏表情
     * @param userId
     * @param callback
     * @param tag
     */
    fun getMyCollect(userId: String, callback: Callback<*>) {
        OkHttpUtils.get()
                .url(FaceIConstants.COLLECT_EMOJ + "?userId=" + userId)
                .addHeader(FaceIConstants.APP_KEY, FaceConfigInfo.appKeyValue)
                .build()
                .execute(callback)
    }

    fun getMyEmojDownRecord(userId: String, page: Int, size: Int, callback: Callback<*>) {
        OkHttpUtils.post()
                .url(FaceIConstants.MY_EMOJ)
                .addHeader(FaceIConstants.APP_KEY, FaceConfigInfo.appKeyValue)
                .addParams(FaceNetConst.USERID, userId)
                .addParams(FaceNetConst.SHOWTYPE, "1")
                .addParams(FaceNetConst.PAGE, page.toString())
                .addParams(FaceNetConst.SIZE, size.toString())
                .build()
                .execute(callback)
    }
}
