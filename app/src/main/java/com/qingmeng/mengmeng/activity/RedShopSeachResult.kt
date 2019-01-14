package com.qingmeng.mengmeng.activity

/**
 * Created by fyf on 2019/1/8
 * 搜索结果页
 */
import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupMenu
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.CommonAdapter
import com.qingmeng.mengmeng.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_common_pop_window.*
import kotlinx.android.synthetic.main.activity_red_shop_seach_result.*
import kotlinx.android.synthetic.main.layout_head_seach.*
import org.jetbrains.anko.startActivity

class RedShopSeachResult : BaseActivity() {
    private lateinit var mAdapter: CommonAdapter<String>
    private lateinit var mLauyoutManger: LinearLayoutManager
    private lateinit var popupMenu: PopupMenu
    private lateinit var leftRecyclerView: RecyclerView
    private lateinit var rightRecyclerView: RecyclerView
    private var dataList = ArrayList<String>()
    private var mList = ArrayList<String>()

    override fun getLayoutId(): Int = R.layout.activity_red_shop_seach_result

    override fun initObject() {
        super.initObject()
        initAdapter()
        setData()
        initPopMenu()
    }

    private fun initPopMenu() {
        //popupWindow背景
        ContextCompat.getDrawable(this, R.drawable.bg_5_f5f5f5)
        //初始化popupWindow中的RecyclerView
        var view: View = LayoutInflater.from(this).inflate(R.layout.activity_common_pop_window, null)
        leftRecyclerView = view.findViewById(R.id.left_recyclerView_pop)
        rightRecyclerView = view.findViewById(R.id.right_recyclerView_pop)
        popupMenu = PopupMenu(this,view)

    }

    //搜索结果 Adapter
    private fun initAdapter() {
        mLauyoutManger = LinearLayoutManager(this)
        search_result_recylerview.layoutManager = mLauyoutManger
        mAdapter = CommonAdapter(this, R.layout.red_shop_search_result_item, mList, holderConvert = { holder, data, position, payloads ->
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
        search_food_type.setOnClickListener {
            //            if (popupWindow.visibility == View.VISIBLE) {
//                popupWindow.visibility = View.GONE
//            } else if (popupWindow.visibility == View.GONE) {
//                popupWindow.visibility = View.VISIBLE
//            }

        }

    }

    private fun setData() {
        mList.clear()
        for (i in 0 until 20) {
            mList.add("")
        }
        mAdapter.notifyDataSetChanged()
    }
}
