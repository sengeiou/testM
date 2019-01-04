package com.qingmeng.mengmeng.activity

import android.content.Context
import android.content.Intent
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import kotlinx.android.synthetic.main.activity_my_settings.*
import kotlinx.android.synthetic.main.layout_head.*

/**
 *  Description :我的 - 设置

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/3
 */
class MySettings : BaseActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_my_settings
    }

    override fun initObject() {
        super.initObject()

        //设置标题
        setHeadName(getString(R.string.my_settings_title))
    }

    override fun initListener() {
        super.initListener()

        //返回
        mBack.setOnClickListener {
            this.finish()
        }

        //用户
        llMySettingsUser.setOnClickListener {
            MySettingsUser().atyToNext(this)
        }

        //设置或修改密码
        llMySettingsUpdatePassword.setOnClickListener {
            MySettingsSetOrUpdatePassword().atyToNext(this,tvMySettingsNewOrOldPassword.text as String)
        }

        //换绑手机
        llMySettingsUpdatePhone.setOnClickListener {
            MySettingsUpdatePhone().atyToNext(this)
        }

        //清理缓存
        llMySettingsClearCache.setOnClickListener {

        }

        //关于我们
        llMySettingsAboutUs.setOnClickListener {
            MySettingsAboutUs().atyToNext(this)
        }

        //退出账号
        tvMySettingsExitUser.setOnClickListener {

        }
    }

    //aty跳转
    fun atyToNext(context: Context) {
        val intent = Intent(context, this.javaClass)
        context.startActivity(intent)
    }
}