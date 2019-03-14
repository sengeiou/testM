package com.qingmeng.mengmeng.utils.loginshare

import AppManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.utils.ToastUtil
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory

/**
 * Created by wr
 * Date: 2018/8/28  21:17
 * describe:
 */
object ShareWechatManager {
    val api: IWXAPI by lazy { WXAPIFactory.createWXAPI(MainApplication.instance, IConstants.APPID_WECHAT, true) }

    /**
     * @param flag        0 微信好友 1 微信朋友圈
     * @param url         点击跳转链接
     * @param title       分享的标题
     * @param description 分享的具体内容
     * @param thumb       分享时显示的图片（R.drawable.icon_logo）
     */
    fun shareToWechat(flag: Int, url: String, title: String, description: String,
                      thumb: Bitmap = BitmapFactory.decodeResource(AppManager.instance.currentActivity().resources, R.drawable.login_logo)) {
        var content = description
        api.registerApp(IConstants.APPID_WECHAT)
        if (flag != 0 && flag != 1) {
            return
        }

        if (!api.isWXAppInstalled) {
            ToastUtil.showShort("未安装微信客户端")
            return
        }

        val webpage = WXWebpageObject()
        webpage.webpageUrl = url
        val msg = WXMediaMessage(webpage)
        msg.title = title
        if (content.length > 32) {
            content = content.substring(0, 30) + "..."
        }
        msg.description = content
        // 这里替换一张自己工程里的图片资源
        msg.setThumbImage(thumb)
        val req = SendMessageToWX.Req()
        req.transaction = System.currentTimeMillis().toString()
        req.message = msg
        req.scene = if (flag == 0) SendMessageToWX.Req.WXSceneSession else SendMessageToWX.Req.WXSceneTimeline
        api.sendReq(req)
    }
}
