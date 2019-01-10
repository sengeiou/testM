package com.qingmeng.mengmeng.adapter

import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.util.ItemViewDelegate
import com.qingmeng.mengmeng.adapter.util.ViewHolder

/**
 *  Description :聊天多个样式的Adapter

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/9
 */
class ChatAdapter {
    //时间布局
    class TimeLayout : ItemViewDelegate<Int> {
        override fun getItemViewLayoutId(): Int {
            return R.layout.activity_my_message_chat_item_time
        }

        override fun isForViewType(item: Int, position: Int): Boolean {
            return item == ChatType.CHAT_TYPE_TIME
        }

        override fun convert(holder: ViewHolder, item: Int, position: Int, payloads: List<Any>?) {

        }

    }

    //品牌详情
    class BrandLayout : ItemViewDelegate<Int> {
        override fun getItemViewLayoutId(): Int {
            return R.layout.activity_my_message_chat_item_brand
        }

        override fun isForViewType(item: Int, position: Int): Boolean {
            return item == ChatType.CHAT_TYPE_BRAND
        }

        override fun convert(holder: ViewHolder, item: Int, position: Int, payloads: List<Any>?) {

        }

    }

    //自己消息
    class MineLayout : ItemViewDelegate<Int> {
        override fun getItemViewLayoutId(): Int {
            return R.layout.activity_my_message_chat_item_mine
        }

        override fun isForViewType(item: Int, position: Int): Boolean {
            return item == ChatType.CHAT_TYPE_MINE
        }

        override fun convert(holder: ViewHolder, item: Int, position: Int, payloads: List<Any>?) {

        }

    }

    //别人消息
    class OtherLayout : ItemViewDelegate<Int> {
        override fun getItemViewLayoutId(): Int {
            return R.layout.activity_my_message_chat_item_other
        }

        override fun isForViewType(item: Int, position: Int): Boolean {
            return item == ChatType.CHAT_TYPE_OTHER
        }

        override fun convert(holder: ViewHolder, item: Int, position: Int, payloads: List<Any>?) {

        }

    }
}