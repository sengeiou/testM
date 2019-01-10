package com.qingmeng.mengmeng.activity

import android.support.v7.widget.LinearLayoutManager
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.TextView
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.CommonAdapter
import com.qingmeng.mengmeng.view.SwipeMenuLayout
import kotlinx.android.synthetic.main.activity_my_message.*
import kotlinx.android.synthetic.main.layout_head.*
import org.jetbrains.anko.startActivity

/**
 *  Description :设置 - 消息

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/3
 */
class MyMessageActivity : BaseActivity() {
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var mAdapter: CommonAdapter<String>
    private var mList = ArrayList<String>()

    override fun getLayoutId(): Int {
        return R.layout.activity_my_message
    }

    override fun initObject() {
        super.initObject()

        setHeadName(getString(R.string.message))

        initAdapter()

        srlMyMessage.isRefreshing = true
        httpLoad()
    }


    override fun initListener() {
        super.initListener()

        //返回
        mBack.setOnClickListener {
            this.finish()
        }

        //下拉刷新
        srlMyMessage.setOnRefreshListener {
            httpLoad()
        }

        rvMyMessage.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                //SwipeMenuLayout关闭view
                SwipeMenuLayout.viewCache?.smoothClose()
            }
            false
        }
    }

    private fun initAdapter() {
        mLayoutManager = LinearLayoutManager(this)
        rvMyMessage.layoutManager = mLayoutManager
        mAdapter = CommonAdapter(this, R.layout.activity_my_message_item, mList, holderConvert = { holder, data, position, payloads ->
            holder.apply {
                //消息点击
                getView<LinearLayout>(R.id.llMyMessageRv).setOnClickListener {
                    startActivity<MyMessageChatActivity>()
                }
                //删除
                getView<TextView>(R.id.tvMyMessageRvDelete).setOnClickListener {
                    //关闭view
                    getView<SwipeMenuLayout>(R.id.smlMyMessageRv).smoothClose()
                }
            }
        }, onItemClick = { view, holder, position ->

        })
        rvMyMessage.adapter = mAdapter
    }

    //消息接口请求
    private fun httpLoad() {
        srlMyMessage.isRefreshing = false
        setData()
    }

    private fun setData() {
        mList.clear()
        for (i in 0 until 5) {
            mList.add("")
        }
        mAdapter.notifyDataSetChanged()
    }
}