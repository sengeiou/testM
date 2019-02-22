package com.qingmeng.mengmeng.activity

import android.os.Build
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.utils.ToastUtil
import kotlinx.android.synthetic.main.view_dialog_user_agreement.*


/**
 * Created by mingyue
 * Date: 2019/2/15
 * mail: 153705849@qq.com
 * describe: 用户协议
 */
class LoginUserAgreementActivity : BaseActivity() {

    override fun getLayoutId(): Int {
        return R.layout.view_dialog_user_agreement
    }

    override fun initObject() {
        super.initObject()
        setHeadName(R.string.user_agreement1)
        initWebView()
        httpLoad()
    }


    override fun initListener() {
        //同意按钮
        user_agreement_agree.setOnClickListener {
            setResult(RESULT_OK, intent.putExtra("agreement", 1))
   //         ToastUtil.showShort(R.string.user_agreement_yes)
            finish()
        }

    }

    //web链接
    private fun httpLoad() {
        myDialog.showLoadingDialog()
        user_agreement_webview.loadUrl("http://pubweb.oss-cn-hangzhou.aliyuncs.com/WebStatic%2Fmengmeng_test%2Fuser_agreement%2Fuser_agreement_Android.html")
    }

    //web配置
    private fun initWebView() {
        val mWebSettings = user_agreement_webview.settings
        user_agreement_webview.isVerticalScrollBarEnabled = false
        mWebSettings.apply {
            defaultTextEncodingName = "UTF-8"
            javaScriptEnabled = true
            saveFormData = false
            loadWithOverviewMode = true
            setSupportZoom(true)
            cacheMode = WebSettings.LOAD_DEFAULT
            useWideViewPort = true
            builtInZoomControls = false
            layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
            javaScriptCanOpenWindowsAutomatically = true
        }
        user_agreement_webview.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                if (progress >= 100) {
                    myDialog.dismissLoadingDialog()
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
    }

}