package com.qingmeng.mengmeng.utils.loginshare


/**
 * Created by wr
 * Date: 2018/8/28  21:17
 * describe: 微博分享工具类
 */
//object ShareWeiboManager : WbShareCallback {
//    private lateinit var mWbShareHandler: WbShareHandler    //微博分享管理类
//    private lateinit var mAuthInfo: IMBaseDefine.AuthInfo
//
//    fun shareToWeibo(mActivity: Activity, title: String, description: String, bitmap: Bitmap) {
//        //注册微博sdk
//        mAuthInfo = IMBaseDefine.AuthInfo(AppManager.instance.currentActivity(), IConstants.APP_KEY_SINA, IConstants.APP_REDIRECT_URL_SINA, IConstants.APP_SCOPE_SINA)
//        WbSdk.install(mActivity, mAuthInfo)
//        mWbShareHandler = WbShareHandler(mActivity)
//        mWbShareHandler.registerApp()
//
//        //分享内容
//        val weiboMultiMessage = WeiboMultiMessage()
//        //文本
//        val textObject = TextObject()
//        textObject.text = title + description
//        weiboMultiMessage.textObject = textObject
//        //图片
//        val imageObject = ImageObject()
//        imageObject.setImageObject(bitmap)
//        weiboMultiMessage.imageObject = imageObject
//        mWbShareHandler.shareMessage(weiboMultiMessage, false)
//    }
//
//    //失败
//    override fun onWbShareFail() {
//
//    }
//
//    //取消
//    override fun onWbShareCancel() {
//
//    }
//
//    //成功
//    override fun onWbShareSuccess() {
//
//    }
//}