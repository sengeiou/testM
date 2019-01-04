package com.qingmeng.mengmeng.fragment

import com.qingmeng.mengmeng.BaseFragment
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.activity.*
import com.qingmeng.mengmeng.view.dialog.SelectDialog
import kotlinx.android.synthetic.main.fragment_my.*

/**
 * 我的板块
 */

class MyFragment : BaseFragment() {

    override fun getLayoutId(): Int {
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
            MySettingsActivity().atyToNext(context!!)
        }

        //我的关注
        llMyMyFollow.setOnClickListener {
            MyMyFollowActivity().atyToNext(context!!, tvMyMyFollow.text as String)
        }

        //我的留言
        llMyMyLeavingMessage.setOnClickListener {
            MyMyLeavingMessageActivity().atyToNext(context!!)
        }

        //我的足迹
        llMyMyFootprint.setOnClickListener {
            MyMyFollowActivity().atyToNext(context!!, tvMyMyFootprint.text as String)
        }

        //企业入驻
        llMyEnterpriseEntry.setOnClickListener {
            MyEnterpriseEntryActivity().atyToNext(context!!)
        }

        //第三方绑定
        llMyThreeBinding.setOnClickListener {
            MyThreeBindingActivity().atyToNext(context!!)
        }

        //消息
        llMyMessage.setOnClickListener {
            MyMessageActivity().atyToNext(context!!)
        }

        //登录
        tvMyLogin.setOnClickListener {

        }
    }
}