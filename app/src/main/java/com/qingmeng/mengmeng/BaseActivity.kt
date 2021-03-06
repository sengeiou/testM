package com.qingmeng.mengmeng

import AppManager
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import com.qingmeng.mengmeng.constant.ImageCodeHandler
import com.qingmeng.mengmeng.utils.SharedSingleton
import com.qingmeng.mengmeng.utils.TimerHandler
import com.qingmeng.mengmeng.view.dialog.DialogCommon
import com.qingmeng.mengmeng.view.dialog.DialogCustom
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.layout_head.*

/**
 * Created by zq on 2018/8/6
 */
abstract class BaseActivity : AppCompatActivity() {
    protected val sharedSingleton = SharedSingleton.instance
    lateinit var myDialog: DialogCustom
    lateinit var imgHandler: ImageCodeHandler
    lateinit var mDialogCommon: DialogCommon
    var totalTime = -1
    var timing = 1
    var timingOver = 2
    lateinit var timerHandler: TimerHandler
    private val mCompositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        savedInstanceState?.remove("android:support:fragments")
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 21) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.main_theme)
        }
        setContentView(getLayoutId())
        AppManager.instance.addActivity(this)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT //禁止横屏
        myDialog = DialogCustom(AppManager.instance.currentActivity())
        mDialogCommon = DialogCommon(this, getString(R.string.tips), getString(R.string.otherLogin_tips),
                getString(R.string.exit), getString(R.string.loginAgain), canBackCancel = false)
        setShowBack(true)
        initObject()
        initListener()
        initData()
    }

    /**
     * 设置标题名
     */
    protected fun setHeadName(headName: String) {
        mTitle?.text = headName
    }

    /**
     * 根据字符串设置标题名
     *
     * ram resourceId
     */
    protected fun setHeadName(resourceId: Int) {
        mTitle?.setText(resourceId)
    }

    /**
     * 设置标题背景色
     */
    protected fun setHeadBack(color: String) {
        mTopView?.setBackgroundColor(Color.parseColor(color))
    }

    /**
     * 根据字符串设置标题背景色
     *
     * ram resourceId
     */
    protected fun setHeadBack(resourceId: Int) {
        mTopView?.setBackgroundResource(resourceId)
    }

    /**
     * 是否显示返回键   默认显示
     */
    protected fun setShowBack(isShowBack: Boolean) {
        mBack?.let { it ->
            it.visibility = if (isShowBack) {
                it.setOnClickListener { onBackPressed() }
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    abstract fun getLayoutId(): Int

    open fun initObject() {

    }

    open fun initData() {}

    open fun initListener() {}

    fun addSubscription(disposable: Disposable) {
        mCompositeDisposable.add(disposable)
    }

    override fun onDestroy() {
        super.onDestroy()
        mCompositeDisposable.clear()
        myDialog.unBindContext()
        AppManager.instance.toFinish(javaClass)
    }
}