package com.qingmeng.mengmeng.activity

import android.os.Build
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import kotlinx.android.synthetic.main.activity_my_enterpriseentry.*
import kotlinx.android.synthetic.main.layout_head.*

/**
 * Created by mingyue
 * Date: 2019/2/15
 * mail: 153705849@qq.com
 * describe: 用户协议
 */
class LoginUserAgreementActivity : BaseActivity() {
    override fun getLayoutId(): Int {
        return R.layout.activity_my_enterpriseentry
    }
    override fun initObject() {
        super.initObject()

        setHeadName(R.string.user_agreement1)

        initWebView()
        httpLoad()
    }

    override fun initListener() {
        super.initListener()

        //返回
        mBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun httpLoad() {
        myDialog.showLoadingDialog()
        wvMyEnterPriseEntry.loadUrl("http://47.99.139.155:11000/api/page_render/enterprises_html")
    }

    private fun initWebView() {
        val mWebSettings = wvMyEnterPriseEntry.settings
        wvMyEnterPriseEntry.isVerticalScrollBarEnabled = false
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
        wvMyEnterPriseEntry.webChromeClient = object : WebChromeClient() {
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