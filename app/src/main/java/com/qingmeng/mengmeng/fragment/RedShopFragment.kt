package com.qingmeng.mengmeng.fragment

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.qingmeng.mengmeng.BaseFragment
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.activity.RedShopSeachResult
import com.qingmeng.mengmeng.adapter.CommonAdapter
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.utils.dp2px
import com.qingmeng.mengmeng.utils.getBarHeight
import com.qingmeng.mengmeng.utils.setMarginExt
import kotlinx.android.synthetic.main.fragment_red_shop.*
import kotlinx.android.synthetic.main.layout_head.*
import org.jetbrains.anko.support.v4.startActivity

class RedShopFragment : BaseFragment() {
    private lateinit var mLeftAdapter: CommonAdapter<String>
    private lateinit var mRightAdapter: CommonAdapter<String>
    private lateinit var mRightInAdapter: CommonAdapter<String>
    private lateinit var mLauyoutManger: LinearLayoutManager
    private lateinit var mGridLayoutManager: GridLayoutManager
    private var mLeftList = arrayListOf<String>("火锅", "烧烤", "快餐", "西餐", "麻辣烫", "小面", "拉面", "日韩料理", "甜品蛋糕")
    private var mRightList = arrayListOf<String>("分类", "热门品牌")
    private var mRightInList = arrayListOf<String>("砂锅", "米线", "馄饨", "炸酱面", "花甲粉", "重庆小面", "拉面", "包子")
    override fun getLayoutId(): Int = R.layout.fragment_red_shop
    override fun initObject() {
        super.initObject()
        mTitle.setText(R.string.tab_name_red_shop)
        // 获得状态栏高度
        val statusBarHeight = getBarHeight(context!!)
        //给布局的高度重新设置一下 加上状态栏高度
        mTopView.layoutParams.height = mTopView.layoutParams.height + getBarHeight(context!!)
        mTitle.setMarginExt(top = statusBarHeight + context!!.dp2px(60))
        mBack.visibility = View.GONE

        initLeftAdapter()
        initRightAdapter()
//        setData()
//        setRightdata()
    }

    //加载   左边适配
    private fun initLeftAdapter() {
        mLauyoutManger = LinearLayoutManager(context)
        red_shop_left_recyclerview.layoutManager = mLauyoutManger
        mLeftAdapter = CommonAdapter(context!!, R.layout.red_shop_left_item, mLeftList, holderConvert = { holder, data, position, payloads ->
            holder.apply {
                setText(R.id.red_shop_left_textview, data)
                //                getView<LinearLayout>(R.id.red_shop_left_lineralayout).setOnClickListener {
//
//                }
            }

        }, onItemClick = { view, holder, position ->

            ToastUtil.showShort("我是" + mLeftList[position])
            view.findViewById<TextView>(R.id.red_shop_left_textview).setBackgroundResource(R.drawable.ripple_bg_drawable_white)
        })
        red_shop_left_recyclerview.adapter = mLeftAdapter
    }

    private fun initRightAdapter() {
        mLauyoutManger = LinearLayoutManager(context)
        red_shop_right_recyclerview.layoutManager = mLauyoutManger
        mRightAdapter = CommonAdapter(context!!, R.layout.red_shop_right_item, mRightList, holderConvert = { holder, data, position, payloads ->
            holder.apply {
                setText(R.id.red_shop_right_tittle, data)
                getView<RecyclerView>(R.id.red_shop_right_inrecycler).apply {
                    mGridLayoutManager = GridLayoutManager(context, 3)
                    layoutManager = mGridLayoutManager
                    mRightInAdapter = CommonAdapter(context, R.layout.fragment_red_shop_right_in_item, mRightInList, holderConvert = { holder, data, position, payloads ->
                        holder.apply {
                            setText(R.id.red_shop_right_inContent, data)
                        }
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

//    private fun setData() {
//        mLeftList.clear()
//        for (i in 0 until 9) {
//            mLeftList.add("")
//        }
//        mLeftAdapter.notifyDataSetChanged()
//    }
//
//    private fun setRightdata() {
//        mRightList.clear()
//        for (i in 0 until 5) {
//            mRightList.add("")
//        }
//        for (i in 0 until 20) {
//            mRightInList.add("")
//        }
//
//        mRightAdapter.notifyDataSetChanged()
//    }

    override fun initData() {
        super.initData()
    }

    override fun initListener() {
        super.initListener()

    }
}