package com.qingmeng.mengmeng.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.utils.ApiUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_my_enterpriseentry.*
import kotlinx.android.synthetic.main.layout_head.*

/**
 *  Description :设置 - 企业入驻

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/3
 */
@SuppressLint("CheckResult")
class MyEnterpriseEntryActivity : BaseActivity() {
    private var mUrl = ""

    override fun getLayoutId(): Int {
        return R.layout.activity_my_enterpriseentry
    }

    override fun initObject() {
        super.initObject()

        setHeadName(R.string.my_enterpriseEntry)

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
        ApiUtils.getApi()
                .myEnterpriseEntry()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    it.apply {
                        if (code == 12000) {
                            mUrl = data.toString()
                            wvMyEnterPriseEntry.loadUrl(mUrl)
//                            wvMyEnterPriseEntry.loadUrl("http://47.99.139.155:11000/api/page_render/brand_detail_html?id=265")
                        } else {
                            myDialog.dismissLoadingDialog()
                        }
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                    llMyEnterpriseEntryTips.visibility = View.VISIBLE
                }, {}, { addSubscription(it) })
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
            domStorageEnabled = true
        }
        wvMyEnterPriseEntry.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                if (progress >= 100) {
                    myDialog.dismissLoadingDialog()
                }
            }
        }
        wvMyEnterPriseEntry.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url == null) return false

                try {
                    if (url.startsWith("openApp.jdMobile://")//京东
                            || url.startsWith("tmall://")//天猫
                            || url.startsWith("taobao://")//淘宝
                            || url.startsWith("tbopen://")//淘宝
                    ) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
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
                super.onReceivedError(view, errorCode, description, failingUrl)
                if (failingUrl == mUrl) {
                    llMyEnterpriseEntryTips.visibility = View.VISIBLE
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
    }
}