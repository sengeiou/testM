package com.qingmeng.mengmeng.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ImageSpan
import android.view.View
import android.view.WindowManager
import android.webkit.*
import cn.jzvd.Jzvd
import cn.jzvd.JzvdStd
import com.bumptech.glide.Glide
import com.lemo.emojcenter.FaceInitData
import com.mogujie.tt.config.IntentConstant
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.ShopDetailVpAdapter
import com.qingmeng.mengmeng.constant.IConstants.BRANDID
import com.qingmeng.mengmeng.constant.IConstants.ENTER_BRAND_NUM
import com.qingmeng.mengmeng.constant.IConstants.FROM_TYPE
import com.qingmeng.mengmeng.constant.IConstants.IMGS
import com.qingmeng.mengmeng.constant.IConstants.MESSAGE_BACK_BRAND_ID
import com.qingmeng.mengmeng.constant.IConstants.MYFRAGMENT_TO_MESSAGE
import com.qingmeng.mengmeng.constant.IConstants.POSITION
import com.qingmeng.mengmeng.constant.IConstants.TO_MESSAGE
import com.qingmeng.mengmeng.entity.*
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.utils.loginshare.ShareQQManager
import com.qingmeng.mengmeng.utils.loginshare.ShareWechatManager
import com.qingmeng.mengmeng.utils.loginshare.ShareWeiboManager
import com.qingmeng.mengmeng.utils.setDrawableTop
import com.qingmeng.mengmeng.view.dialog.ShareDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_shop_detail.*
import org.jetbrains.anko.*
import org.jetbrains.anko.internals.AnkoInternals

@Suppress("DEPRECATION")
@SuppressLint("CheckResult", "StringFormatMatches", "SetTextI18n", "SetJavaScriptEnabled")
class ShopDetailActivity : BaseActivity() {
    private lateinit var vpAdapter: ShopDetailVpAdapter
    private val mImgList = ArrayList<ShopDetailImg>()
    private val mJoinSupport = ArrayList<String>()
    private var brandInformation: BrandInformation? = null
    private var brandInitialFee: BrandInitialFee? = null
    private lateinit var mShareDialog: ShareDialog
    private var mShareBean = ShareBean()
    private var brandBean: BrandBean? = null
    private var name = ""
    private var id = 0
    private var totalImg = 0
    private var hasVideo = false
    private var isAttention = 0
    private var webUrl = ""

    override fun getLayoutId(): Int = R.layout.activity_shop_detail

    override fun initObject() {
        //进一次详情就加一次
        ENTER_BRAND_NUM += 1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.transparent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
        id = intent.getIntExtra(BRANDID, 0)
        vpAdapter = ShopDetailVpAdapter(this, mImgList) {
            val bundle = Bundle()
            bundle.putInt(POSITION, it)
            bundle.putSerializable(IMGS, mImgList)
            startActivityForResult(Intent(this, VideoDetailActivity::class.java).putExtras(bundle), 122)
        }
        mDetailVp.adapter = vpAdapter
        mDetailVp.offscreenPageLimit = 6
        val lp = mDetailVp.layoutParams
        lp.height = resources.displayMetrics.widthPixels
        initWebView()
    }

    override fun initData() {
        ApiUtils.getApi().getBrandDetail(MainApplication.instance.TOKEN, id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    if (bean.code == 12000) {
                        bean.data?.let {
                            setData(it)
                        }
                    } else {
                        ToastUtil.showShort(bean.msg)
                    }
                }, {
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

    //获取分享信息
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

    private fun setData(bean: BrandBean) {
        //品牌被下架或特殊处理了，不做展示
        if (bean.isStand || bean.brandIsShow == 0) {
            mGoodsUndercarriage.visibility = View.VISIBLE
            mGoodsUndercarriageText.visibility = View.VISIBLE
            Handler().postDelayed({ finish() }, 2000)
        } else {
            mGoodsUndercarriage.visibility = View.GONE
            mGoodsUndercarriageText.visibility = View.GONE
            brandBean = bean
            name = bean.name
            val density = resources.displayMetrics.density
            if (bean.status == 1) {
                mDetailName.setPadding(0, 0, 0, 0)
                val spanString = SpannableString("证 $name")
                val drawable = resources.getDrawable(R.drawable.detail_icon_certification)
                val imageSpan = ImageSpan(drawable, ImageSpan.ALIGN_BASELINE)
                spanString.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                drawable.setBounds(0, (7 * density).toInt(), (14 * density).toInt(), (21 * density).toInt())
                mDetailName.text = spanString
            } else {
                mDetailName.setPadding(0, (5 * density).toInt(), 0, 0)
                mDetailName.text = name
            }
            mDetailType.text = bean.foodName
            mDetailMoney.text = bean.capitalName
            mDetailJoinCount.text = getString(R.string.join_count, bean.joinStoreNumStr)
            mDetailDirectCount.text = getString(R.string.direct_count, bean.directStoreNumStr)
            if (!mJoinSupport.isEmpty()) {
                mJoinSupport.clear()
            }
            bean.affiliateSupport.apply {
                trainContent?.let { mJoinSupport.addAll(it) }
                operateSupport?.let { mJoinSupport.addAll(it) }
                operationalSupervision?.let { mJoinSupport.addAll(it) }
                if (!TextUtils.isEmpty(locationName)) mJoinSupport.add(locationName)
                if (!TextUtils.isEmpty(decorationName)) mJoinSupport.add(decorationName)
                if (!TextUtils.isEmpty(trainingMethodName)) mJoinSupport.add(trainingMethodName)
            }
            brandInformation = bean.brandInformation
            brandInitialFee = bean.brandInitialFee
            isAttention = bean.isAttention
            if (bean.isAttention == 0) {
                mCollection.setDrawableTop(R.drawable.detail_icon_collection)
                mCollection.setText(R.string.attention)
            } else {
                mCollection.setDrawableTop(R.drawable.detail_icon_collected)
                mCollection.setText(R.string.already_attention)
            }
            if (!mImgList.isEmpty()) {
                mImgList.clear()
            }
            if (!TextUtils.isEmpty(bean.video)) {
                mImgList.add(ShopDetailImg(bean.video, true))
            }
            hasVideo = !TextUtils.isEmpty(bean.video)
            bean.planImage.forEach { mImgList.add(ShopDetailImg(it, false)) }
            totalImg = bean.planImage.size
            if (hasVideo) {
                mImgCount.visibility = View.GONE
                mImgCount.text = "0/$totalImg"
            } else {
                mImgCount.visibility = View.VISIBLE
                mImgCount.text = "1/$totalImg"
            }
            vpAdapter.notifyDataSetChanged()
            mDetailWeb.loadUrl(bean.brandHtmlUrl)
            webUrl = bean.brandHtmlUrl
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun initListener() {
        mDetailBack.setOnClickListener { onBackPressed() }
        mDetailMore.setOnClickListener {
            myDialog.showMorePop(it, {
                toNextResult<MyMessageActivity>(TO_MESSAGE)
                if (!MYFRAGMENT_TO_MESSAGE) {
                    //如果聊天列表页面存在了 就销毁它
                    finishAty(MyMessageActivity::class.java)
                }
            }, {
                startActivity(intentFor<MainActivity>().newTask().clearTask())
            }, {
                toNext<JoinFeedbackActivity>(BRANDID to id)
            }, {
                if (TextUtils.isEmpty(MainApplication.instance.TOKEN)) {
                    startActivity<LoginMainActivity>(FROM_TYPE to 1)
                    ToastUtil.showShort(getString(R.string.pls_login))
                } else {
                    mShareDialog = ShareDialog(this, {
                        async {
                            val bitmap = if (mShareBean.WeChat.icon.isNullOrBlank()) BitmapFactory.decodeResource(resources, R.mipmap.my_settings_aboutus_icon) else Glide.with(this@ShopDetailActivity)
                                    .asBitmap()
                                    .load(mShareBean.WeChat.icon)
                                    .submit(50, 50).get()
                            ShareWechatManager.shareToWechat(0, mShareBean.WeChat.url, mShareBean.WeChat.title, mShareBean.WeChat.content, bitmap)
                        }
                    }, {
                        async {
                            val bitmap = if (mShareBean.WeChatCircle.icon.isNullOrBlank()) BitmapFactory.decodeResource(resources, R.mipmap.my_settings_aboutus_icon) else Glide.with(this@ShopDetailActivity)
                                    .asBitmap()
                                    .load(mShareBean.WeChatCircle.icon)
                                    .submit(50, 50).get()
                            ShareWechatManager.shareToWechat(1, mShareBean.WeChatCircle.url, mShareBean.WeChatCircle.title, mShareBean.WeChatCircle.content, bitmap)
                        }
                    }, {
                        ShareQQManager.shareToQQ(this@ShopDetailActivity, mShareBean.qq.url, mShareBean.qq.title, mShareBean.qq.content, mShareBean.qq.icon)
                    }, {
                        async {
                            val bitmap = if (mShareBean.microBlog.icon.isNullOrBlank()) BitmapFactory.decodeResource(resources, R.mipmap.my_settings_aboutus_icon) else Glide.with(this@ShopDetailActivity)
                                    .asBitmap()
                                    .load(mShareBean.microBlog.icon)
                                    .submit(50, 50).get()
                            ShareWeiboManager.shareToWeibo(this@ShopDetailActivity, mShareBean.microBlog.url, mShareBean.microBlog.title, mShareBean.microBlog.content, bitmap)
                        }
                    })
                    if (mShareBean.qq.title.isEmpty()) {
                        httpShareMessage(1, id)
                    } else {
                        mShareDialog.show()
                    }
                }
            })
        }
        mDetailJoinSupport.setOnClickListener {
            myDialog.showBrandDialog(mJoinSupport)
        }
        mDetailBrandInformation.setOnClickListener {
            brandInformation?.let { myDialog.showBrandDialog(it) }
        }
        mDetailJoinMoney.setOnClickListener {
            brandInitialFee?.let { myDialog.showBrandDialog(it) }
        }
        mCustomerService.setOnClickListener {
            brandBean?.let {
                FaceInitData.init(applicationContext)
                FaceInitData.setAlias("${MainApplication.instance.user.wxUid}")
                toNextResult<MyMessageChatActivity>(TO_MESSAGE, IntentConstant.KEY_SESSION_KEY to "1_${it.wxServiceId}", "title" to it.nickname, "bundle" to Bundle().apply {
                    putInt("id", id)
                    putString("logo", it.logo)
                    putString("name", it.name)
                    putString("capitalName", it.capitalName)
                    putString("avatar", it.avatar)
                })
                //如果聊天页面存在了 就销毁它
                finishAty(MyMessageChatActivity::class.java)
                //如果是从我的板块进去的，就直接关闭此页面
                if (MYFRAGMENT_TO_MESSAGE) {
                    onBackPressed()
                }
            }
        }
        mCollection.setOnClickListener {
            if (TextUtils.isEmpty(MainApplication.instance.TOKEN)) {
                startActivity<LoginMainActivity>(FROM_TYPE to 1)
                ToastUtil.showShort(getString(R.string.pls_login))
                return@setOnClickListener
            }
            if (isAttention == 0) addAttention() else unAttention()
        }
        mDetailJoin.setOnClickListener { _ ->
            brandBean?.let {
                FaceInitData.init(applicationContext)
                FaceInitData.setAlias("${MainApplication.instance.user.wxUid}")
                toNextResult<MyMessageChatActivity>(TO_MESSAGE, IntentConstant.KEY_SESSION_KEY to "1_${it.wxServiceId}", "title" to it.nickname, "bundle" to Bundle().apply {
                    putInt("id", id)
                    putString("logo", it.logo)
                    putString("name", it.name)
                    putString("capitalName", it.capitalName)
                    putString("avatar", it.avatar)
                })
            }
            //如果聊天页面存在了 就销毁它
            finishAty(MyMessageChatActivity::class.java)
            //如果是从我的板块进去的，就直接关闭此页面
            if (MYFRAGMENT_TO_MESSAGE) {
                onBackPressed()
            }
        }
        mGetJoinData.setOnClickListener { _ ->
            myDialog.showJoinDataDialog(name) { name, phone, message, dialog ->
                ApiUtils.join(id, name, phone, message, 0, myDialog, { dialog.cancel() }, { addSubscription(it) })
            }
        }
        mDetailScroll.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            val totalScroll = mDetailVp.height
            val maskAlpha = 1 - scrollY * 2f / totalScroll
            mDetailTopBg.alpha = scrollY * 1f / totalScroll
            if (maskAlpha > 0) {
                mDetailBackMask.alpha = maskAlpha
                mDetailMoreMask.alpha = maskAlpha
                mDetailBack.alpha = 0f
                mDetailMore.alpha = 0f
            } else {
                mDetailBackMask.alpha = 0f
                mDetailMoreMask.alpha = 0f
                if (Math.abs(maskAlpha) <= 1) {
                    mDetailBack.alpha = Math.abs(maskAlpha)
                    mDetailMore.alpha = Math.abs(maskAlpha)
                } else {
                    mDetailBack.alpha = 1f
                    mDetailMore.alpha = 1f
                }
            }
        }
        mDetailVp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                if (mImgList[position].isVideo) {
                    mImgCount.visibility = View.GONE
                    JzvdStd.goOnPlayOnResume()
                } else {
                    mImgCount.visibility = View.VISIBLE
                    JzvdStd.goOnPlayOnPause()
                    mImgCount.text = if (hasVideo) "$position/$totalImg" else "${position + 1}/$totalImg"
                }
            }

        })
    }

    //收藏
    private fun addAttention() {
        myDialog.showLoadingDialog()
        ApiUtils.getApi().addAttention(MainApplication.instance.TOKEN, id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    myDialog.dismissLoadingDialog()
                    if (bean.code == 12000) {
                        isAttention = 1
                        mCollection.setDrawableTop(R.drawable.detail_icon_collected)
                        mCollection.setText(R.string.already_attention)
                    } else {
                        ToastUtil.showShort(bean.msg)
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

    //取消收藏
    private fun unAttention() {
        myDialog.showLoadingDialog()
        ApiUtils.getApi().deleteMyFollow(id, MainApplication.instance.TOKEN)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    myDialog.dismissLoadingDialog()
                    if (bean.code == 12000) {
                        isAttention = 0
                        mCollection.setDrawableTop(R.drawable.detail_icon_collection)
                        mCollection.setText(R.string.attention)
                    } else {
                        ToastUtil.showShort(bean.msg)
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

    private inline fun <reified T : Activity> toNext(vararg params: Pair<String, Any>) {
        if (TextUtils.isEmpty(MainApplication.instance.TOKEN)) {
            startActivity<LoginMainActivity>(FROM_TYPE to 1)
            ToastUtil.showShort(getString(R.string.pls_login))
            return
        }
        AnkoInternals.internalStartActivity(this, T::class.java, params)
    }

    private inline fun <reified T : Activity> toNextResult(requestCode: Int, vararg params: Pair<String, Any>) {
        if (TextUtils.isEmpty(MainApplication.instance.TOKEN)) {
            startActivity<LoginMainActivity>(FROM_TYPE to 1)
            ToastUtil.showShort(getString(R.string.pls_login))
            return
        }
        //判断下当前id在不在BRAND_TO_MESSAGE内，如果在 就删掉它（防止返回会直接finish掉）
        if (MESSAGE_BACK_BRAND_ID.contains("$id")) {
            MESSAGE_BACK_BRAND_ID = MESSAGE_BACK_BRAND_ID.replace("$id", "")
        }
        AnkoInternals.internalStartActivityForResult(this, T::class.java, requestCode, params)
    }

    //如果页面存在了 就销毁它
    private fun finishAty(activity: Class<*>) {
        when (activity) {
            MyMessageActivity::class.java -> {
                if (MyMessageActivity.instance != null) {
                    MyMessageActivity.instance!!.setResult(Activity.RESULT_OK)
                    MyMessageActivity.instance!!.finish()
                }
            }
            MyMessageChatActivity::class.java -> {
                if (MyMessageChatActivity.instance != null) {
                    MyMessageChatActivity.instance!!.setResult(Activity.RESULT_OK)
                    MyMessageChatActivity.instance!!.finish()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let { mDetailVp.currentItem = it.getIntExtra(POSITION, 0) }
        if (resultCode == Activity.RESULT_OK && requestCode == TO_MESSAGE) {
            //通过MESSAGE_BACK_BRAND_ID判断，如果当前id存在了，就直接finish掉
            if (MESSAGE_BACK_BRAND_ID.contains("$id")) {
                onBackPressed()
            } else {
                if (TextUtils.isEmpty(MESSAGE_BACK_BRAND_ID)) {
                    MESSAGE_BACK_BRAND_ID = "$id"
                } else {
                    MESSAGE_BACK_BRAND_ID += ",$id"
                }
            }
        }
    }

    override fun onBackPressed() {
        //如果是第一个进来的页面，就让MESSAGE_BACK_BRAND_ID置空
        if (ENTER_BRAND_NUM == 1) {
            ENTER_BRAND_NUM = 0
            MESSAGE_BACK_BRAND_ID = ""
        } else {
            ENTER_BRAND_NUM -= 1
        }
        if (Jzvd.backPress()) return
        super.onBackPressed()
    }

    override fun onDestroy() {
        Jzvd.releaseAllVideos()
        MainApplication.firstVideo = null
        super.onDestroy()
    }

    private fun initWebView() {
        val mWebSettings = mDetailWeb.settings
        mDetailWeb.isVerticalScrollBarEnabled = false
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

        mDetailWeb.apply {
            //解决底部空白问题
            isVerticalScrollBarEnabled = false
            setVerticalScrollbarOverlay(false)
            isHorizontalScrollBarEnabled = false
            setHorizontalScrollbarOverlay(false)
        }

        mDetailWeb.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest?): Boolean {
                view.loadUrl(webUrl)
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                mDetailWeb.loadUrl("javascript:App.resize(document.body.getBoundingClientRect().height)")
                super.onPageFinished(view, url)
//                //编写 javaScript方法
//                val javascript =
//                        "javascript:function hideOther() {" +
//                                "var firstP = document.getElementsByTagName('p');" +
//                                "   if(firstP[0].innerHTML == null || firstP[0].innerHTML == ''){" +
//                                "       firstP[0].parentElement.removeChild(firstP[0]);" +
//                                "   }" +
//                                "}"
//
//                //创建方法
//                view.loadUrl(javascript)
//                //加载方法
//                view.loadUrl("javascript:hideOther();")
            }
        }

        mDetailWeb.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                if (progress >= 100) {
                    myDialog.dismissLoadingDialog()
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        mDetailWeb.addJavascriptInterface(this, "App")
    }

//    @JavascriptInterface
//    fun resize(height: Float) {
//        this@ShopDetailActivity.runOnUiThread(Runnable {
//            ToastUtil.showShort("$height")
//            val layoutParams = mDetailWeb.layoutParams
//            layoutParams.width = resources.displayMetrics.widthPixels
//            layoutParams.height = (height * resources.displayMetrics.density).toInt() + 50
//            mDetailWeb.layoutParams = layoutParams
//        })
//    }

//    override fun onNewIntent(intent: Intent) {
//        super.onNewIntent(intent)
//        startActivity<ShopDetailActivity>(IConstants.BRANDID to intent.getIntExtra(BRANDID, 0))
//        this.finish()
//    }
}