package com.qingmeng.mengmeng.view.dialog

import android.app.Activity
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.utils.getLoacalBitmap

/**
 *  Description :最近图片显示和发送图片

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/01/05
 */
class PopChatImg(activity: Activity, val imgUrl: String, private val isSeeImg: Boolean, private val onClick: (imgUrl: String) -> Unit) : PopupWindow(activity) {
    private var mMenuView: View = if (isSeeImg) {
        LayoutInflater.from(activity).inflate(R.layout.activity_my_message_chat_img_pop_see, null)
    } else {
        LayoutInflater.from(activity).inflate(R.layout.activity_my_message_chat_img_pop_send, null)
    }

    private fun initView() {
        if (isSeeImg) {
            mMenuView.findViewById<ImageView>(R.id.ivMyMessageChatSeePopScreenshot).setImageBitmap(getLoacalBitmap(imgUrl))
        } else {
            mMenuView.findViewById<ImageView>(R.id.ivMyMessageChatSendPopScreenshot).setImageBitmap(getLoacalBitmap(imgUrl))
        }
    }

    private fun initListener() {
        if (isSeeImg) {
            //图片点击
            mMenuView.findViewById<ImageView>(R.id.ivMyMessageChatSeePopScreenshot).setOnClickListener {
                onClick(imgUrl)
                dismiss()
            }
        } else {
            //发送按钮
            mMenuView.findViewById<TextView>(R.id.tvMyMessageChatSendPopSend).setOnClickListener {
                onClick(imgUrl)
                dismiss()
            }
        }
    }

    init {
        initView()
        initListener()

        this.contentView = mMenuView
        if(isSeeImg){
            this.width = ViewGroup.LayoutParams.WRAP_CONTENT
            this.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }else{
            this.width = ViewGroup.LayoutParams.MATCH_PARENT
            this.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        this.isFocusable = true
        this.setBackgroundDrawable(ColorDrawable(-0x00000000))
    }

    override fun showAsDropDown(anchor: View, xoff: Int, yoff: Int) {
        //解决7.0 showAsDropDowm  无效果适配
        if (Build.VERSION.SDK_INT == 24) {
            val rect = Rect()
            anchor.getGlobalVisibleRect(rect)
            val heigt = anchor.resources.displayMetrics.heightPixels - rect.bottom
            height = heigt
        }
        super.showAsDropDown(anchor, xoff, yoff)
    }
}