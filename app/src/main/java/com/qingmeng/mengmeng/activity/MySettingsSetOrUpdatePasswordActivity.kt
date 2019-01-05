package com.qingmeng.mengmeng.activity

import android.content.Context
import android.content.Intent
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import kotlinx.android.synthetic.main.activity_my_settings_setorupdate.*
import kotlinx.android.synthetic.main.layout_head.*

/**
 *  Description :设置 - 设置或修改密码

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/3
 */
class MySettingsSetOrUpdatePasswordActivity : BaseActivity(){

    override fun getLayoutId(): Int {
        return R.layout.activity_my_settings_setorupdate
    }

    override fun initObject() {
        super.initObject()

        //设置密码标题
        if (intent.getStringExtra("title") == getString(R.string.my_settings_setPassword)){
            setHeadName(getString(R.string.my_settings_setPassword))
        }else{//修改密码
            setHeadName(getString(R.string.my_settings_updatePassword))
        }
    }

    override fun initListener() {
        super.initListener()

        //返回
        mBack.setOnClickListener {
            this.finish()
        }

        //保存
        tvMySettingsSetOrUpdateSave.setOnClickListener {

        }
    }

    fun atyToNext(context: Context,title: String) {
        val intent = Intent(context, this.javaClass)
        intent.putExtra("title",title)
        context.startActivity(intent)
    }
}