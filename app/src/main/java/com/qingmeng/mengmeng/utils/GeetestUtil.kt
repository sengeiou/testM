package com.qingmeng.mengmeng.utils

import android.content.Context
import android.util.Log
import com.geetest.sdk.GT3ConfigBean
import com.geetest.sdk.GT3ErrorBean
import com.geetest.sdk.GT3GeetestUtils
import com.geetest.sdk.GT3Listener
import org.json.JSONObject

object GeetestUtil {
    private val TAG = "GeetestUtil"

    lateinit var gt3GeetestUtils: GT3GeetestUtils
    lateinit var gt3ConfigBean: GT3ConfigBean

    fun init(context: Context) {
        gt3GeetestUtils = GT3GeetestUtils(context)
    }

    fun customVerity(buttonCallBack: () -> Unit, callBack: (String) -> Unit) {
        gt3ConfigBean = GT3ConfigBean()
        // 设置验证模式，1：bind，2：unbind
        gt3ConfigBean.pattern = 1
        // 设置点击灰色区域是否消失，默认不消息
        gt3ConfigBean.isCanceledOnTouchOutside = false
        // 设置debug模式，开代理可调试 TODO 线上版本关闭
        gt3ConfigBean.isDebug = false
        // 设置语言，如果为null则使用系统默认语言
        gt3ConfigBean.lang = null
        // 设置webview加载超时
        gt3ConfigBean.timeout = 15000
        // 设置webview请求超时
        gt3ConfigBean.webviewTimeout = 10000
        gt3ConfigBean.listener = object : GT3Listener() {
            /**
             * api1结果回调
             * @param result
             */
            override fun onApi1Result(result: String) {
                Log.e(TAG, "GT3BaseListener-->onApi1Result-->$result")
            }

            /**
             * 验证码加载完成
             * @param duration 加载时间和版本等信息，为json格式
             */
            override fun onDialogReady(duration: String) {
                Log.e(TAG, "GT3BaseListener-->onDialogReady-->$duration")
            }

            /**
             * 验证结果
             * @param result
             */
            override fun onDialogResult(result: String) {
                Log.e(TAG, "GT3BaseListener-->onDialogResult-->$result")
                //回调发送短信验证码
                callBack(result)
            }

            /**
             * api2回调
             * @param result
             */
            override fun onApi2Result(result: String) {
                Log.e(TAG, "GT3BaseListener-->onApi2Result-->$result")
            }

            /**
             * 统计信息，参考接入文档
             * @param result
             */
            override fun onStatistics(result: String) {
                Log.e(TAG, "GT3BaseListener-->onStatistics-->$result")
            }

            /**
             * 验证码被关闭
             * @param num 1 点击验证码的关闭按钮来关闭验证码, 2 点击屏幕关闭验证码, 3 点击返回键关闭验证码
             */
            override fun onClosed(num: Int) {
                Log.e(TAG, "GT3BaseListener-->onClosed-->$num")
            }

            /**
             * 验证成功回调
             * @param result
             */
            override fun onSuccess(result: String) {
                Log.e(TAG, "GT3BaseListener-->onSuccess-->$result")
            }

            /**
             * 验证失败回调
             * @param errorBean 版本号，错误码，错误描述等信息
             */
            override fun onFailed(errorBean: GT3ErrorBean) {
                Log.e(TAG, "GT3BaseListener-->onFailed-->" + errorBean.toString())
            }

            /**
             * api1回调
             */
            override fun onButtonClick() {
                buttonCallBack()
            }
        }
        gt3GeetestUtils.init(gt3ConfigBean)
        gt3GeetestUtils.startCustomFlow()
    }

    fun showGeetest(params: JSONObject) {
        gt3ConfigBean.api1Json = params
        gt3GeetestUtils.getGeetest()
    }

    fun destroy() {
        gt3GeetestUtils.destory()
    }

    fun showSuccessDialog() {
        gt3GeetestUtils.showSuccessDialog()
    }

    fun showFailedDialog() {
        gt3GeetestUtils.showFailedDialog()
    }

    fun dismissGeetestDialog() {
        gt3GeetestUtils.dismissGeetestDialog()
    }
}