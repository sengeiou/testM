package com.qingmeng.mengmeng.fragment

import com.qingmeng.mengmeng.BaseFragment
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.activity.MySettings
import kotlinx.android.synthetic.main.fragment_my.*

/**
 * 我的板块
 */

class MyFragment :BaseFragment(){

    override fun getLayoutId(): Int{
        return R.layout.fragment_my
    }

    //view初始化
    override fun initObject() {
        super.initObject()


    }

    //点击事件
    override fun initListener() {
        super.initListener()

        //头像
        ivMyHeadPortrait.setOnClickListener {

        }

        //设置
        ivMySettings.setOnClickListener {
            //跳转aty
            MySettings().atyToNext(context!!)
        }

        //我的关注
        llMyMyFollow.setOnClickListener {

        }

        //我的留言
        llMyMyLeavingMessage.setOnClickListener {

        }

        //我的足迹
        llMyMyFootprint.setOnClickListener {

        }

        //企业入驻
        llMyEnterpriseEntry.setOnClickListener {

        }

        //第三方绑定
        llMyThreeBinding.setOnClickListener {

        }

        //消息
        llMyMessage.setOnClickListener {

        }

        //登录
        tvMyLogin.setOnClickListener {

        }
    }
}