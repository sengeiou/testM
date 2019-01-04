package com.qingmeng.mengmeng.activity

import android.content.Context
import android.content.Intent
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import kotlinx.android.synthetic.main.activity_my_settings_updatephone.*
import kotlinx.android.synthetic.main.layout_head.*

/**
 *  Description :设置 - 换绑手机

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/3
 */
class MySettingsUpdatePhone : BaseActivity(){

    override fun getLayoutId(): Int {
        return R.layout.activity_my_settings_updatephone
    }

    override fun initObject() {
        super.initObject()

        setHeadName(getString(R.string.my_settings_updatePhone))
    }

    override fun initListener() {
        super.initListener()

        //返回
        mBack.setOnClickListener {
            this.finish()
        }

        //获取验证码
        tvMySettingsUpdatePhoneGetMsg.setOnClickListener {

        }

        //提交
        tvMySettingsUpdatePhoneSubmit.setOnClickListener {

        }
    }

    fun atyToNext(context: Context) {
        val intent = Intent(context, this.javaClass)
        context.startActivity(intent)
    }
}