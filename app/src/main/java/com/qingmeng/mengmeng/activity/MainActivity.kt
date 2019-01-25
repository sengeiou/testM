package com.qingmeng.mengmeng.activity

import AppManager
import android.annotation.SuppressLint
import android.os.Build
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TabWidget
import android.widget.TextView
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.base.MainTab
import com.qingmeng.mengmeng.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    private var firstTime = 0L

    override fun getLayoutId(): Int = R.layout.activity_main


    @SuppressLint("ObsoleteSdkInt")
    override fun initObject() {
        //设置状态栏隐藏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.transparent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }

        tabhost.setup(this, supportFragmentManager, R.id.realtabcontent)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            tabhost.tabWidget.showDividers = TabWidget.SHOW_DIVIDER_NONE
        }
        initTabs()
        setShowBack(false)
    }

    private fun initTabs() {
        val tabs = MainTab.values()
        val size = tabs.size
        for (i in 0 until size) {
            val mainTab = tabs[i]
            val tab = tabhost.newTabSpec(getString(mainTab.resName))
            val indicator = layoutInflater.inflate(R.layout.tab_indicator, null)
            val icon: ImageView = indicator.findViewById(R.id.tab_icon)
            icon.setImageResource(mainTab.resIcon)
            val title: TextView = indicator.findViewById(R.id.tab_titile)
            title.text = getString(mainTab.resName)
            tab.setIndicator(indicator)
            tab.setContent { View(this) }
            tabhost.addTab(tab, mainTab.clz, null)
        }
    }

    override fun onBackPressed() {
        val secondTime = System.currentTimeMillis()
        if (secondTime - firstTime > 2000) {//如果两次按键时间间隔大于2秒，则不退出
            ToastUtil.showShort(getString(R.string.exit_app))
            firstTime = secondTime//更新firstTime
        } else {//两次按键小于2秒时，退出应用
            AppManager.instance.appExit(this)
        }
    }
}
