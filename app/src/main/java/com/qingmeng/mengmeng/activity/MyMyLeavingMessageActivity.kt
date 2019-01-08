package com.qingmeng.mengmeng.activity

import android.support.v7.widget.LinearLayoutManager
import android.widget.RelativeLayout
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.CommonAdapter
import kotlinx.android.synthetic.main.activity_my_myleavingmessage.*
import kotlinx.android.synthetic.main.layout_head.*

/**
 *  Description :设置 - 我的留言

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/3
 */
class MyMyLeavingMessageActivity : BaseActivity() {
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var mAdapter: CommonAdapter<String>
    private var mList = ArrayList<String>()

    override fun getLayoutId(): Int {
        return R.layout.activity_my_myleavingmessage
    }

    override fun initObject() {
        super.initObject()

        setHeadName(getString(R.string.my_myLeavingMessage))

        initAdapter()

        srlMyMyLeavingMessage.isRefreshing = true
        httpLoad()
    }

    override fun initListener() {
        super.initListener()

        //下拉刷新
        srlMyMyLeavingMessage.setOnRefreshListener {
            httpLoad()
        }

        //返回
        mBack.setOnClickListener {
            this.finish()
        }
    }

    //适配器加载
    private fun initAdapter() {
        mLayoutManager = LinearLayoutManager(this)
        rvMyMyLeavingMessage.layoutManager = mLayoutManager
        mAdapter = CommonAdapter(this, R.layout.activity_my_myleavingmessage_item, mList, holderConvert = { holder, data, position, payloads ->
            holder.apply {
                //品牌详情点击
                getView<RelativeLayout>(R.id.rlMyMyLeavingMessageRvBrandDetails).setOnClickListener {

                }
            }
        }, onItemClick = { view, holder, position ->

        })
        rvMyMyLeavingMessage.adapter = mAdapter
    }

    //我的关注列表接口请求
    private fun httpLoad() {
        srlMyMyLeavingMessage.isRefreshing = false
        setData()
    }

    private fun setData() {
        mList.clear()
        for (i in 0 until 20) {
            mList.add("")
        }
        mAdapter.notifyDataSetChanged()
    }
}