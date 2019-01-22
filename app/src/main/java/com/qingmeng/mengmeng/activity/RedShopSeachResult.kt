package com.qingmeng.mengmeng.activity

/**
 * Created by fyf on 2019/1/8
 * 搜索结果页
 */
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.LinearLayout
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.CommonAdapter
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.view.dialog.PopSeachSelect
import kotlinx.android.synthetic.main.activity_red_shop_seach_result.*
import kotlinx.android.synthetic.main.layout_head_seach.*
import org.jetbrains.anko.startActivity
import java.util.*

class RedShopSeachResult : BaseActivity() {
    private lateinit var mAdapter: CommonAdapter<String>
    private lateinit var mLauyoutManger: LinearLayoutManager
    private lateinit var popupMenu1: PopSeachSelect
    private lateinit var popupMenu2: PopSeachSelect
    private lateinit var popupMenu3: PopSeachSelect
    private lateinit var leftRecyclerView: RecyclerView
    private lateinit var rightRecyclerView: RecyclerView
    private var mList = ArrayList<String>()
    private var mIsInstantiationOne = false
    private var mIsInstantiationTwo = false
    private var mIsInstantiationThree = false


    override fun getLayoutId(): Int = R.layout.activity_red_shop_seach_result

    override fun initObject() {
        super.initObject()
        initAdapter()
        setData()
        initPop()
    }

    private fun initPop() {

    }

    private fun initAdapter() {
        //搜索结果 Adapter
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
            var LeftList = arrayListOf<String>("全部", "火锅", "烧烤", "快餐", "西餐", "麻辣烫", "小面", "拉面", "日韩料理", "甜品蛋糕")
            var RightList = arrayListOf<String>("砂锅", "米线", "馄饨", "炸酱面", "花甲粉", "重庆小面", "拉面", "包子")
            if (!mIsInstantiationOne) {
                popupMenu1 = PopSeachSelect(this, LeftList, RightList, 1)
            }
            popupMenu1.showAsDropDown(search_result_view)
//            popupMenu1.showAtLocation(search_food_type, Gravity.CENTER, 0, 0)
            mIsInstantiationOne = true
            setShow(1)
        }
        search_add_area.setOnClickListener {
            var LeftList = arrayListOf<String>("全国", "北京", "上海", "广州", "深圳", "杭州", "西安", "郑州", "大连", "武汉")
            var RightList = arrayListOf<String>("徐汇区", "静安区", "松江区", "长宁区", "浦东新区", "黄浦区", "虹口区", "闵行区")
            if (!mIsInstantiationTwo) {
                popupMenu2 = PopSeachSelect(this, LeftList, RightList, 2)
            }
            popupMenu2.showAsDropDown(search_result_view)
//            popupMenu2.showAtLocation(search_food_type, Gravity.CENTER, 0, 0)
            mIsInstantiationTwo = true
            setShow(2)
        }
        search_ranking.setOnClickListener {
            var LeftList = arrayListOf<String>("综合排序", "人气优先", "留言优先", "低价优先", "高价优先")
            var RightList = arrayListOf<String>()
            if (!mIsInstantiationThree) {
                popupMenu3 = PopSeachSelect(this, LeftList, RightList, 3)
            }
            popupMenu3.showAsDropDown(search_result_view)
//            popupMenu3.showAtLocation(search_food_type, Gravity.CENTER, 0, 0)
            mIsInstantiationThree = true
            setShow(3)
        }
        search_screning_conditon.setOnClickListener { }
    }

    private fun setShow(position: Int) {
        when (position) {
            1 -> {
                //避免点击之后另外两个窗口还存在情况
                if (mIsInstantiationTwo) {
                    popupMenu2.dismiss()
                }
                if (mIsInstantiationThree) {
                    popupMenu3.dismiss()
                }
//                //实现连续点击
//                if (!popupMenu1.isShowing) {
//                    ToastUtil.showShort("连续点击1")
//                    popupMenu1.showAsDropDown(search_result_view)
////                    popupMenu1.showAtLocation(search_food_type, Gravity.CENTER, 0, 0)
//                } else {
//                    ToastUtil.showShort("连续点击2")
//                    popupMenu1.dismiss()
//
//                }
            }
            2 -> {
                if (mIsInstantiationOne) {
                    popupMenu1.dismiss()
                }
                if (mIsInstantiationThree) {
                    popupMenu3.dismiss()
                }
//                if (mIsInstantiationTwo) {
//                    ToastUtil.showShort("连续点击1")
//                    popupMenu2.showAsDropDown(search_result_view)
//                    // popupMenu2.showAtLocation(search_food_type, Gravity.CENTER, 0, 0)
//                } else {
//                    ToastUtil.showShort("连续点击2")
//                    popupMenu2.dismiss()
//                }
            }
            3 -> {
                if (mIsInstantiationOne) {
                    popupMenu1.dismiss()
                }
                if (mIsInstantiationTwo) {
                    popupMenu2.dismiss()
                }
//                if (popupMenu3.isShowing) {
//                    popupMenu3.dismiss()
//                } else {
//                    popupMenu3.showAsDropDown(search_result_view)
////                    popupMenu3.showAtLocation(search_food_type, Gravity.CENTER, 0, 0)
//
//                }
            }
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
