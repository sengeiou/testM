package com.qingmeng.mengmeng.activity

import AppManager
import android.annotation.SuppressLint
import android.os.Build
import android.view.View
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
