package com.qingmeng.mengmeng.activity

import android.support.v4.app.Fragment
import android.support.v4.view.PagerAdapter
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.CommonAdapter
import com.qingmeng.mengmeng.adapter.MyFragmentPagerAdapter
import com.qingmeng.mengmeng.fragment.MyMessageChatExpressionTabLayoutFragment
import com.qingmeng.mengmeng.utils.imageLoader.CacheType
import com.qingmeng.mengmeng.utils.imageLoader.GlideLoader
import kotlinx.android.synthetic.main.activity_my_message_chat.*
import kotlinx.android.synthetic.main.layout_head.*

/**
 *  Description :设置 - 消息 - 聊天界面

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/3
 */
class MyMessageChatActivity : BaseActivity() {
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var mAdapter: CommonAdapter<String>
    private lateinit var mPageAdapter: PagerAdapter
    private var mList = ArrayList<String>()
    private val mTabTitles = arrayOf("", "", "")    //tabLayout头部 先加3个试试
    private var mFragmentList = ArrayList<Fragment>()

    override fun getLayoutId(): Int {
        return R.layout.activity_my_message_chat
    }

    override fun initObject() {
        super.initObject()

        setHeadName(getString(R.string.mm_customerService))

        //表情里添加fragment
        mTabTitles.forEachIndexed { index, _ ->
            mFragmentList.add(MyMessageChatExpressionTabLayoutFragment())
            (mFragmentList[index] as MyMessageChatExpressionTabLayoutFragment).setContent("$index")
        }

        initAdapter()

//        .isRefreshing = true
//        httpLoad()
    }


    override fun initListener() {
        super.initListener()

        //返回
        mBack.setOnClickListener {
            this.finish()
        }

        //下拉刷新
//        .setOnRefreshListener {
//            httpLoad()
//        }
    }

    private fun initAdapter() {
        //消息适配器
        mLayoutManager = LinearLayoutManager(this)


        //表情viewPager适配器
        mPageAdapter = MyFragmentPagerAdapter(supportFragmentManager, mFragmentList, mTabTitles)
        vpMyMessageChat.adapter = mPageAdapter
        //设置ViewPager缓存为5
        vpMyMessageChat.offscreenPageLimit = 5
        //将ViewPager和TabLayout绑定
        tlMyMessageChat.setupWithViewPager(vpMyMessageChat)

        mTabTitles.forEachIndexed { index, _ ->
            //tabLayout里添加view
            val tabView = View.inflate(this, R.layout.view_tab_layout, null)
            GlideLoader.load(this, "https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=3236989755,3566217273&fm=26&gp=0.jpg", tabView.findViewById(R.id.ivChatExpressionIcon), cacheType = CacheType.All, centerCrop = false)
            tlMyMessageChat.getTabAt(index)?.customView = tabView
        }
    }

    //消息接口请求
    private fun httpLoad() {
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