package com.qingmeng.mengmeng.activity

import android.content.Context
import android.content.Intent
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import kotlinx.android.synthetic.main.activity_my_settings_user.*
import kotlinx.android.synthetic.main.layout_head.*

/**
 *  Description :设置 - 修改用户信息

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/3
 */
class MySettingsUser : BaseActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_my_settings_user
    }

    override fun initObject() {
        super.initObject()

        setHeadName(getString(R.string.my_settings_user_title))
    }

    override fun initListener() {
        super.initListener()

        //返回
        mBack.setOnClickListener {
            this.finish()
        }

        //头像
        llMySettingsUserHead.setOnClickListener {

        }

        //性别
        llMySettingsUserGender.setOnClickListener {

        }

        //所在城市
        llMySettingsUserCity.setOnClickListener {

        }

        //创业资本
        llMySettingsUserMoney.setOnClickListener {

        }

        //感兴趣行业
        llMySettingsUserInterestIndustry.setOnClickListener {

        }
    }

    fun atyToNext(context: Context) {
        val intent = Intent(context, this.javaClass)
        context.startActivity(intent)
    }
}