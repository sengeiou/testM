package com.qingmeng.mengmeng.activity

import com.app.common.extensions.isConnected
import com.app.common.utils.WebViewUtil
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import kotlinx.android.synthetic.main.activity_privacyprotocal.*


/**
 * describe: 隐私声明
 */
class PrivacyProtocolWebViewActivity : BaseActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_privacyprotocal
    }

    override fun initObject() {
        super.initObject()
        setHeadName(R.string.privacy_protocol)
        initWebView()
    }


    override fun initListener() {
        btnAgree.setOnClickListener {
            finish()
        }
    }

    //web配置
    private fun initWebView() {
        WebViewUtil.initWeb(application, webview, true)
        if (application.isConnected()) {
            webview.loadUrl("https://wangru.oss-cn-qingdao.aliyuncs.com/web/cprivacy-policy.html")
        } else {
            webview.loadUrl("file:///android_asset/web/privacy/privacy_protocol.html")
        }
    }

}