package com.qingmeng.mengmeng.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.CommonAdapter
import com.qingmeng.mengmeng.adapter.LoadMoreWrapper
import com.qingmeng.mengmeng.adapter.util.FooterView
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.entity.MyFollow
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.utils.imageLoader.CacheType
import com.qingmeng.mengmeng.utils.imageLoader.GlideLoader
import com.qingmeng.mengmeng.utils.setFooterStatus
import com.qingmeng.mengmeng.view.SwipeMenuLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_my_myfollow.*
import kotlinx.android.synthetic.main.layout_head.*
import org.jetbrains.anko.startActivity

/**
 *  Description :设置 - 我的关注/我的足迹

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/3
 */
@SuppressLint("CheckResult")
class MyMyFollowActivity : BaseActivity() {
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var mAdapter: LoadMoreWrapper<MyFollow>
    private var mList = ArrayList<MyFollow>()
    private lateinit var mFootView: FooterView
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
        val title = intent.getStringExtra("title")
        setHeadName(title)
        mIsMyFollow = title == getString(R.string.my_myFollow)

        mFootView = FooterView(this)
        setFooterStatus(mFootView, 3)

        //适配器初始化
        initAdapter()

//        //自动刷新请求
//        srlMyMyFollow.isRefreshing = true
        myDialog.showLoadingDialog()
        httpLoad(1)
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
                        srlMyMyFollow.isLoadMoreEnabled = false
                    }
                    //!recyclerView.canScrollVertically(1)  //滑到最底部了
                    //没有滑动时 在最下面一个item
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE && mLayoutManager.findLastVisibleItemPosition() + 1 == mAdapter.itemCount) {
                    //如果下拉刷新没有刷新的话
                    if (!srlMyMyFollow.isRefreshing) {
                        if (mList.isNotEmpty()) {
                            //是否有下一页
                            if (mHasNextPage) {
                                //是否可以请求接口
                                if (mCanHttpLoad) {
                                    srlMyMyFollow.isRefreshEnabled = false
//                                    srlMyMyFollow.isLoadMoreEnabled = true
                                    httpLoad(mPageNum)
                                }
                            }
                        }
                    }
                } else {
//                    srlMyMyFollow.isRefreshEnabled = false
                    srlMyMyFollow.isLoadMoreEnabled = false
                }
            }
        })
    }

    //适配器加载
    private fun initAdapter() {
        mLayoutManager = LinearLayoutManager(this)
        rvMyMyFollow.layoutManager = mLayoutManager
        val adapter = CommonAdapter(this, R.layout.activity_my_myfollow_item, mList, holderConvert = { holder, t, position, payloads ->
            holder.apply {
                //glide加载图片
                GlideLoader.load(this@MyMyFollowActivity, t.logo, getView(R.id.ivMyMyFollowRvLogo), cacheType = CacheType.All, placeholder = R.drawable.default_img_icon)
                if (mIsMyFollow) {
                    setText(R.id.tvMyMyFollowRvBrandName, t.name)
                } else {
                    setText(R.id.tvMyMyFollowRvDelete, getString(R.string.delete))
                    setText(R.id.tvMyMyFollowRvBrandName, t.brandName)
                }
                setText(R.id.tvMyMyFollowRvCateringType, t.foodName)
                if (t.capitalName.isNullOrBlank()) {
                    setText(R.id.tvMyMyFollowRvInvestmentAmount, getString(R.string.face))
                } else {
                    setText(R.id.tvMyMyFollowRvInvestmentAmount, t.capitalName)
                }
                //item点击
                getView<LinearLayout>(R.id.llMyMyFollowRv).setOnClickListener {
                    //辨别是我的关注还是我的足迹
                    if (mIsMyFollow) {
                        startActivity<ShopDetailActivity>(IConstants.BRANDID to t.id)
                    } else {
                        startActivity<ShopDetailActivity>(IConstants.BRANDID to t.brandId)
                    }
                }
                //item取消关注
                getView<TextView>(R.id.tvMyMyFollowRvDelete).setOnClickListener {
                    //删除菜单关闭
                    getView<SwipeMenuLayout>(R.id.smlMyMyFollowRv).smoothClose()
                    httpDelLoadOne(mPageNum, t)
                }
            }
        })
        //为apdater嵌套一个划到底部加载更多
        mAdapter = LoadMoreWrapper(adapter)
        //加载更多布局
        mAdapter.setLoadMoreView(mFootView)
        rvMyMyFollow.adapter = mAdapter
    }

    //我的关注列表接口请求
    private fun httpLoad(pageNum: Int) {
        mCanHttpLoad = false
        ApiUtils.getApi().let {
            if (mIsMyFollow) {
                it.myFollow(pageNum, MainApplication.instance.TOKEN)
            } else {
                it.myFootprint(pageNum, MainApplication.instance.TOKEN)
            }
        }
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
                                mList.clear()
                                mPageNum = 1
                            }
                            //请求后判断里面数据
                            if (data == null || data?.data!!.isEmpty()) {
                                mHasNextPage = false
                                if (pageNum == 1) {
                                    //空白页提示
                                    if (mIsMyFollow) {
                                        setTipsText(getString(R.string.my_myFollow_null_tips))
                                    } else {
                                        setTipsText(getString(R.string.my_myFootprint_null_tips))
                                    }
                                    llMyMyFollowTips.visibility = View.VISIBLE
                                    srlMyMyFollow.isRefreshEnabled = true
                                    setFooterStatus(mFootView, 3)
                                } else {
                                    setFooterStatus(mFootView, 2)
                                }
                            } else {
                                mHasNextPage = true
                                if (pageNum == 1) {
                                    if (mIsMyFollow) {
                                        setTipsText(getString(R.string.my_myFollow_null_tips))
                                    } else {
                                        setTipsText(getString(R.string.my_myFootprint_null_tips))
                                    }
                                    llMyMyFollowTips.visibility = View.GONE
                                }
                                //把内容添加到mList里去
                                mList.addAll(data?.data!!)
                                mPageNum++
                                setFooterStatus(mFootView, 1)
                                //如果不满一个屏幕 就隐藏
                                if (mList.size < 15) {
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
                    llMyMyFollowTips.visibility = View.VISIBLE
                    srlMyMyFollow.isRefreshEnabled = true
                }, {}, { addSubscription(it) })
    }

    //取消关注接口 先把下一页的数据查出来传给删除方法
    private fun httpDelLoadOne(pageNum: Int, myFollowDel: MyFollow) {
        myDialog.showLoadingDialog()
        ApiUtils.getApi().let {
            if (mIsMyFollow) {
                it.myFollow(pageNum, MainApplication.instance.TOKEN)
            } else {
                it.myFootprint(pageNum, MainApplication.instance.TOKEN)
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
                }, {}, { addSubscription(it) })
    }

    //真.取消关注接口
    private fun httpDelLoadTwo(myFollowList: List<MyFollow>, myFollowDel: MyFollow) {
        ApiUtils.getApi().let {
            if (mIsMyFollow) {
                it.deleteMyFollow(myFollowDel.id, MainApplication.instance.TOKEN)
            } else {
                it.deleteMyFootprint(myFollowDel.brandId, MainApplication.instance.TOKEN)
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
                }, {}, { addSubscription(it) })
    }

    //用到的地方偏多 统一一下
    private fun setRefreshAsFalse() {
        srlMyMyFollow.isRefreshing = false
        srlMyMyFollow.isLoadingMore = false
        srlMyMyFollow.isRefreshEnabled = false
        srlMyMyFollow.isLoadMoreEnabled = false
    }

    //设置提示内容
    private fun setTipsText(tips: String) {
        llMyMyFollowTips.findViewById<TextView>(R.id.tvViewTips).text = tips
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra("isDelete", mIsDelete)
        })
        super.onBackPressed()
    }
}