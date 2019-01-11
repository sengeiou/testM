package com.qingmeng.mengmeng.activity

/**
 * Created by fyf on 2019/1/8
 * 搜索结果页
 */
import android.support.v7.widget.LinearLayoutManager
import android.widget.LinearLayout
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.CommonAdapter
import com.qingmeng.mengmeng.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_red_shop_seach_result.*
import kotlinx.android.synthetic.main.layout_head_seach.*
import org.jetbrains.anko.startActivity

class RedShopSeachResult : BaseActivity() {
    private lateinit var mAdapter: CommonAdapter<String>
    private lateinit var mLauyoutManger: LinearLayoutManager
    private var mList = ArrayList<String>()
    override fun getLayoutId(): Int = R.layout.activity_red_shop_seach_result

    override fun initObject() {
        super.initObject()
        initAdapter()
        setData()
    }

    //搜索结果 Adapter
    private fun initAdapter() {
        mLauyoutManger = LinearLayoutManager(this)
        search_result_recylerview.layoutManager = mLauyoutManger
        mAdapter = CommonAdapter(this, R.layout.item_red_shop_search_result, mList, holderConvert = { holder, data, position, payloads ->
            holder.apply {
                getView<LinearLayout>(R.id.search_linearlayout).setOnClickListener {
                    ToastUtil.showShort("我是搜索之后的详情页")
                }
            }

        }, onItemClick = { view, holder, position ->

        })
        search_result_recylerview.adapter = mAdapter
    }

    override fun initData() {
        super.initData()

    }

    override fun initListener() {
        super.initListener()
        head_search.setOnClickListener { startActivity<RedShopSeach>() }
        head_search_mBack.setOnClickListener { this.finish() }
    }

    private fun setData() {
        mList.clear()
        for (i in 0 until 20) {
            mList.add("")
        }
        mAdapter.notifyDataSetChanged()
    }
}
