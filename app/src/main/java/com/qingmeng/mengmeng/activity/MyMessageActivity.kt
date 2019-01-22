package com.qingmeng.mengmeng.activity

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.CommonAdapter
import com.qingmeng.mengmeng.constant.IConstants.TEST_ACCESS_TOKEN
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.view.SwipeMenuLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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
    private var mPageNum: Int = 1                                        //接口请求页数
    private var mCanHttpLoad = true                                      //是否可以请求接口
    private var mHasNextPage = true                                      //是否有下一页

    override fun getLayoutId(): Int {
        return R.layout.activity_my_message
    }

    override fun initObject() {
        super.initObject()

        setHeadName(getString(R.string.message))

        initAdapter()

        srlMyMessage.isRefreshing = true
    }


    override fun initListener() {
        super.initListener()

        //下拉刷新
        srlMyMessage.setOnRefreshListener {
            httpLoad(1)
        }

        //上滑加载
        srlMyMessage.setOnLoadMoreListener {
            httpLoad(mPageNum)
        }

        //返回
        mBack.setOnClickListener {
            onBackPressed()
        }

        rvMyMessage.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                //SwipeMenuLayout关闭view
                SwipeMenuLayout.viewCache?.smoothClose()
            }
            false
        }

        //RecyclerView滑动监听
        rvMyMessage.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                //滑到顶部了
                if (!recyclerView.canScrollVertically(-1)) {
                    if (!srlMyMessage.isLoadingMore) {
                        srlMyMessage.isRefreshEnabled = true
                    }
                } else if (!recyclerView.canScrollVertically(1)) {  //滑到底部了
                    //如果下拉刷新没有刷新的话
                    if (!srlMyMessage.isRefreshing) {
                        if (mList.isNotEmpty()) {
                            //是否有下一页
                            if (mHasNextPage) {
                                //是否可以请求接口
                                if (mCanHttpLoad) {
                                    srlMyMessage.isLoadMoreEnabled = true
                                }
                            }
                        }
                    }
                } else {
                    srlMyMessage.isRefreshEnabled = false
                    srlMyMessage.isLoadMoreEnabled = false
                }
            }
        })
    }

    private fun initAdapter() {
        mLayoutManager = LinearLayoutManager(this)
        rvMyMessage.layoutManager = mLayoutManager
        mAdapter = CommonAdapter(this, R.layout.activity_my_message_item, mList, holderConvert = { holder, t, position, payloads ->
            holder.apply {
                //消息点击
                getView<LinearLayout>(R.id.llMyMessageRv).setOnClickListener {
                    startActivity<MyMessageChatActivity>("title" to getView<TextView>(R.id.tvMyMessageRvTitle).text.toString())
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
    private fun httpLoad(pageNum: Int) {
        mCanHttpLoad = false
        ApiUtils.getApi()
                .threeBindingState(TEST_ACCESS_TOKEN)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    setRefreshAsFalse()
                    mCanHttpLoad = true
                    it.apply {
                        if (code == 12000) {
//                            //如果页数是1 清空内容重新加载
//                            if (pageNum == 1) {
//                                //清空已经选择集合
//                                mList.clear()
//                                mPageNum = 1
//                            }
//                            //请求后判断里面数据
//                            if (data == null || data?.data!!.isEmpty()) {
//                                mHasNextPage = false
//                                if (pageNum == 1) {
//                                    //空白页提示
//                                    llMyMessageTips.visibility = View.VISIBLE
//                                    srlMyMyFollow.isRefreshEnabled = true
//                                }
//                            } else {
//                                mHasNextPage = true
//                                if (pageNum == 1) {
//                                    llMyMessageTips.visibility = View.GONE
//                                }
//                                //把内容添加到mList里去
//                                mList.addAll(data?.data!!)
//                                mPageNum++
//                            }
                            setData()
//                            mAdapter.notifyDataSetChanged()
                        }
                    }
                }, {
                    setRefreshAsFalse()
                    mCanHttpLoad = true
                    llMyMessageTips.visibility = View.VISIBLE
                    srlMyMessage.isRefreshEnabled = true
                })
    }

    private fun setData() {
        mList.clear()
        for (i in 0 until 5) {
            mList.add("")
        }
        mAdapter.notifyDataSetChanged()
    }

    private fun setRefreshAsFalse() {
        srlMyMessage.isRefreshing = false
        srlMyMessage.isLoadingMore = false
        srlMyMessage.isRefreshEnabled = false
        srlMyMessage.isLoadMoreEnabled = false
    }
}