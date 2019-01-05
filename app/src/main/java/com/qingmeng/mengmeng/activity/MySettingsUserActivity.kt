package com.qingmeng.mengmeng.activity

import android.content.Context
import android.content.Intent
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.view.dialog.SelectDialog
import kotlinx.android.synthetic.main.activity_my_settings_user.*
import kotlinx.android.synthetic.main.layout_head.*

/**
 *  Description :设置 - 修改用户信息

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/3
 */
class MySettingsUserActivity : BaseActivity() {
    private lateinit var mBottomDialog: SelectDialog

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
            //菜单内容
            val menuList = arrayListOf(getString(R.string.photograph), getString(R.string.albumSelect))
            mBottomDialog = SelectDialog(this, menuList, onItemClick = {
                //拍照
                if (menuList[it] == getString(R.string.photograph)) {
                    ToastUtil.showShort("拍照")
                } else {  //相册选择
                    ToastUtil.showShort("相册选择")
                }
            })
            mBottomDialog.show()
        }

        //性别
        llMySettingsUserGender.setOnClickListener {
            //菜单内容
            val menuList = arrayListOf(getString(R.string.boy), getString(R.string.girl))
            mBottomDialog = SelectDialog(this, menuList, onItemClick = {
                //设置内容
                tvMySettingsUserGender.text = menuList[it]
            })
            mBottomDialog.show()
        }

        //所在城市
        llMySettingsUserCity.setOnClickListener {

        }

        //创业资本
        llMySettingsUserMoney.setOnClickListener {
            //菜单内容
            val menuList = arrayListOf(getString(R.string.money_3), getString(R.string.money_3_5), getString(R.string.money_5_10), getString(R.string.money_10_20), getString(R.string.money_20_50), getString(R.string.money_50))
            mBottomDialog = SelectDialog(this, menuList, onItemClick = {
                ToastUtil.showShort(menuList[it])
            })
            mBottomDialog.show()
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