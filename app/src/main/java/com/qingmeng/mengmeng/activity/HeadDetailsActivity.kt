package com.qingmeng.mengmeng.activity

/**
 * Created by fyf on 2019/1/5
 * 头报详情页
 */

import android.webkit.WebView
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.view.dialog.ShareDialog
import kotlinx.android.synthetic.main.layout_head.*


class HeadDetailsActivity : BaseActivity() {
    private lateinit var mBottomDialog: ShareDialog
    private lateinit var newsDetailsWebView: WebView
    override fun getLayoutId(): Int = R.layout.activity_head_details
    override fun initObject() {
        super.initObject()
        setHeadName(R.string.head_detail)
        //设置 分享背景 宽 高
        mMenu.setBackgroundResource(R.drawable.common_btn_back)
        mMenu.setBackgroundResource(R.drawable.icon_head_details_share)
//        val lp=mMenu.layoutParams
//        lp.width=100
//        mMenu.layoutParams=lp
        mMenu.width = 50
        mMenu.height = 50
    }

    override fun initData() {
        super.initData()
    }

    override fun initListener() {
        super.initListener()
        mBack.setOnClickListener {
            onBackPressed()
        }
        mMenu.setOnClickListener {
            mBottomDialog = ShareDialog(this)
            mBottomDialog.show()
        }

    }


}
