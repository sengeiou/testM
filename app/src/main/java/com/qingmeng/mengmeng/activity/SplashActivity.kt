package com.qingmeng.mengmeng.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.luck.picture.lib.immersive.LightStatusBarUtils
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.utils.ApiUtils
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
    private var time = -1
    private var advImg = ""
    private var advLink = ""
    private var advJump = 1
    private lateinit var handler: SkipHandler

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
        ApiUtils.getApi().getBanners("", 7)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ bean ->
                    if (bean.code == 12000) {
                        bean.data?.let {
                            if (!it.banners.isEmpty()) {
                                advImg = it.banners[0].imgUrl
                                if (isFinishing || (Build.VERSION.SDK_INT >= 17 && isDestroyed)) {
                                    return@let
                                }
                                advLink = it.banners[0].url
                                advJump = it.banners[0].skipType
                                Glide.with(this@SplashActivity).load(advImg)
                                        .apply(RequestOptions().placeholder(R.drawable.img_splash).error(R.drawable.img_splash))
                                        .into(mSplashAdv)
                            }
                        }
                    }
                }, {}, {}, { addSubscription(it) })
    }

    override fun initListener() {
        mSplashAdv.setOnClickListener {
            //todo 广告页跳转
        }
        mSplashBtn.setOnClickListener { handler.sendEmptyMessage(WHAT_SKIP) }
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
                    if (TextUtils.isEmpty(splashActivity.advImg)) {
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