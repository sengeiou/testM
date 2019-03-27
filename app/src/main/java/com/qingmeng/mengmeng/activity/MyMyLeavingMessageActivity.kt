package com.qingmeng.mengmeng.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.CommonAdapter
import com.qingmeng.mengmeng.adapter.LoadMoreWrapper
import com.qingmeng.mengmeng.adapter.util.FooterView
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.entity.MyLeavingMessage
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.utils.imageLoader.GlideLoader
import com.qingmeng.mengmeng.utils.setFooterStatus
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_my_myleavingmessage.*
import kotlinx.android.synthetic.main.layout_head.*
import org.jetbrains.anko.startActivity

/**
 *  Description :设置 - 我的留言

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/3
 */
@SuppressLint("CheckResult")
class MyMyLeavingMessageActivity : BaseActivity() {
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var mAdapter: LoadMoreWrapper<MyLeavingMessage>
    private var mList = ArrayList<MyLeavingMessage>()
    private lateinit var mFootView: FooterView
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

        mFootView = FooterView(this)
        setFooterStatus(mFootView, 3)

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
                        srlMyMyLeavingMessage.isLoadMoreEnabled = false
                    }
                    //没有滑动时 在最下面一个item
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE && mLayoutManager.findLastVisibleItemPosition() + 1 == mAdapter.itemCount) {
                    //如果下拉刷新没有刷新的话
                    if (!srlMyMyLeavingMessage.isRefreshing) {
                        if (mList.isNotEmpty()) {
                            //是否有下一页
                            if (mHasNextPage) {
                                //是否可以请求接口
                                if (mCanHttpLoad) {
                                    srlMyMyLeavingMessage.isRefreshEnabled = false
//                                    srlMyMyLeavingMessage.isLoadMoreEnabled = true
                                    httpLoad(mPageNum)
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
        val adapter = CommonAdapter(this, R.layout.activity_my_myleavingmessage_item, mList, holderConvert = { holder, t, position, payloads ->
            holder.apply {
                setText(R.id.tvMyMyLeavingMessageRvTime, t.createTime)
                //待查看
                if (t.status == 0) {
                    setText(R.id.tvMyMyLeavingMessageRvState, getString(R.string.my_myLeavingMessage_noReply))
                    setTextColorRes(R.id.tvMyMyLeavingMessageRvState, R.color.red)
                } else {    //已回复
                    setText(R.id.tvMyMyLeavingMessageRvState, getString(R.string.my_myLeavingMessage_reply))
                    setTextColorRes(R.id.tvMyMyLeavingMessageRvState, R.color.green)
                }
                setText(R.id.tvMyMyLeavingMessageRvLeavingMessage, t.message)
                GlideLoader.load(this@MyMyLeavingMessageActivity, t.logo, getView(R.id.ivMyMyLeavingMessageRvLogo), centerCrop = false, placeholder = R.drawable.default_img_icon)
                setText(R.id.tvMyMyLeavingMessageRvBrandName, t.brandName)
                if (t.capitalName.isNullOrBlank()) {
                    setText(R.id.tvMyMyLeavingMessageRvInvestmentAmount, getString(R.string.face))
                } else {
                    setText(R.id.tvMyMyLeavingMessageRvInvestmentAmount, t.capitalName)
                }
                getView<TextView>(R.id.tvMyMyLeavingMessageRvStoreNumStatic).let {
                    if (TextUtils.isEmpty(t.storesNum) || t.storesNum == "null" || t.storesNum == "0") {
                        it.visibility = View.GONE
                        setText(R.id.tvMyMyLeavingMessageRvStoreNum, "")
                    } else {
                        it.visibility = View.VISIBLE
                        setText(R.id.tvMyMyLeavingMessageRvStoreNum, t.storesNum)
                    }
                }
                //删除点击
                getView<LinearLayout>(R.id.llMyMyLeavingMessageRvDelete).setOnClickListener {
                    httpDelLoadOne(mPageNum, t)
                }
                //品牌详情点击
                getView<RelativeLayout>(R.id.rlMyMyLeavingMessageRvBrandDetails).setOnClickListener {
                    if (t.brandId == 0 || t.isDel || t.commentType == 1) {
                        ToastUtil.showShort(R.string.invalid_brand_tips)
                    } else {
                        startActivity<ShopDetailActivity>(IConstants.BRANDID to t.brandId)
                    }
                }
            }
        })
        mAdapter = LoadMoreWrapper(adapter)
        mAdapter.setLoadMoreView(mFootView)
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
                                    setTipsText(getString(R.string.my_myLeavingMessage_null_tips))
                                    llMyMyLeavingMessageTips.visibility = View.VISIBLE
                                    srlMyMyLeavingMessage.isRefreshEnabled = true
                                    setFooterStatus(mFootView, 3)
                                } else {
                                    setFooterStatus(mFootView, 2)
                                }
                            } else {
                                mHasNextPage = true
                                if (pageNum == 1) {
                                    setTipsText(getString(R.string.my_myLeavingMessage_null_tips))
                                    llMyMyLeavingMessageTips.visibility = View.GONE
                                }
                                //把内容添加到mList里去
                                mList.addAll(data?.data!!)
                                mPageNum++
                                setFooterStatus(mFootView, 1)
                                //如果不满一个屏幕 就隐藏
                                if (mList.size < 8) {
                                    setFooterStatus(mFootView, 3)
                                }
                            }
                            mAdapter.notifyDataSetChanged()
                        } else {
                            ToastUtil.showShort(it.msg)
                        }
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                    setRefreshAsFalse()
                    mCanHttpLoad = true
                    setTipsText(getString(R.string.no_net))
                    llMyMyLeavingMessageTips.visibility = View.VISIBLE
                    srlMyMyLeavingMessage.isRefreshEnabled = true
                }, {}, { addSubscription(it) })
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
                            ToastUtil.showShort(getString(R.string.my_myFollow_delete_fail))
                            myDialog.dismissLoadingDialog()
                        }
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                }, {}, { addSubscription(it) })
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
                        } else {
                            ToastUtil.showShort(msg)
                        }
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                }, {}, { addSubscription(it) })
    }

    //关闭刷新状态
    private fun setRefreshAsFalse() {
        srlMyMyLeavingMessage.isRefreshing = false
        srlMyMyLeavingMessage.isLoadingMore = false
        srlMyMyLeavingMessage.isRefreshEnabled = false
        srlMyMyLeavingMessage.isLoadMoreEnabled = false
    }

    //设置提示内容
    private fun setTipsText(tips: String) {
        llMyMyLeavingMessageTips.findViewById<TextView>(R.id.tvViewTips).text = tips
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra("isDelete", mIsDelete)
        })
        super.onBackPressed()
    }
}