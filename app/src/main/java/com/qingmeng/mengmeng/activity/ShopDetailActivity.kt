package com.qingmeng.mengmeng.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ImageSpan
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import cn.jzvd.Jzvd
import cn.jzvd.JzvdStd
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.ShopDetailVpAdapter
import com.qingmeng.mengmeng.constant.IConstants.BRANDID
import com.qingmeng.mengmeng.constant.IConstants.IMGS
import com.qingmeng.mengmeng.constant.IConstants.POSITION
import com.qingmeng.mengmeng.entity.BrandBean
import com.qingmeng.mengmeng.entity.BrandInformation
import com.qingmeng.mengmeng.entity.BrandInitialFee
import com.qingmeng.mengmeng.entity.ShopDetailImg
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.utils.setDrawableTop
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_shop_detail.*

@Suppress("DEPRECATION")
@SuppressLint("CheckResult", "StringFormatMatches", "SetTextI18n", "SetJavaScriptEnabled")
class ShopDetailActivity : BaseActivity() {
    private lateinit var vpAdapter: ShopDetailVpAdapter
    private val mImgList = ArrayList<ShopDetailImg>()
    private val mJoinSupport = ArrayList<String>()
    private var brandInformation: BrandInformation? = null
    private var brandInitialFee: BrandInitialFee? = null
    private var name = ""
    private var id = 0
    private var totalImg = 0
    private var hasVideo = false
    private var isAttention = 0

    override fun getLayoutId(): Int = R.layout.activity_shop_detail

    override fun initObject() {
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

    private fun setData(bean: BrandBean) {
        name = bean.name
        if (bean.status == 1) {
            val spanString = SpannableString("认证\t$name")
            val drawable = resources.getDrawable(R.drawable.detail_icon_certification)
            val imageSpan = ImageSpan(drawable, ImageSpan.ALIGN_BASELINE)
            spanString.setSpan(imageSpan, 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            val density = resources.displayMetrics.density
            drawable.setBounds(0, (7 * density).toInt(), (26 * density).toInt(), (21 * density).toInt())
            mDetailName.text = spanString
        } else {
            mDetailName.text = name
        }
        mDetailType.text = bean.foodName
        mDetailMoney.text = bean.capitalName
        mDetailJoinCount.text = getString(R.string.join_count, bean.joinStoreNum)
        mDetailDirectCount.text = getString(R.string.join_count, bean.directStoreNum)
        if (!mJoinSupport.isEmpty()) {
            mJoinSupport.clear()
        }
        bean.affiliateSupport.apply {
            trainContent?.let { mJoinSupport.addAll(it) }
            operateSupport?.let { mJoinSupport.addAll(it) }
            operationalSupervision?.let { mJoinSupport.addAll(it) }
            if (!TextUtils.isEmpty(decorationName)) mJoinSupport.add(decorationName)
            if (!TextUtils.isEmpty(trainingMethodName)) mJoinSupport.add(decorationName)
        }
        brandInformation = bean.brandInformation
        brandInitialFee = bean.brandInitialFee
        isAttention = bean.isAttention
        if (bean.isAttention == 0) {
            mCollection.setDrawableTop(R.drawable.detail_icon_collection)
        } else {
            mCollection.setDrawableTop(R.drawable.detail_icon_collected)
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
//        mDetailWeb.loadUrl("https://www.baidu.com/")
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun initListener() {
        mDetailBack.setOnClickListener { onBackPressed() }
        mDetailMore.setOnClickListener {
            myDialog.showMorePop(it, {

            }, {

            }, {

            }, {
                startActivity(Intent(this, JoinFeedbackActivity::class.java).putExtra(BRANDID, id))
            })
        }
        mDetailJoinSupport.setOnClickListener { myDialog.showBrandDialog(mJoinSupport) }
        mDetailBrandInformation.setOnClickListener { _ -> brandInformation?.let { myDialog.showBrandDialog(it) } }
        mDetailJoinMoney.setOnClickListener { _ -> brandInitialFee?.let { myDialog.showBrandDialog(it) } }
        mCustomerService.setOnClickListener { }
        mCollection.setOnClickListener { if (isAttention == 0) addAttention() else unAttention() }
        mGetJoinData.setOnClickListener { myDialog.showJoinDataDialog(name) { name, phone, message -> join(name, phone, message) } }
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
                if (Math.abs(maskAlpha)<=1) {
                    mDetailBack.alpha = Math.abs(maskAlpha)
                    mDetailMore.alpha = Math.abs(maskAlpha)
                } else{
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

    //加盟
    private fun join(name: String, phone: String, message: String) {
        myDialog.showLoadingDialog()
        ApiUtils.getApi().join(id, name, phone, message)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    myDialog.dismissLoadingDialog()
                    if (bean.code == 12000) {
                        ToastUtil.showShort(R.string.submit_success)
                    } else {
                        ToastUtil.showShort(bean.msg)
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
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
                    } else {
                        ToastUtil.showShort(bean.msg)
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let { mDetailVp.currentItem = it.getIntExtra(POSITION, 0) }
    }

    override fun onBackPressed() {
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
    }
}