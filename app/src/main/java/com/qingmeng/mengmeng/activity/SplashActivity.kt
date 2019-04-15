package com.qingmeng.mengmeng.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.os.Handler
import android.os.Message
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.luck.picture.lib.immersive.LightStatusBarUtils
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.GuideImgAdapter
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.OpenMallApp
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_splash.*
import java.lang.ref.WeakReference

class SplashActivity : BaseActivity() {
    /**
     * 跳转到新界面
     */
    private val WHAT_SKIP = 0
    private val REQUEST_CODE_ADV = 0
    /**
     * 显示广告页面
     */
    private val WHAT_ADV = 1
    private var currentIndex = 0
    private var time = -1
    private var advImg = ""
    private var advWebLink = ""
    private var advAppLink = ""
    private var advJump = 1
    private lateinit var handler: SkipHandler
    private var mAdapter: GuideImgAdapter? = null

    override fun getLayoutId(): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.TRANSPARENT
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            LightStatusBarUtils.setLightStatusBar(this, true)
        }
        return R.layout.activity_splash
    }

    override fun initObject() {
        handler = SkipHandler(this)
    }

    override fun initData() {
        handler.sendEmptyMessageDelayed(WHAT_ADV, 1500)
        getAdv()
    }

    @SuppressLint("ObsoleteSdkInt", "CheckResult")
    private fun getAdv() {
        ApiUtils.getApi().getBanners("", 9)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ bean ->
                    if (bean.code == 12000) {
                        bean.data?.let {
                            if (!it.banners.isEmpty()) {
                                advImg = it.banners[it.banners.lastIndex].imgUrl
                                if (isFinishing || (Build.VERSION.SDK_INT >= 17 && isDestroyed)) {
                                    return@let
                                }
                                advWebLink = it.banners[it.banners.lastIndex].url
                                advAppLink = it.banners[it.banners.lastIndex].exteriorUrl
                                advJump = it.banners[it.banners.lastIndex].skipType
                                Glide.with(this@SplashActivity).load(advImg)
                                        .apply(RequestOptions().placeholder(R.drawable.img_splash).error(R.drawable.img_splash))
                                        .into(mSplashAdv)
                            }
                        }
                    }
                }, {}, {}, { addSubscription(it) })
    }

    override fun initListener() {
        //广告跳转
        mSplashAdv.setOnClickListener {
            if (!TextUtils.isEmpty(advAppLink)) {
                try {
                    OpenMallApp.open(this, advAppLink)
                } catch (e: OpenMallApp.NotInstalledException) {
                    startActivity(Intent(this, WebViewActivity::class.java).apply {
                        putExtra(IConstants.detailUrl, advWebLink)
                        putExtra(IConstants.title, "详情")
                    })
                }
            }
        }
        mSplashBtn.setOnClickListener { handler.sendEmptyMessage(WHAT_SKIP) }
        mSplashVp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                Log.e("SplashActivity", "onPageScrollStateChanged  state == $state")
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                Log.e("SplashActivity", "onPageScrolled  position == $position  positionOffset == $positionOffset  positionOffsetPixels == $positionOffsetPixels")
            }

            override fun onPageSelected(position: Int) {
                Log.e("SplashActivity", "onPageSelected  position == $position")
                currentIndex = position
            }

        })
        mSplashVp.setOnTouchListener(object : View.OnTouchListener {
            var startX = 0f
            var endX = 0f
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                event?.apply {
                    when (action) {
                        MotionEvent.ACTION_DOWN -> startX = x
                        MotionEvent.ACTION_UP -> {
                            endX = x
                            val windowManager = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                            //获取屏幕的宽度
                            val size = Point()
                            windowManager.defaultDisplay.getSize(size)
                            val width = size.x
                            //首先要确定的是，是否到了最后一页，然后判断是否向左滑动，并且滑动距离是否符合，我这里的判断距离是屏幕宽度的4分之一（这里可以适当控制）
                            mAdapter?.let {
                                if (currentIndex == it.count - 1 && startX - endX > 0 && startX - endX >= width / 4) {
                                    sharedSingleton.setBoolean(IConstants.FIRSTLOGIN, false)
                                    toMainActivity()
                                }
                            }
                        }
                    }
                }
                return false
            }

        })
    }

    private fun showAdv() {
        val animation = AlphaAnimation(0.0f, 1.0f)
        animation.duration = 500
        mSplashAdv.animation = animation
        mSplashBtn.animation = animation
        mSplashAdv.visibility = View.VISIBLE
        mSplashBtn.visibility = View.VISIBLE
    }

    private fun toMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        handler.removeMessages(WHAT_ADV)
        handler.removeMessages(WHAT_SKIP)
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADV) {
            handler.sendEmptyMessage(WHAT_SKIP)
        }
    }

    @SuppressLint("HandlerLeak")
    inner class SkipHandler(activity: SplashActivity) : Handler() {
        private var mWeakActivity: WeakReference<SplashActivity> = WeakReference(activity)

        @SuppressLint("SetTextI18n")
        override fun handleMessage(msg: Message?) {
            val splashActivity = mWeakActivity.get() ?: return
            when (msg?.what) {
                splashActivity.WHAT_SKIP -> {
                    toMainActivity()
                }
                splashActivity.WHAT_ADV -> {
                    if (sharedSingleton.getBoolean(IConstants.FIRSTLOGIN, true)) {
                        mSplashContent.visibility = View.GONE
                        mSplashVp.visibility = View.VISIBLE
                        mAdapter = GuideImgAdapter()
                        mSplashVp.adapter = mAdapter
                    } else if (TextUtils.isEmpty(splashActivity.advImg)) {
                        //没有广告信息
                        splashActivity.handler.sendEmptyMessageDelayed(splashActivity.WHAT_SKIP, 1000)
                    } else {
                        if (splashActivity.time == 0) {
                            splashActivity.handler.sendEmptyMessageDelayed(splashActivity.WHAT_SKIP, 1000)
                        } else {
                            if (splashActivity.time == -1) {
                                splashActivity.time = 4
                                showAdv()
                            }
                            mSplashBtn.text = "跳过(${splashActivity.time})"
                            splashActivity.time--
                            splashActivity.handler.sendEmptyMessageDelayed(splashActivity.WHAT_ADV, 1000)
                        }
                    }
                }
            }
        }
    }
}