package com.qingmeng.mengmeng.wxapi


import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.activity.LoginBindingPhoneActivity
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.startActivity

/**
 * Created by mingyue
 * Date: 2019/1/16
 * mail: 153705849@qq.com
 * describe:
 */
class WXEntryActivity : Activity(), IWXAPIEventHandler {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //如果没回调onResp，八成是这句没有写

        MainApplication().mWxApi.handleIntent(intent, this)
    }

    // 微信发送请求到第三方应用时，会回调到该方法
    override fun onReq(req: BaseReq) {}

    // 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
    //app发送消息给微信，处理返回消息的回调
    override fun onResp(resp: BaseResp) {
        Log.d("", resp.errStr)
        Log.d("", "错误码 : " + resp.errCode + "")
        when (resp.errCode) {

            BaseResp.ErrCode.ERR_AUTH_DENIED, BaseResp.ErrCode.ERR_USER_CANCEL -> if (RETURN_MSG_TYPE_SHARE == resp.type)
                ToastUtil.showShort("分享失败")
            else
                ToastUtil.showShort("登录失败")
            BaseResp.ErrCode.ERR_OK -> when (resp.type) {
                RETURN_MSG_TYPE_LOGIN -> {
                    //拿到了微信返回的code,立马再去请求access_token
                    val code = (resp as SendAuth.Resp).code
                    var openid = resp.openId
                    Log.d("", "code = " + code)

                    //就在这个地方，用网络库什么的或者自己封的网络api，发请求去咯，注意是get请求
                    ApiUtils.getApi().thirdlogin(openid, 2)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ bean ->
                                if (bean.code == 12000) {
                                    bean.data?.let {
                                        MainApplication.instance.user = it
                                        MainApplication.instance.TOKEN = it.token
                                        it.upDate()
                                    }
                                    if (bean.code == 25093) {
                                        ToastUtil.showShort(bean.msg)
                                        startActivity<LoginBindingPhoneActivity>()
                                    }
                                } else {
                                    ToastUtil.showShort(bean.msg)
                                }
                            })
                }

                RETURN_MSG_TYPE_SHARE -> {
                    ToastUtil.showShort("微信分享成功")
                    finish()
                }
            }
        }
    }

    companion object {
        private val RETURN_MSG_TYPE_LOGIN = 1
        private val RETURN_MSG_TYPE_SHARE = 2
    }
}
