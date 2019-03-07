package com.qingmeng.mengmeng.activity

/**
 * Created by fyf on 2019/1/5
 * 头报详情页
 */

import android.annotation.SuppressLint
import android.os.Build
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.constant.IConstants.articleId
import com.qingmeng.mengmeng.entity.MicroBlog
import com.qingmeng.mengmeng.entity.Qq
import com.qingmeng.mengmeng.entity.WeChat
import com.qingmeng.mengmeng.entity.WeChatCircle
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.view.dialog.ShareDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_head_details.*
import kotlinx.android.synthetic.main.activity_shop_detail.*
import kotlinx.android.synthetic.main.layout_head.*


class HeadDetailsActivity : BaseActivity() {
    private lateinit var mBottomDialog: ShareDialog
    private var url = String()
    private var id = 0
    private lateinit var wxList: ArrayList<WeChat>
    private lateinit var monentsList: ArrayList<WeChatCircle>
    private lateinit var qqList: ArrayList<Qq>
    private lateinit var sinaList: ArrayList<MicroBlog>
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
        id = intent.getIntExtra(articleId, 1)
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
            //httpShareMessage("", 2,id)
            mBottomDialog = ShareDialog(this)
            mBottomDialog.show()
        }

    }

    @SuppressLint("CheckResult")
    private fun httpShareMessage(accessToken: String, type: Int, id: Int) {
        ApiUtils.getApi()
                .getShareMessage(accessToken, type, id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    if (bean.code == 12000) {
                        bean.data?.let {
                            if (!it.WeChat.isEmpty()) {
                                if (!wxList.isEmpty()) {
                                    wxList.clear()
                                }
                                wxList.addAll(it.WeChat)
                            }
                            if (!it.WeChatCircle.isEmpty()) {
                                if (!monentsList.isEmpty()) {
                                    monentsList.clear()
                                }
                                monentsList.addAll(it.WeChatCircle)
                            }
                            if (!it.qq.isEmpty()) {
                                if (!qqList.isEmpty()) {
                                    qqList.clear()
                                }
                                qqList.addAll(it.qq)
                            }
                            if (!it.microBlog.isEmpty()) {
                                if (!sinaList.isEmpty()) {
                                    sinaList.clear()
                                }
                                sinaList.addAll(it.microBlog)
                            }
                        }
                        //分享数据未传入
                        mBottomDialog = ShareDialog(this)
                        mBottomDialog.show()
                    }
                })
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
