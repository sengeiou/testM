package com.qingmeng.mengmeng.activity

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
                            wvMyEnterPriseEntry.loadUrl(data.toString())
                        } else {
                            myDialog.dismissLoadingDialog()
                        }
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                })
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