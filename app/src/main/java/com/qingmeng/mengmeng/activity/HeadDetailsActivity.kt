package com.qingmeng.mengmeng.activity

/**
 * Created by fyf on 2019/1/5
 * 头报详情页
 */

import android.os.Build
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.view.dialog.ShareDialog
import kotlinx.android.synthetic.main.activity_head_details.*
import kotlinx.android.synthetic.main.activity_shop_detail.*
import kotlinx.android.synthetic.main.layout_head.*


class HeadDetailsActivity : BaseActivity() {
    private lateinit var mBottomDialog: ShareDialog
    private var url = String()
    override fun getLayoutId(): Int = R.layout.activity_head_details
    override fun initObject() {
        super.initObject()
        setHeadName(R.string.head_detail)
        //设置 分享背景 宽 高
        mMenu.setBackgroundResource(R.drawable.common_btn_back)
        mMenu.setBackgroundResource(R.drawable.icon_head_details_share)
//        val lp=mMenu.layoutParams
//        lp.width=100
//        mMenu.layoutParams=lp
        mMenu.width = 50
        mMenu.height = 50
        url = intent.getStringExtra("URL")
        initWebView()
        NewsWebView.loadUrl(url)
    }

    override fun initData() {
        super.initData()
    }

    override fun initListener() {
        super.initListener()
        mBack.setOnClickListener {
            onBackPressed()
        }
        mMenu.setOnClickListener {
            mBottomDialog = ShareDialog(this)
            mBottomDialog.show()
        }

    }

    private fun initWebView() {
        val mWebSettings = NewsWebView.settings
        NewsWebView.isVerticalScrollBarEnabled = false
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
        NewsWebView.webChromeClient = object : WebChromeClient() {
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
