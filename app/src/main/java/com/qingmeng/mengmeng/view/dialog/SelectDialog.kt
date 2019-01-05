package com.qingmeng.mengmeng.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.CommonAdapter

/**
 *  Description :从下往上弹出的dialog

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/4
 */
class SelectDialog constructor(context: Context, val menuList: ArrayList<String>,
                               val onItemClick: (position: Int) -> Unit = { },
                               val onCalcelClick: (view: View) -> Unit = { },
                               theme: Int = R.style.dialog_common) : Dialog(context, theme) {
    private lateinit var mDialogView: View                      //dialog
    private lateinit var mTvCalcel: TextView                    //取消按钮
    private lateinit var mAdapterLayout: LinearLayoutManager    //布局管理器
    private lateinit var mRvSelect: RecyclerView                //菜单列表
    private lateinit var mAdapter: CommonAdapter<String>        //适配器

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

        initAdapter()
    }

    private fun initAdapter() {
        mAdapterLayout = LinearLayoutManager(context)
        mRvSelect.layoutManager = mAdapterLayout
        mAdapter = CommonAdapter(context, R.layout.view_dialog_select_item, menuList, holderConvert = { holder, it, position, _ ->
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
                } else {    //只要一个菜单
                    getView<View>(R.id.viewSelectDialogRvLine).visibility = View.GONE
                    getView<TextView>(R.id.tvSelectDialogRvMenu).setBackgroundResource(R.drawable.ripple_bg_white_radius5)
                }
                setText(R.id.tvSelectDialogRvMenu, it)
            }
        }, onItemClick = { _, _, position ->
            //点击把下标传过去
            onItemClick(position)
            dismiss()
        })
        mRvSelect.adapter = mAdapter
    }

    private fun initListener() {
        //取消按钮
        mTvCalcel.setOnClickListener {
            onCalcelClick(it)
            dismiss()
        }
    }
}