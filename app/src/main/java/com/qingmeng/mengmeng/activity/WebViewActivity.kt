package com.qingmeng.mengmeng.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
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
        if (TextUtils.isEmpty(detailUrl)) {
            mNoNet.text = getString(R.string.url_error)
            mNoNet.visibility = View.VISIBLE
        } else {
            initWebView()
        }
    }

    private fun initWebView() {
        var mWebSettings = mWebView.settings
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
            domStorageEnabled = true
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
//                    title?.let { setHeadName(it) }
                }
            }
        }
        mWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url == null) return false

                try {
                    if (url.startsWith("openApp.jdMobile://")//京东
                            || url.startsWith("tmall://")//天猫
                            || url.startsWith("taobao://")//淘宝
                            || url.startsWith("tbopen://")//淘宝
                    ) {
                        val intent = Intent(ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                        return true
                    }//其他自定义的scheme
                } catch (e: Exception) { //防止crash (如果手机上没有安装处理某个scheme开头的url的APP, 会导致crash)
                    return true//没有安装该app时，返回true，表示拦截自定义链接，但不跳转，避免弹出上面的错误页面
                }

                //处理http和https开头的url
                view.loadUrl(url)
                return true
            }

            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
//                super.onReceivedError(view, errorCode, description, failingUrl)
                if (failingUrl == detailUrl) {
                    mWebView.visibility = View.GONE
                    mNoNet.visibility = View.VISIBLE
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        mWebView.loadUrl(detailUrl)
    }
}