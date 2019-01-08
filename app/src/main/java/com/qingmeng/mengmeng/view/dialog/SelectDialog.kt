package com.qingmeng.mengmeng.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.CommonAdapter
import com.qingmeng.mengmeng.entity.SelectDialogBean

/**
 *  Description :从下往上弹出的dialog

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/4
 */
class SelectDialog constructor(context: Context, val menuList: ArrayList<SelectDialogBean>, val isDefaultLayout: Boolean = true,
                               val onItemClick: (position: Int) -> Unit = { },
                               val onCalcelClick: (view: View) -> Unit = { },
                               val onOtherDismiss: (menuList: ArrayList<SelectDialogBean>) -> Unit = {},
                               theme: Int = R.style.dialog_common) : Dialog(context, theme) {
    private lateinit var mDialogView: View                          //dialog
    private lateinit var mTvCalcel: TextView                        //取消按钮
    private lateinit var mLayoutManager: LinearLayoutManager        //布局管理器
    private lateinit var mmGridLayoutManager: GridLayoutManager     //布局管理器
    private lateinit var mRvSelect: RecyclerView                    //菜单列表
    private lateinit var mAdapter: CommonAdapter<SelectDialogBean>  //适配器
    private var mIsCancel = false                                   //变量 是否是点击取消调的dismiss()方法

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDialogView = View.inflate(context, R.layout.view_dialog_select, null)
        setContentView(mDialogView)
        initView()
        initListener()
    }

    private fun initView() {
        mTvCalcel = mDialogView.findViewById(R.id.tvCancel)
        mRvSelect = mDialogView.findViewById(R.id.rvSelect)

        val window = window
        val wlp = window.attributes
        //设置位置和动画
        wlp.gravity = Gravity.BOTTOM
        wlp.windowAnimations = R.style.bottomDialog_animStyle
        window.attributes = wlp

        if (isDefaultLayout) {
            mRvSelect.background = null
        } else {
            mRvSelect.setPadding(15, 15, 15, 15)
            mRvSelect.setBackgroundResource(R.drawable.ripple_bg_drawable_white_radius5)
        }

        initAdapter()
    }

    private fun initAdapter() {
        //默认竖向排布
        if (isDefaultLayout) {
            mLayoutManager = LinearLayoutManager(context)
            mRvSelect.layoutManager = mLayoutManager
            mAdapter = CommonAdapter(context, R.layout.view_dialog_select_item, menuList, holderConvert = { holder, t, position, _ ->
                holder.apply {
                    //如果不止一个
                    if (menuList.size > 1) {
                        //按位置设置背景
                        if (position == 0) {
                            getView<View>(R.id.viewSelectDialogRvLine).visibility = View.GONE
                            getView<TextView>(R.id.tvSelectDialogRvMenu).setBackgroundResource(R.drawable.ripple_bg_white_top_radius5)
                        } else if (position == menuList.lastIndex) {
                            getView<View>(R.id.viewSelectDialogRvLine).visibility = View.VISIBLE
                            getView<TextView>(R.id.tvSelectDialogRvMenu).setBackgroundResource(R.drawable.ripple_bg_white_bottom_radius5)
                        } else {
                            getView<View>(R.id.viewSelectDialogRvLine).visibility = View.VISIBLE
                            getView<TextView>(R.id.tvSelectDialogRvMenu).setBackgroundResource(R.drawable.ripple_bg_white)
                        }
                    } else {    //只有一个菜单
                        getView<View>(R.id.viewSelectDialogRvLine).visibility = View.GONE
                        getView<TextView>(R.id.tvSelectDialogRvMenu).setBackgroundResource(R.drawable.ripple_bg_white_radius5)
                    }
                    setText(R.id.tvSelectDialogRvMenu, t.menu)
                }
            }, onItemClick = { _, _, position ->
                //点击把下标传过去
                onItemClick(position)
                dismiss()
            })
        } else {  //表格排布
            mmGridLayoutManager = GridLayoutManager(context, 3)
            mRvSelect.layoutManager = mmGridLayoutManager
            mAdapter = CommonAdapter(context, R.layout.view_dialog_choose_item, menuList, holderConvert = { holder, it, _, _ ->
                holder.apply {
                    getView<RelativeLayout>(R.id.rlSelectDialogRvMenu).apply {
                        if (it.checkState) {
                            setBackgroundResource(R.mipmap.view_choose_bg)
                        } else {
                            setBackgroundResource(R.color.dialog_item_bg)
                        }
                    }
                    setText(R.id.tvSelectDialogRvMenu, it.menu)
                }
            }, onItemClick = { _, _, position ->
                menuList[position].let {
                    it.checkState = !it.checkState
                }
                mAdapter.notifyDataSetChanged()
            })
        }
        mRvSelect.adapter = mAdapter
    }

    private fun initListener() {
        //取消按钮
        mTvCalcel.setOnClickListener {
            onCalcelClick(it)
            mIsCancel = true
            dismiss()
        }
    }

    //重写dismiss方法 如果是点击屏幕或按返回键 就返回一个回调
    override fun dismiss() {
        super.dismiss()

        //不是点击取消取消的
        if (!mIsCancel) {
            onOtherDismiss(menuList)
        }
    }
}