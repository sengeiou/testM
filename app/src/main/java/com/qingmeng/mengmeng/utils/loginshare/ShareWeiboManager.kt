package com.qingmeng.mengmeng.utils.loginshare

import AppManager
import android.app.Activity
import android.graphics.Bitmap
import com.qingmeng.mengmeng.constant.IConstants
import com.sina.weibo.sdk.WbSdk
import com.sina.weibo.sdk.api.TextObject
import com.sina.weibo.sdk.api.WebpageObject
import com.sina.weibo.sdk.api.WeiboMultiMessage
import com.sina.weibo.sdk.auth.AuthInfo
import com.sina.weibo.sdk.share.WbShareCallback
import com.sina.weibo.sdk.share.WbShareHandler


/**
 * Created by wr
 * Date: 2018/8/28  21:17
 * describe: 微博分享工具类
 */
object ShareWeiboManager : WbShareCallback {
    private lateinit var mWbShareHandler: WbShareHandler    //微博分享管理类
    private lateinit var mAuthInfo: AuthInfo

    fun shareToWeibo(mActivity: Activity, url: String, title: String, description: String, bitmap: Bitmap) {
        //注册微博sdk
        mAuthInfo = AuthInfo(AppManager.instance.currentActivity(), IConstants.APP_KEY_SINA, IConstants.APP_REDIRECT_URL_SINA, IConstants.APP_SCOPE_SINA)
        WbSdk.install(mActivity, mAuthInfo)
        mWbShareHandler = WbShareHandler(mActivity)
        mWbShareHandler.registerApp()

        //分享内容
        val weiboMultiMessage = WeiboMultiMessage()
        //文本
        val textObject = TextObject()
        textObject.text = title
        weiboMultiMessage.textObject = textObject
//        //图片
//        val imageObject = ImageObject()
//        imageObject.setImageObject(bitmap)
//        weiboMultiMessage.imageObject = imageObject
        //跳转内容
        val webpageObject = WebpageObject()
        webpageObject.title = title
        webpageObject.description = description
        webpageObject.actionUrl = url
        webpageObject.setThumbImage(bitmap)
        weiboMultiMessage.mediaObject = webpageObject
        mWbShareHandler.shareMessage(weiboMultiMessage, false)
    }

    //失败
    override fun onWbShareFail() {

    }

    //取消
    override fun onWbShareCancel() {

    }

    //成功
    override fun onWbShareSuccess() {

    }
}