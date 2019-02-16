package com.qingmeng.mengmeng.activity

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.CommonAdapter
import com.qingmeng.mengmeng.entity.MyLeavingMessage
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.utils.imageLoader.GlideLoader
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

        setHeadName(R.string.my_myLeavingMessage)

        initAdapter()

//        //自动刷新请求
//        srlMyMyLeavingMessage.isRefreshing = true
        myDialog.showLoadingDialog()
        httpLoad(1)
    }

    override fun initListener() {
        super.initListener()

        //下拉刷新
        srlMyMyLeavingMessage.setOnRefreshListener {
            httpLoad(1)
        }

        //上滑加载
        srlMyMyLeavingMessage.setOnLoadMoreListener {
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
                    if (!srlMyMyLeavingMessage.isLoadingMore) {
                        srlMyMyLeavingMessage.isRefreshEnabled = true
                    }
                } else if (!recyclerView.canScrollVertically(1)) {  //滑到底部了
                    //如果下拉刷新没有刷新的话
                    if (!srlMyMyLeavingMessage.isRefreshing) {
                        if (mList.isNotEmpty()) {
                            //是否有下一页
                            if (mHasNextPage) {
                                //是否可以请求接口
                                if (mCanHttpLoad) {
                                    srlMyMyLeavingMessage.isLoadMoreEnabled = true
                                }
                            }
                        }
                    }
                } else {
                    srlMyMyLeavingMessage.isRefreshEnabled = false
                    srlMyMyLeavingMessage.isLoadMoreEnabled = false
                }
            }
        })
    }

    //适配器加载
    private fun initAdapter() {
        mLayoutManager = LinearLayoutManager(this)
        rvMyMyLeavingMessage.layoutManager = mLayoutManager
        mAdapter = CommonAdapter(this, R.layout.activity_my_myleavingmessage_item, mList, holderConvert = { holder, t, position, payloads ->
            holder.apply {
                setText(R.id.tvMyMyLeavingMessageRvTime, t.createTime)
                //待查看
                if (t.status == 0) {
                    setText(R.id.tvMyMyLeavingMessageRvState, getString(R.string.my_myLeavingMessage_reply))
                    setTextColorRes(R.id.tvMyMyLeavingMessageRvState, R.color.red)
                } else {    //已回复
                    setText(R.id.tvMyMyLeavingMessageRvState, getString(R.string.my_myLeavingMessage_noReply))
                    setTextColorRes(R.id.tvMyMyLeavingMessageRvState, R.color.green)
                }
                setText(R.id.tvMyMyLeavingMessageRvLeavingMessage, t.message)
                GlideLoader.load(this, t.logo, getView(R.id.ivMyMyLeavingMessageRvLogo), centerCrop = false)
                setText(R.id.tvMyMyLeavingMessageRvBrandName, t.brandName)
                setText(R.id.tvMyMyLeavingMessageRvInvestmentAmount, t.capitalName)
                setText(R.id.tvMyMyLeavingMessageRvStoreNum, t.storesNum)
                //删除点击
                getView<LinearLayout>(R.id.llMyMyLeavingMessageRvDelete).setOnClickListener {
                    httpDelLoadOne(mPageNum, t)
                }
                //品牌详情点击
                getView<RelativeLayout>(R.id.rlMyMyLeavingMessageRvBrandDetails).setOnClickListener {

                }
            }
        })
        rvMyMyLeavingMessage.adapter = mAdapter
    }

    //我的留言列表接口请求
    private fun httpLoad(pageNum: Int) {
        mCanHttpLoad = false
        ApiUtils.getApi()
                .myLeavingMessage(pageNum, MainApplication.instance.TOKEN)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    myDialog.dismissLoadingDialog()
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
                                    srlMyMyLeavingMessage.isRefreshEnabled = true
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
                    myDialog.dismissLoadingDialog()
                    setRefreshAsFalse()
                    mCanHttpLoad = true
                    llMyMyLeavingMessageTips.visibility = View.VISIBLE
                    srlMyMyLeavingMessage.isRefreshEnabled = true
                })
    }

    //删除留言接口 先把下一页的数据查出来传给删除方法
    private fun httpDelLoadOne(pageNum: Int, myLeavingMessageDel: MyLeavingMessage) {
        myDialog.showLoadingDialog()
        ApiUtils.getApi()
                .myLeavingMessage(pageNum, MainApplication.instance.TOKEN)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    it.apply {
                        if (code == 12000) {
                            httpDelLoadTwo(data?.data!!, myLeavingMessageDel)
                        } else {
                            ToastUtil.showShort(getString(R.string.my_myFollow_cancel_fail))
                            myDialog.dismissLoadingDialog()
                        }
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                })
    }

    //真.删除留言接口
    private fun httpDelLoadTwo(myLeavingMessageList: List<MyLeavingMessage>, myLeavingMessageDel: MyLeavingMessage) {
        ApiUtils.getApi()
                .deleteMyLeavingMessage(myLeavingMessageDel.id, MainApplication.instance.TOKEN)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    myDialog.dismissLoadingDialog()
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
                    myDialog.dismissLoadingDialog()
                })
    }

    //关闭刷新状态
    private fun setRefreshAsFalse() {
        srlMyMyLeavingMessage.isRefreshing = false
        srlMyMyLeavingMessage.isLoadingMore = false
        srlMyMyLeavingMessage.isRefreshEnabled = false
        srlMyMyLeavingMessage.isLoadMoreEnabled = false
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra("isDelete", mIsDelete)
        })
        super.onBackPressed()
    }
}