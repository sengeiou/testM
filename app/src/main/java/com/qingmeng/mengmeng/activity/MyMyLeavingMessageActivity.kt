package com.qingmeng.mengmeng.activity

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.RelativeLayout
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.CommonAdapter
import com.qingmeng.mengmeng.constant.IConstants.TEST_ACCESS_TOKEN
import com.qingmeng.mengmeng.entity.MyLeavingMessage
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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
    private lateinit var mAdapter: CommonAdapter<MyLeavingMessage>
    private var mList = ArrayList<MyLeavingMessage>()
    private var mPageNum: Int = 1                                        //接口请求页数
    private var mCanHttpLoad = true                                      //是否可以请求接口
    private var mHasNextPage = true                                      //是否有下一页
    private var mIsDelete = false                                        //是否删除过数据

    override fun getLayoutId(): Int {
        return R.layout.activity_my_myleavingmessage
    }

    override fun initObject() {
        super.initObject()

        setHeadName(getString(R.string.my_myLeavingMessage))

        initAdapter()

        slMyMyLeavingMessage.isRefreshing = true
        //接口请求
        httpLoad(1)
    }

    override fun initListener() {
        super.initListener()

        //下拉刷新
        slMyMyLeavingMessage.setOnRefreshListener {
            httpLoad(1)
        }

        //上滑加载
        slMyMyLeavingMessage.setOnLoadMoreListener {
            httpLoad(mPageNum)
        }

        //返回
        mBack.setOnClickListener {
            onBackPressed()
        }

        //RecyclerView滑动监听
        rvMyMyLeavingMessage.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                //滑到顶部了
                if (!recyclerView.canScrollVertically(-1)) {
                    if (!slMyMyLeavingMessage.isLoadingMore) {
                        slMyMyLeavingMessage.isRefreshEnabled = true
                    }
                } else if (!recyclerView.canScrollVertically(1)) {  //滑到底部了
                    //如果下拉刷新没有刷新的话
                    if (!slMyMyLeavingMessage.isRefreshing) {
                        if (mList.isNotEmpty()) {
                            //是否有下一页
                            if (mHasNextPage) {
                                //是否可以请求接口
                                if (mCanHttpLoad) {
                                    slMyMyLeavingMessage.isLoadMoreEnabled = true
                                }
                            }
                        }
                    }
                } else {
                    slMyMyLeavingMessage.isRefreshEnabled = false
                    slMyMyLeavingMessage.isLoadMoreEnabled = false
                }
            }
        })
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

    //我的留言列表接口请求
    private fun httpLoad(pageNum: Int) {
        mCanHttpLoad = false
        ApiUtils.getApi()
                .myLeavingMessage(pageNum, TEST_ACCESS_TOKEN)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    //刷新状态关闭
                    setRefreshAsFalse()
                    mCanHttpLoad = true
                    it.apply {
                        if (code == 12000) {
                            //如果页数是1 清空内容重新加载
                            if (pageNum == 1) {
                                //清空已经选择集合
                                mList.clear()
                                mPageNum = 1
                            }
                            //请求后判断里面数据
                            if (data == null || data?.data!!.isEmpty()) {
                                mHasNextPage = false
                                if (pageNum == 1) {
                                    //空白页提示
                                    llMyMyLeavingMessageTips.visibility = View.VISIBLE
                                }
                            } else {
                                mHasNextPage = true
                                if (pageNum == 1) {
                                    llMyMyLeavingMessageTips.visibility = View.GONE
                                }
                                //把内容添加到mList里去
                                mList.addAll(data?.data!!)
                                mPageNum++
                            }
                            mAdapter.notifyDataSetChanged()
                        }
                    }
                }, {
                    setRefreshAsFalse()
                    mCanHttpLoad = true
                    llMyMyLeavingMessageTips.visibility = View.VISIBLE
                })
    }

    //删除留言接口 先把下一页的数据查出来传给删除方法
    private fun httpDelLoadOne(pageNum: Int, myLeavingMessageDel: MyLeavingMessage) {
        mCanHttpLoad = false
        ApiUtils.getApi()
                .myLeavingMessage(pageNum, TEST_ACCESS_TOKEN)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    it.apply {
                        if (code == 12000) {
                            httpDelLoadTwo(data?.data!!, myLeavingMessageDel)
                        } else {
                            ToastUtil.showShort(getString(R.string.my_myFollow_cancel_fail))
                        }
                    }
                }, {

                })
    }

    //真.删除留言接口
    private fun httpDelLoadTwo(myLeavingMessageList: List<MyLeavingMessage>, myLeavingMessageDel: MyLeavingMessage) {
        ApiUtils.getApi()
                .deleteMyLeavingMessage(myLeavingMessageDel.id, TEST_ACCESS_TOKEN)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    it.apply {
                        if (code == 12000) {
                            mIsDelete = true
                            //直接根据对象从mList里移除数据
                            mList.remove(myLeavingMessageDel)
                            //再往最后面加一个下一页接口的第一个数据
                            if (myLeavingMessageList.isNotEmpty()) {
                                mList.add(myLeavingMessageList[0])
                            }
                            mAdapter.notifyDataSetChanged()
                        }
                    }
                }, {

                })
    }

    //关闭刷新状态
    private fun setRefreshAsFalse() {
        slMyMyLeavingMessage.isRefreshing = false
        slMyMyLeavingMessage.isLoadingMore = false
        slMyMyLeavingMessage.isRefreshEnabled = false
        slMyMyLeavingMessage.isLoadMoreEnabled = false
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra("isDelete", mIsDelete)
        })
        super.onBackPressed()
    }
}