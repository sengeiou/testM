package com.qingmeng.mengmeng.view.dialog

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.CommonAdapter
import com.qingmeng.mengmeng.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_common_pop_window.view.*
import org.jetbrains.anko.textColor

/**
 * Created by fyf on 2019/1/16
 * 搜索结果筛选菜单PopWindow
 */
class PopSeachSelect : PopupWindow {
    private var mLeftList = ArrayList<String>()
    private var mRightList = ArrayList<String>()
    private var mActivity: Activity
    private var mMenuView: View
    private lateinit var mLauyoutManger: LinearLayoutManager
    private lateinit var mLeftAdapter: CommonAdapter<String>
    private lateinit var mRightAdapter: CommonAdapter<String>
    private lateinit var mGridManager: GridLayoutManager
    //1 为餐饮类型  2为加盟区域 3 为综合排序 4 为筛选条件
    private var type: Int = 1

    constructor(mActivity: Activity, mLeftList: ArrayList<String>, mRightList: ArrayList<String>, type: Int) : super(mActivity) {
        this.mActivity = mActivity
        this.mLeftList = mLeftList
        this.mRightList = mRightList
        this.type = type
        mMenuView = LayoutInflater.from(mActivity).inflate(R.layout.activity_common_pop_window, null)
        initListener()
        initLeftAdapter()
        initRightAdapter()

        this.contentView = mMenuView
        this.width = ViewGroup.LayoutParams.MATCH_PARENT
        this.height = ViewGroup.LayoutParams.WRAP_CONTENT
        this.animationStyle = R.style.TopPopWindow_animStyle
//        this.isFocusable = true
        this.setBackgroundDrawable(ColorDrawable(-0x00000000))

//        //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
//        mMenuView.setOnTouchListener { _, event ->
//            val height = mMenuView.search_popupWindow.top
//            val y = event.y
//            if (event.action == MotionEvent.ACTION_UP) {
//                if (y < height) {
//                    dismiss()
//                }
//            }
//            true
//        }
        mMenuView.bottom_view.setOnClickListener {
            dismiss()
        }
    }

    private fun initListener() {

    }

    //加载   左边适配
    private fun initLeftAdapter() {
        mLauyoutManger = LinearLayoutManager(mActivity)
        mMenuView.left_recyclerView_pop.layoutManager = mLauyoutManger
        mLeftAdapter = CommonAdapter(mActivity, R.layout.seach_result_left_item, mLeftList, holderConvert = { holder, data, position, payloads ->
            holder.apply {
                setText(R.id.search_result_pop_left_item, data)
            }

        }, onItemClick = { view, holder, position ->
            ToastUtil.showShort("我是" + mLeftList[position])
            view.findViewById<TextView>(R.id.search_result_pop_left_item).textColor = R.color.color_5ab1e1
        })
        mMenuView.left_recyclerView_pop.adapter = mLeftAdapter
    }

    private fun initRightAdapter() {
        if (type == 1) {
            mGridManager = GridLayoutManager(mActivity, 3)
            mMenuView.right_recyclerView_pop.layoutManager = mGridManager
            mRightAdapter = CommonAdapter(mActivity, R.layout.fragment_red_shop_right_in_item, mRightList, holderConvert = { holder, data, position, payloads ->
                holder.apply {
                    setText(R.id.red_shop_right_inContent, data)
                }
            }, onItemClick = { view, holder, position ->

            })
            mMenuView.right_recyclerView_pop.adapter = mRightAdapter
        } else if (type == 2) {
            mLauyoutManger = LinearLayoutManager(mActivity)
            mMenuView.right_recyclerView_pop.layoutManager = mLauyoutManger
            mLeftAdapter = CommonAdapter(mActivity, R.layout.seach_result_left_item, mRightList, holderConvert = { holder, data, position, payloads ->
                holder.apply {
                    setText(R.id.search_result_pop_left_item, data)
                }

            }, onItemClick = { view, holder, position ->
                ToastUtil.showShort("我是" + mLeftList[position])
                view.findViewById<TextView>(R.id.search_result_pop_left_item).textColor = R.color.color_5ab1e1
            })
            mMenuView.right_recyclerView_pop.adapter = mLeftAdapter
        } else if (type == 3) {

        }
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int, gravity: Int) {
        super.showAsDropDown(anchor, xoff, yoff, gravity)
//        backgroundAlphaExt(0.5f)
        mMenuView.bottom_view.visibility = View.VISIBLE
    }

    override fun dismiss() {
        super.dismiss()
//        backgroundAlphaExt(1f)
        mMenuView.bottom_view.visibility = View.GONE
    }

//    //改变背景亮度
//    private fun backgroundAlphaExt(bgAlpha: Float) {
//        val lp = mActivity.window.attributes
//        //0.0-1.0
//        lp?.alpha = bgAlpha
//        mActivity.window.attributes = lp
//    }
}