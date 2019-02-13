package com.qingmeng.mengmeng.activity

import android.content.Context
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import kotlinx.android.synthetic.main.activity_my_settings_aboutus.*
import kotlinx.android.synthetic.main.layout_head.*

/**
 *  Description :设置 - 关于我们

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/3
 */
class MySettingsAboutUsActivity : BaseActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_my_settings_aboutus
    }

    override fun initObject() {
        super.initObject()

        setHeadName(R.string.my_settings_aboutUs)

        //设置版本号
        tvMySettingsAboutUsVersion.text = tvMySettingsAboutUsVersion.text.toString() + getLocalVersion(this)
    }

    override fun initListener() {
        super.initListener()

        //返回
        mBack.setOnClickListener {
            onBackPressed()
        }
    }

    //版本
    private fun getLocalVersion(context: Context): String {
        val packageInfo = context.applicationContext
                .packageManager
                .getPackageInfo(context.packageName, 0)
        return packageInfo.versionName
    }
}