package com.qingmeng.mengmeng.activity

import android.content.Context
import android.content.Intent
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import kotlinx.android.synthetic.main.layout_head.*

/**
 *  Description :设置 - 我的关注/我的足迹

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/3
 */
class MyMyFollowActivity : BaseActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_my_myfollow
    }

    override fun initObject() {
        super.initObject()

        //设置标题
        if (intent.getStringExtra("title") == getString(R.string.my_myFollow)) {
            setHeadName(getString(R.string.my_myFollow))
        } else {//修改密码
            setHeadName(getString(R.string.my_myFootprint))
        }
    }

    override fun initListener() {
        super.initListener()

        //返回
        mBack.setOnClickListener {
            this.finish()
        }
    }

    fun atyToNext(context: Context,title: String) {
        val intent = Intent(context, this.javaClass)
        intent.putExtra("title",title)
        context.startActivity(intent)
    }
}