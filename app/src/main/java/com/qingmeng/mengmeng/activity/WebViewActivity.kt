package com.qingmeng.mengmeng.activity

import android.annotation.SuppressLint
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.constant.IConstants
import kotlinx.android.synthetic.main.activity_web.*

@SuppressLint("SetJavaScriptEnabled")
@Suppress("DEPRECATION", "OverridingDeprecatedMember")
class WebViewActivity : BaseActivity() {
    private var mTitle = ""
    private var detailUrl = ""
    override fun getLayoutId(): Int = R.layout.activity_web

    override fun initObject() {
        detailUrl = intent.getStringExtra(IConstants.detailUrl) ?: ""
        mTitle = intent.getStringExtra(IConstants.title) ?: ""
        if (!TextUtils.isEmpty(mTitle)) {
            setHeadName(mTitle)
        }
        initWebView()
    }

    private fun initWebView() {
        val mWebSettings = mWebView.settings
        mWebView.isVerticalScrollBarEnabled = false
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
        myDialog.showLoadingDialog()
        mWebView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                if (progress >= 100) {
                    myDialog.dismissLoadingDialog()
                }
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                if (TextUtils.isEmpty(mTitle)) {
                    title?.let { setHeadName(it) }
                }
            }
        }
        mWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                mWebView.visibility = View.GONE
                mNoNet.visibility = View.VISIBLE
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        mWebView.loadUrl(detailUrl)
    }
}