package com.qingmeng.mengmeng.activity


import android.annotation.SuppressLint
import android.os.Build
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import com.bumptech.glide.Glide
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.constant.IConstants.articleId
import com.qingmeng.mengmeng.entity.MicroBlog
import com.qingmeng.mengmeng.entity.Qq
import com.qingmeng.mengmeng.entity.WeChat
import com.qingmeng.mengmeng.entity.WeChatCircle
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.loginshare.ShareQQManager
import com.qingmeng.mengmeng.utils.loginshare.ShareWechatManager
import com.qingmeng.mengmeng.utils.loginshare.ShareWeiboManager
import com.qingmeng.mengmeng.utils.setDrawableLeft
import com.qingmeng.mengmeng.view.dialog.ShareDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_head_details.*
import kotlinx.android.synthetic.main.layout_head.*
import org.jetbrains.anko.async

/**
 * Created by fyf on 2019/1/5
 * 头报详情页
 */
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
        //设置 分享按钮
        mMenu.setDrawableLeft(R.drawable.icon_head_details_share)
        url = intent.getStringExtra("URL")
        id = intent.getIntExtra(articleId, 1)
        initWebView()
        NewsWebView.loadUrl(url)
    }

    override fun initListener() {
        super.initListener()
        mBack.setOnClickListener {
            onBackPressed()
        }
        mMenu.setOnClickListener {
            //  httpShareMessage(2, id)
            //   mBottomDialog = ShareDialog(this,wxList,monentsList,qqList,sinaList)
            mBottomDialog = ShareDialog(this,{
                async {
                    val bitmap = Glide.with(this@HeadDetailsActivity)
                            .asBitmap()
                            .load("http://pic1.nipic.com/2008-12-30/200812308231244_2.jpg")
                            .submit(50, 50).get()
                    ShareWechatManager.shareToWechat(0, "测试链接", "测试标题", "内容测试", bitmap)
                }
            }, {
                async {
                    val bitmap = Glide.with(this@HeadDetailsActivity)
                            .asBitmap()
                            .load("http://pic1.nipic.com/2008-12-30/200812308231244_2.jpg")
                            .submit(50, 50).get()
                    ShareWechatManager.shareToWechat(1, "测试链接", "测试标题", "内容测试", bitmap)
                }
            }, {
                ShareQQManager.shareToQQ(this@HeadDetailsActivity, "测试链接", "测试标题", "内容测试", "http://pic1.nipic.com/2008-12-30/200812308231244_2.jpg")
            }, {
                async {
                    val bitmap = Glide.with(this@HeadDetailsActivity)
                            .asBitmap()
                            .load("http://pic1.nipic.com/2008-12-30/200812308231244_2.jpg")
                            .submit(50, 50).get()
                    ShareWeiboManager.shareToWeibo(this@HeadDetailsActivity, "测试标题", "内容测试", bitmap)
                }
            })
            mBottomDialog.show()
        }

    }

    @SuppressLint("CheckResult")
    private fun httpShareMessage(type: Int, id: Int) {
        ApiUtils.getApi()
                .getShareMessage(MainApplication.instance.TOKEN, type, id)
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
                        //  mBottomDialog = ShareDialog(this, wxList, monentsList, qqList, sinaList)
                        //  mBottomDialog = ShareDialog(this, wxList)
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
