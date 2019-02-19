package com.qingmeng.mengmeng.activity

import AppManager
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Button
import android.widget.Toast
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.view.dialog.DialogCustom


/**
 * Created by mingyue
 * Date: 2019/2/15
 * mail: 153705849@qq.com
 * describe: 用户协议
 */
class LoginUserAgreementActivity : Activity() {
    //不能继承基类，AppTheme有冲突
    protected lateinit var myDialog: DialogCustom
    lateinit var muser_agreement_webview: WebView
    lateinit var muser_agreement_agree: Button
        lateinit var muser_agreement_agree_not: Button
    //同意
    var boolAgreement = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_dialog_user_agreement)
        muser_agreement_webview = findViewById(R.id.user_agreement_webview)
        muser_agreement_agree = findViewById(R.id.user_agreement_agree)
        muser_agreement_agree_not=findViewById(R.id.user_agreement_agree_not)
        myDialog = DialogCustom(AppManager.instance.currentActivity())
        initWebView()
        httpLoad()
        initListener()
    }

    fun initListener() {
        //同意按钮
        muser_agreement_agree.setOnClickListener {
            boolAgreement = true
            IConstants.USER_AGREEMENT = boolAgreement
            Toast.makeText(this,R.string.user_agreement_yes,Toast.LENGTH_LONG)
            finish()
        }
        muser_agreement_agree_not.setOnClickListener{
            Toast.makeText(this,R.string.user_agreement_no,Toast.LENGTH_LONG)
            finish()
        }
    }

    //弹窗设置
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val view = window.decorView
        val lp = view.layoutParams as WindowManager.LayoutParams
        lp.gravity = Gravity.CENTER
        lp.width = 800
        lp.height = 2000
        windowManager.updateViewLayout(view, lp)
    }

    //web链接
    private fun httpLoad() {
        myDialog.showLoadingDialog()
        muser_agreement_webview.loadUrl("http://pubweb.oss-cn-hangzhou.aliyuncs.com/WebStatic%2Fmengmeng_test%2Fuser_agreement%2Fuser_agreement_Android.html")
    }

    //web配置
    private fun initWebView() {
        val mWebSettings = muser_agreement_webview.settings
        muser_agreement_webview.isVerticalScrollBarEnabled = false
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
        muser_agreement_webview.webChromeClient = object : WebChromeClient() {
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

    override fun onDestroy() {
        super.onDestroy()
        myDialog.unBindContext()
    }
}