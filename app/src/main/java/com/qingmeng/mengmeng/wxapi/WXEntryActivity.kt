package com.qingmeng.mengmeng.wxapi


import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import com.qingmeng.mengmeng.activity.LoginMainActivity
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.entity.WxBean
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import de.greenrobot.event.EventBus

/**
 * Created by mingyue
 * Date: 2019/1/16
 * mail: 153705849@qq.com
 * describe:
 */
class WXEntryActivity : AppCompatActivity(), IWXAPIEventHandler {
    private lateinit var msgApi: IWXAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        msgApi = WXAPIFactory.createWXAPI(this, IConstants.APPID_WECHAT)
        msgApi.registerApp(IConstants.APPID_WECHAT)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        //如果没回调onResp，八成是这句没有写
        msgApi.handleIntent(intent, this)
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    // 微信发送请求到第三方应用时，会回调到该方法
    override fun onReq(req: BaseReq) {}

    // 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
    //app发送消息给微信，处理返回消息的回调
    override fun onResp(resp: BaseResp?) {
        resp?.apply {
            when (errCode) {
                BaseResp.ErrCode.ERR_OK -> {
                    if (ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX != type) {
                        val code = (resp as SendAuth.Resp).code
                        EventBus.getDefault().post(WxBean(code))
                    }
                }
                BaseResp.ErrCode.ERR_USER_CANCEL -> {
                }
                BaseResp.ErrCode.ERR_AUTH_DENIED -> {
                }
                else -> {
                }
            }
        }
        finish()
    }
}
