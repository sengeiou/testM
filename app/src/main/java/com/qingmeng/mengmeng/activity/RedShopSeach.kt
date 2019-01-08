package com.qingmeng.mengmeng.activity

import android.view.View
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import kotlinx.android.synthetic.main.layout_head_seach.*

class RedShopSeach : BaseActivity() {
    override fun getLayoutId(): Int = R.layout.activity_red_shop_seach
    override fun initObject() {
        super.initObject()
        head_search_mBack.visibility = View.GONE
        head_search_mMenu.visibility = View.VISIBLE
    }

    override fun initListener() {
        super.initListener()
    }

    override fun initData() {
        super.initData()
    }
}
