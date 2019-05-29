package com.qingmeng.mengmeng.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.bumptech.glide.Glide
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.entity.ShareBean
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.utils.loginshare.ShareQQManager
import com.qingmeng.mengmeng.utils.loginshare.ShareWechatManager
import com.qingmeng.mengmeng.utils.loginshare.ShareWeiboManager
import com.qingmeng.mengmeng.utils.setDrawableLeft
import com.qingmeng.mengmeng.view.dialog.ShareDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_web.*
import kotlinx.android.synthetic.main.layout_head.*
import org.jetbrains.anko.async
import org.jetbrains.anko.startActivity


@SuppressLint("SetJavaScriptEnabled")
@Suppress("DEPRECATION", "OverridingDeprecatedMember")
class HeadDetailsActivity : BaseActivity() {
    private var id = 0
    private var url = ""
    private lateinit var mShareDialog: ShareDialog
    private var mShareBean = ShareBean()

    override fun getLayoutId(): Int = R.layout.activity_web

    override fun initObject() {
        url = intent.getStringExtra("URL") ?: ""
        id = intent.getIntExtra(IConstants.articleId,0)
        setHeadName(R.string.head_detail)
        mMenu.setDrawableLeft(R.drawable.icon_head_details_share)
        if (TextUtils.isEmpty(url)) {
            mNoNet.text = getString(R.string.url_error)
            mNoNet.visibility = View.VISIBLE
        } else {
            initWebView()
        }
    }

    override fun initListener() {
        super.initListener()
        //返回
        mBack.setOnClickListener {
            onBackPressed()
        }

        //分享
        mMenu.setOnClickListener {
            if (TextUtils.isEmpty(MainApplication.instance.TOKEN)) {
                startActivity<LoginMainActivity>(IConstants.FROM_TYPE to 1)
                ToastUtil.showShort(getString(R.string.pls_login))
            } else {
                mShareDialog = ShareDialog(this, {
                    async {
                        val bitmap = if (mShareBean.WeChat.icon.isNullOrBlank()) BitmapFactory.decodeResource(resources, R.mipmap.my_settings_aboutus_icon) else Glide.with(this@HeadDetailsActivity)
                                .asBitmap()
                                .load(mShareBean.WeChat.icon)
                                .submit(50, 50).get()
                        ShareWechatManager.shareToWechat(0, mShareBean.WeChat.url, mShareBean.WeChat.title, mShareBean.WeChat.content, bitmap)
                    }
                }, {
                    async {
                        val bitmap = if (mShareBean.WeChatCircle.icon.isNullOrBlank()) BitmapFactory.decodeResource(resources, R.mipmap.my_settings_aboutus_icon) else Glide.with(this@HeadDetailsActivity)
                                .asBitmap()
                                .load(mShareBean.WeChatCircle.icon)
                                .submit(50, 50).get()
                        ShareWechatManager.shareToWechat(1, mShareBean.WeChatCircle.url, mShareBean.WeChatCircle.title, mShareBean.WeChatCircle.content, bitmap)
                    }
                }, {
                    ShareQQManager.shareToQQ(this@HeadDetailsActivity, mShareBean.qq.url, mShareBean.qq.title, mShareBean.qq.content, mShareBean.qq.icon)
                }, {
                    async {
                        val bitmap = if (mShareBean.microBlog.icon.isNullOrBlank()) BitmapFactory.decodeResource(resources, R.mipmap.my_settings_aboutus_icon) else Glide.with(this@HeadDetailsActivity)
                                .asBitmap()
                                .load(mShareBean.microBlog.icon)
                                .submit(50, 50).get()
                        ShareWeiboManager.shareToWeibo(this@HeadDetailsActivity, mShareBean.microBlog.url, mShareBean.microBlog.title, mShareBean.microBlog.content, bitmap)
                    }
                })
                if (mShareBean.qq.title.isEmpty()) {
                    httpShareMessage(2, id)
                } else {
                    mShareDialog.show()
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun httpShareMessage(type: Int, id: Int) {
        myDialog.showLoadingDialog()
        ApiUtils.getApi()
                .getShareMessage(MainApplication.instance.TOKEN, type, id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    myDialog.dismissLoadingDialog()
                    if (bean.code == 12000) {
                        bean.data?.let {
                            mShareBean = it
                            mShareDialog.show()
                        }
                    } else {
                        ToastUtil.showShort(bean.msg)
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                }, {}, { addSubscription(it) })
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
            domStorageEnabled = true
        }
        myDialog.showLoadingDialog()
        mWebView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                if (progress >= 100) {
                    myDialog.dismissLoadingDialog()
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
                if (failingUrl == url) {
                    mWebView.visibility = View.GONE
                    mNoNet.visibility = View.VISIBLE
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        mWebView.loadUrl(url)
    }
}