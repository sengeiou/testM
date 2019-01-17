package com.qingmeng.mengmeng.activity

/**
 * Created by fyf on 2019/1/8
 * 搜索页
 */
import android.view.View
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.CommonAdapter
import kotlinx.android.synthetic.main.layout_head_seach.*
import org.jetbrains.anko.startActivity

class RedShopSeach : BaseActivity() {
    private lateinit var mAdapter: CommonAdapter<String>
    override fun getLayoutId(): Int = R.layout.activity_red_shop_seach
    override fun initObject() {
        super.initObject()
        //头部  返回 隐藏  取消 显示
        head_search_mBack.visibility = View.GONE
        head_search_mMenu.visibility = View.VISIBLE
        initAdapter()
    }

    private fun initAdapter() {

    }

    override fun initListener() {
        super.initListener()
        head_search.setOnClickListener { startActivity<RedShopSeachResult>() }
        head_search_mMenu.setOnClickListener { this.finish() }
    }

    override fun initData() {
        super.initData()
    }
}
