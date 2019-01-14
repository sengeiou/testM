package com.qingmeng.mengmeng.activity

import android.support.v7.widget.LinearLayoutManager
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.TextView
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.CommonAdapter
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

    override fun getLayoutId(): Int {
        return R.layout.activity_my_myfollow
    }

    override fun initObject() {
        super.initObject()

        //设置标题
        if (intent.getStringExtra("title") == getString(R.string.my_myFollow)) {
            setHeadName(getString(R.string.my_myFollow))
        } else {//修改密码
            setHeadName(getString(R.string.my_myFootprint))
        }

        //适配器初始化
        initAdapter()

        srlMyMyFollow.isRefreshing = true
        //请求接口
        httpLoad()
    }

    override fun initListener() {
        super.initListener()

        //下拉刷新
        srlMyMyFollow.setOnRefreshListener {
            httpLoad()
        }

        //返回
        mBack.setOnClickListener {
            this.finish()
        }

        rvMyMyFollow.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                //SwipeMenuLayout关闭view
                SwipeMenuLayout.viewCache?.smoothClose()
            }
            false
        }
    }

    //适配器加载
    private fun initAdapter() {
        mLayoutManager = LinearLayoutManager(this)
        rvMyMyFollow.layoutManager = mLayoutManager
        mAdapter = CommonAdapter(this, R.layout.activity_my_myfollow_item, mList, holderConvert = { holder, t, position, payloads ->
            holder.apply {
                //glide加载图片
                GlideLoader.load(this@MyMyFollowActivity, t.logo, getView(R.id.ivMyMyFollowRvLogo), cacheType = CacheType.All)
                setText(R.id.tvMyMyFollowRvBrandName, t.name)
                setText(R.id.tvMyMyFollowRvCateringType, t.foodName)
                setText(R.id.tvMyMyFollowRvInvestmentAmount, t.capitalName)
                //item点击
                getView<LinearLayout>(R.id.llMyMyFollowRv).setOnClickListener {

                }
                //item取消关注
                getView<TextView>(R.id.tvMyMyFollowRvDelete).setOnClickListener {
                    //删除菜单关闭
                    getView<SwipeMenuLayout>(R.id.smlMyMyFollowRv).smoothClose()
                    ToastUtil.showShort("删除" + position)
                }
            }
        }, onItemClick = { view, holder, position ->

        })
        rvMyMyFollow.adapter = mAdapter
    }

    //我的关注列表接口请求
    private fun httpLoad() {
        ApiUtils.getApi()
                .myFollow(1, 1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    srlMyMyFollow.isRefreshing = false
                    //模拟点数据
                    setData()
                }, {
                    srlMyMyFollow.isRefreshing = false
                    setData()
                })
    }

    private fun setData() {
        mList.clear()
        for (i in 0 until 20) {
            mList.add(MyFollow("DRAGON" + i, "https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=3236989755,3566217273&fm=26&gp=0.jpg", "小吃", "0元加盟"))
        }
        mAdapter.notifyDataSetChanged()
    }
}