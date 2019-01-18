package com.qingmeng.mengmeng.activity

import android.app.Activity
import android.content.Intent
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
import com.qingmeng.mengmeng.entity.MyFollow
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.utils.imageLoader.CacheType
import com.qingmeng.mengmeng.utils.imageLoader.GlideLoader
import com.qingmeng.mengmeng.view.SwipeMenuLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_my_myfollow.*
import kotlinx.android.synthetic.main.layout_head.*

/**
 *  Description :设置 - 我的关注/我的足迹

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/3
 */
class MyMyFollowActivity : BaseActivity() {
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var mAdapter: CommonAdapter<MyFollow>
    private var mList = ArrayList<MyFollow>()
    private var mPageNum: Int = 1                                        //接口请求页数
    private var mCanHttpLoad = true                                      //是否可以请求接口
    private var mHasNextPage = true                                      //是否有下一页
    private var mIsMyFollow = true                                       //是否是我的关注
    private var mIsDelete = false                                        //是否删除过数据

    override fun getLayoutId(): Int {
        return R.layout.activity_my_myfollow
    }

    override fun initObject() {
        super.initObject()

        //设置标题
        if (intent.getStringExtra("title") == getString(R.string.my_myFollow)) {
            setHeadName(getString(R.string.my_myFollow))
            mIsMyFollow = true
        } else {
            setHeadName(getString(R.string.my_myFootprint))
            mIsMyFollow = false
        }

        //适配器初始化
        initAdapter()

        //自动刷新请求
        srlMyMyFollow.isRefreshing = true
    }

    override fun initListener() {
        super.initListener()

        //下拉刷新
        srlMyMyFollow.setOnRefreshListener {
            httpLoad(1)
        }

        //上滑加载
        srlMyMyFollow.setOnLoadMoreListener {
            httpLoad(mPageNum)
        }

        //返回
        mBack.setOnClickListener {
            onBackPressed()
        }

        rvMyMyFollow.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                //SwipeMenuLayout关闭view
                SwipeMenuLayout.viewCache?.smoothClose()
            }
            false
        }

        //RecyclerView滑动监听
        rvMyMyFollow.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                //滑到顶部了
                if (!recyclerView.canScrollVertically(-1)) {
                    if (!srlMyMyFollow.isLoadingMore) {
                        srlMyMyFollow.isRefreshEnabled = true
                    }
                } else if (!recyclerView.canScrollVertically(1)) {  //滑到底部了
                    //如果下拉刷新没有刷新的话
                    if (!srlMyMyFollow.isRefreshing) {
                        if (mList.isNotEmpty()) {
                            //是否有下一页
                            if (mHasNextPage) {
                                //是否可以请求接口
                                if (mCanHttpLoad) {
                                    srlMyMyFollow.isLoadMoreEnabled = true
                                }
                            }
                        }
                    }
                } else {
                    srlMyMyFollow.isRefreshEnabled = false
                    srlMyMyFollow.isLoadMoreEnabled = false
                }
            }
        })
    }

    //适配器加载
    private fun initAdapter() {
        mLayoutManager = LinearLayoutManager(this)
        rvMyMyFollow.layoutManager = mLayoutManager
        mAdapter = CommonAdapter(this, R.layout.activity_my_myfollow_item, mList, holderConvert = { holder, t, position, payloads ->
            holder.apply {
                //glide加载图片
                GlideLoader.load(this@MyMyFollowActivity, t.logo, getView(R.id.ivMyMyFollowRvLogo), cacheType = CacheType.All)
                if (mIsMyFollow) {
                    setText(R.id.tvMyMyFollowRvBrandName, t.name)
                } else {
                    setText(R.id.tvMyMyFollowRvBrandName, t.brandName)
                }
                setText(R.id.tvMyMyFollowRvCateringType, t.foodName)
                setText(R.id.tvMyMyFollowRvInvestmentAmount, t.capitalName)
                //item点击
                getView<LinearLayout>(R.id.llMyMyFollowRv).setOnClickListener {

                }
                //item取消关注
                getView<TextView>(R.id.tvMyMyFollowRvDelete).setOnClickListener {
                    //删除菜单关闭
                    getView<SwipeMenuLayout>(R.id.smlMyMyFollowRv).smoothClose()
                    httpDelLoadOne(mPageNum, t)
                }
            }
        })
        rvMyMyFollow.adapter = mAdapter
    }

    //我的关注列表接口请求
    private fun httpLoad(pageNum: Int) {
        mCanHttpLoad = false
        ApiUtils.getApi().let {
            if (mIsMyFollow) {
                it.myFollow(pageNum, TEST_ACCESS_TOKEN)
            } else {
                it.myFootprint(pageNum, TEST_ACCESS_TOKEN)
            }
        }
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
                                    llMyMyFollowTips.visibility = View.VISIBLE
                                    srlMyMyFollow.isRefreshEnabled = true
                                }
                            } else {
                                mHasNextPage = true
                                if (pageNum == 1) {
                                    llMyMyFollowTips.visibility = View.GONE
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
                    llMyMyFollowTips.visibility = View.VISIBLE
                    srlMyMyFollow.isRefreshEnabled = true
                })
    }

    //取消关注接口 先把下一页的数据查出来传给删除方法
    private fun httpDelLoadOne(pageNum: Int, myFollowDel: MyFollow) {
        myDialog.showLoadingDialog()
        ApiUtils.getApi().let {
            if (mIsMyFollow) {
                it.myFollow(pageNum, TEST_ACCESS_TOKEN)
            } else {
                it.myFootprint(pageNum, TEST_ACCESS_TOKEN)
            }
        }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    it.apply {
                        if (code == 12000) {
                            httpDelLoadTwo(data?.data!!, myFollowDel)
                        } else {
                            ToastUtil.showShort(getString(R.string.my_myFollow_cancel_fail))
                            myDialog.dismissLoadingDialog()
                        }
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                })
    }

    //真.取消关注接口
    private fun httpDelLoadTwo(myFollowList: List<MyFollow>, myFollowDel: MyFollow) {
        ApiUtils.getApi().let {
            if (mIsMyFollow) {
                it.deleteMyFollow(myFollowDel.id, TEST_ACCESS_TOKEN)
            } else {
                it.deleteMyFootprint(myFollowDel.brandId, TEST_ACCESS_TOKEN)
            }
        }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    myDialog.dismissLoadingDialog()
                    it.apply {
                        if (code == 12000) {
                            mIsDelete = true
                            //直接根据对象从mList里移除数据
                            mList.remove(myFollowDel)
                            //再往最后面加一个下一页接口的第一个数据
                            if (myFollowList.isNotEmpty()) {
                                mList.add(myFollowList[0])
                            }
                            mAdapter.notifyDataSetChanged()
                        }
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                })
    }

    //用到的地方偏多 统一一下
    private fun setRefreshAsFalse() {
        srlMyMyFollow.isRefreshing = false
        srlMyMyFollow.isLoadingMore = false
        srlMyMyFollow.isRefreshEnabled = false
        srlMyMyFollow.isLoadMoreEnabled = false
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra("isDelete", mIsDelete)
        })
        super.onBackPressed()
    }
}