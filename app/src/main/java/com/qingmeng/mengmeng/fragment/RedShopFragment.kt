package com.qingmeng.mengmeng.fragment

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.qingmeng.mengmeng.BaseFragment
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.activity.RedShopSeachResult
import com.qingmeng.mengmeng.adapter.CommonAdapter
import com.qingmeng.mengmeng.utils.ToastUtil
import kotlinx.android.synthetic.main.fragment_red_shop.*
import org.jetbrains.anko.support.v4.startActivity

class RedShopFragment : BaseFragment() {
    private lateinit var mLeftAdapter: CommonAdapter<String>
    private lateinit var mRightAdapter: CommonAdapter<String>
    private lateinit var mRightInAdapter: CommonAdapter<String>
    private lateinit var mLauyoutManger: LinearLayoutManager
    private lateinit var mGridLayoutManager: GridLayoutManager
    private var mLeftList = ArrayList<String>()
    private var mRightList = ArrayList<String>()
    private var mRightInList = ArrayList<String>()
    override fun getLayoutId(): Int = R.layout.fragment_red_shop
    override fun initObject() {
        super.initObject()
        initLeftAdapter()
        initRightAdapter()
        setData()
        setRightdata()
    }

    //加载   左边适配
    private fun initLeftAdapter() {
        mLauyoutManger = LinearLayoutManager(context)
        red_shop_left_recyclerview.layoutManager = mLauyoutManger
        mLeftAdapter = CommonAdapter(context!!, R.layout.item_red_shop_left, mLeftList, holderConvert = { holder, data, position, payloads ->
            holder.apply {
                //                getView<LinearLayout>(R.id.red_shop_left_lineralayout).setOnClickListener {
//
//                }
            }

        }, onItemClick = { view, holder, position ->

            ToastUtil.showShort("我是左边1级菜单")
            view.findViewById<TextView>(R.id.red_shop_left_textview).setBackgroundResource(R.drawable.ripple_bg_drawable_white)
        })
        red_shop_left_recyclerview.adapter = mLeftAdapter
    }

    private fun initRightAdapter() {
        mLauyoutManger = LinearLayoutManager(context)
        red_shop_right_recyclerview.layoutManager = mLauyoutManger
        mRightAdapter = CommonAdapter(context!!, R.layout.item_red_shop_right, mRightList, holderConvert = { holder, data, position, payloads ->
            holder.apply {
                if (mRightList.size > 1) {

                }

                getView<RecyclerView>(R.id.red_shop_right_inrecycler).apply {
                    mGridLayoutManager = GridLayoutManager(context, 3)
                    layoutManager = mGridLayoutManager
                    mRightInAdapter = CommonAdapter(context, R.layout.fragment_red_shop_right_in_item, mRightInList, holderConvert = { holder, data, position, payloads ->

                    }, onItemClick = { view, holder, position ->
                   startActivity<RedShopSeachResult>()
                    })
                    adapter = mRightInAdapter
                }
            }

        }, onItemClick = { view, holder, position ->

        })
        red_shop_right_recyclerview.adapter = mRightAdapter
    }

    private fun setData() {
        mLeftList.clear()
        for (i in 0 until 9) {
            mLeftList.add("")
        }
        mLeftAdapter.notifyDataSetChanged()
    }

    private fun setRightdata() {
        mRightList.clear()
        for (i in 0 until 5) {
            mRightList.add("")
        }
        for (i in 0 until 20) {
            mRightInList.add("")
        }

        mRightAdapter.notifyDataSetChanged()
    }

    override fun initData() {
        super.initData()
    }

    override fun initListener() {
        super.initListener()
    }
}