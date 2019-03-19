package com.qingmeng.mengmeng.activity

import android.annotation.SuppressLint
import android.os.Build
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
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
//                            wvMyEnterPriseEntry.loadUrl(data.toString())
                            wvMyEnterPriseEntry.loadUrl("http://47.99.139.155:11000/api/page_render/brand_detail_html?id=265")
                        } else {
                            myDialog.dismissLoadingDialog()
                        }
                    }
                }, {
                    myDialog.dismissLoadingDialog()
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
        }
        wvMyEnterPriseEntry.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                if (progress >= 100) {
                    myDialog.dismissLoadingDialog()
                }
            }
        }
//        wvMyEnterPriseEntry.webViewClient = object : WebViewClient(){
//            override fun onPageFinished(view: WebView?, url: String?) {
//                super.onPageFinished(view, url)
//
//                //这个是一定要加上那个的,配合scrollView和WebView的height=wrap_content属性使用
//                val w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
//                val h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
//                //重新测量
//                wvMyEnterPriseEntry.measure(w, h)
//            }
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
    }
}