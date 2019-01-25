package com.qingmeng.mengmeng.adapter

import AppManager
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.mogujie.tt.config.DBConstant
import com.mogujie.tt.db.entity.MessageEntity
import com.mogujie.tt.imservice.entity.ImageMessage
import com.mogujie.tt.imservice.entity.TextMessage
import com.mogujie.tt.imservice.entity.VideoMessage
import com.mogujie.tt.ui.widget.message.RenderType
import com.mogujie.tt.utils.CommonUtil
import com.mogujie.tt.utils.DateUtil
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.util.ItemViewDelegate
import com.qingmeng.mengmeng.adapter.util.ViewHolder
import com.qingmeng.mengmeng.utils.imageLoader.GlideLoader
import java.util.*

/**
 *  Description :聊天多个样式的Adapter

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/9
 */
class ChatAdapter {
    //时间布局
    class TimeLayout : ItemViewDelegate<Any> {
        private var renderType: RenderType? = null
        override fun getItemViewLayoutId(): Int {
            return R.layout.activity_my_message_chat_item_time
        }

        override fun isForViewType(item: Any, position: Int): Boolean {
            renderType = RenderType.values()[ChatAdapter.getItemViewType(item, false)]
            return renderType == RenderType.MESSAGE_TYPE_TIME_TITLE
                    || renderType == RenderType.MESSAGE_TYPE_OTHER_REVOKE
                    || renderType == RenderType.MESSAGE_TYPE_MINE_REVOKE
        }

        override fun convert(holder: ViewHolder, item: Any, position: Int, payloads: List<Any>?) {
            holder.apply {
                when (renderType) {
                    RenderType.MESSAGE_TYPE_TIME_TITLE -> {    //时间
                        val timeBubble = item as Int
                        val timeStamp = timeBubble as Long
                        val msgTimeDate = Date(timeStamp * 1000)
                        setText(R.id.tvMyMessageChatRvTime, DateUtil.getTimeDiffDesc(msgTimeDate))
                    }
                    RenderType.MESSAGE_TYPE_MINE_REVOKE -> {    //自己撤回
                        setText(R.id.tvMyMessageChatRvTime, "对方撤回了一条消息")
                    }
                    RenderType.MESSAGE_TYPE_OTHER_REVOKE -> {    //别人撤回
                        setText(R.id.tvMyMessageChatRvTime, "您撤回了一条消息")
                    }
                }
            }
        }
    }

    //品牌详情
    class BrandLayout : ItemViewDelegate<Any> {
        private var renderType: RenderType? = null
        override fun getItemViewLayoutId(): Int {
            return R.layout.activity_my_message_chat_item_brand
        }

        override fun isForViewType(item: Any, position: Int): Boolean {
            renderType = RenderType.values()[ChatAdapter.getItemViewType(item, false)]
            return renderType == RenderType.MESSAGE_TYPE_INVALID
        }

        override fun convert(holder: ViewHolder, item: Any, position: Int, payloads: List<Any>?) {
            holder.apply {

            }
        }
    }

    //别人消息
    class OtherLayout : ItemViewDelegate<Any> {
        private var renderType: RenderType? = null
        override fun getItemViewLayoutId(): Int {
            return R.layout.activity_my_message_chat_item_other
        }

        override fun isForViewType(item: Any, position: Int): Boolean {
            renderType = RenderType.values()[ChatAdapter.getItemViewType(item, false)]
            return renderType == RenderType.MESSAGE_TYPE_OTHER_AUDIO
                    || renderType == RenderType.MESSAGE_TYPE_OTHER_GIF_IMAGE
                    || renderType == RenderType.MESSAGE_TYPE_OTHER_IMAGE
                    || renderType == RenderType.MESSAGE_TYPE_OTHER_TEXT
                    || renderType == RenderType.MESSAGE_TYPE_OTHER_GIF
                    || renderType == RenderType.MESSAGE_TYPE_OTHER_VIDEO
                    || renderType == RenderType.MESSAGE_TYPE_OTHER_VIDEO
        }

        override fun convert(holder: ViewHolder, item: Any, position: Int, payloads: List<Any>?) {
            holder.apply {
                when (renderType) {
                    RenderType.MESSAGE_TYPE_OTHER_AUDIO -> {    //别人语音
                        ChatAdapter.setShowOtherView(holder, 3)
                    }
                    RenderType.MESSAGE_TYPE_OTHER_GIF_IMAGE -> {    //别人gif图
                        ChatAdapter.setShowOtherView(holder, 2)
                        val imageMessage = item as ImageMessage
                        GlideLoader.load(AppManager.instance, imageMessage.url, getView(R.id.ivMyMessageChatRvOtherImage), roundRadius = 15)
                    }
                    RenderType.MESSAGE_TYPE_OTHER_IMAGE -> {    //别人图片
                        ChatAdapter.setShowOtherView(holder, 2)
                        val imageMessage = item as ImageMessage
                        GlideLoader.load(AppManager.instance, imageMessage.url, getView(R.id.ivMyMessageChatRvOtherImage), roundRadius = 15)
                    }
                    RenderType.MESSAGE_TYPE_OTHER_TEXT -> {    //别人文字
                        ChatAdapter.setShowOtherView(holder, 1)
                        val textMessage = item as TextMessage
                        setText(R.id.tvMyMessageChatRvOtherText, textMessage.info)
                    }
                    RenderType.MESSAGE_TYPE_OTHER_GIF -> {    //别人表情
                        ChatAdapter.setShowOtherView(holder, 2)
                        val imageMessage = item as ImageMessage
                        GlideLoader.load(AppManager.instance, imageMessage.url, getView(R.id.ivMyMessageChatRvOtherImage), roundRadius = 15)
                    }
                    RenderType.MESSAGE_TYPE_OTHER_VIDEO -> {    //别人视频
                        ChatAdapter.setShowOtherView(holder, 4)
                        val videoMessage = item as VideoMessage
                        //设置封面
                        GlideLoader.load(AppManager.instance, videoMessage.thumbUrl, getView(R.id.ivMyMessageChatRvOtherVideoCover), roundRadius = 15)
                    }
                }
            }
        }
    }

    //自己消息
    class MineLayout : ItemViewDelegate<Any> {
        private var renderType: RenderType? = null
        override fun getItemViewLayoutId(): Int {
            return R.layout.activity_my_message_chat_item_mine
        }

        override fun isForViewType(item: Any, position: Int): Boolean {
            renderType = RenderType.values()[ChatAdapter.getItemViewType(item, false)]
            return renderType == RenderType.MESSAGE_TYPE_MINE_AUDIO
                    || renderType == RenderType.MESSAGE_TYPE_MINE_GIF_IMAGE
                    || renderType == RenderType.MESSAGE_TYPE_MINE_IMAGE
                    || renderType == RenderType.MESSAGE_TYPE_MINE_TEXT
                    || renderType == RenderType.MESSAGE_TYPE_MINE_GIF
                    || renderType == RenderType.MESSAGE_TYPE_MINE_VIDEO
                    || renderType == RenderType.MESSAGE_TYPE_MINE_VIDEO
        }

        override fun convert(holder: ViewHolder, item: Any, position: Int, payloads: List<Any>?) {
            holder.apply {
                when (renderType) {
                    RenderType.MESSAGE_TYPE_MINE_AUDIO -> {    //自己语音
                        ChatAdapter.setShowMineView(holder, 3)
                    }
                    RenderType.MESSAGE_TYPE_MINE_GIF_IMAGE -> {    //自己gif图
                        ChatAdapter.setShowMineView(holder, 2)
                        val imageMessage = item as ImageMessage
                        GlideLoader.load(AppManager.instance, imageMessage.url, getView(R.id.ivMyMessageChatRvMineImage), roundRadius = 15)
                    }
                    RenderType.MESSAGE_TYPE_MINE_IMAGE -> {    //自己图片
                        ChatAdapter.setShowMineView(holder, 2)
                        val imageMessage = item as ImageMessage
                        GlideLoader.load(AppManager.instance, imageMessage.url, getView(R.id.ivMyMessageChatRvMineImage), roundRadius = 15)
                    }
                    RenderType.MESSAGE_TYPE_MINE_TEXT -> {    //自己文字
                        ChatAdapter.setShowMineView(holder, 1)
                        val textMessage = item as TextMessage
                        setText(R.id.tvMyMessageChatRvMineText, textMessage.info)
                    }
                    RenderType.MESSAGE_TYPE_MINE_GIF -> {    //自己表情
                        ChatAdapter.setShowMineView(holder, 2)
                        val imageMessage = item as ImageMessage
                        GlideLoader.load(AppManager.instance, imageMessage.url, getView(R.id.ivMyMessageChatRvMineImage), roundRadius = 15)
                    }
                    RenderType.MESSAGE_TYPE_MINE_VIDEO -> {    //自己视频
                        ChatAdapter.setShowMineView(holder, 4)
                        val videoMessage = item as VideoMessage
                        //设置封面
                        GlideLoader.load(AppManager.instance, videoMessage.thumbUrl, getView(R.id.ivMyMessageChatRvMineVideoCover), roundRadius = 15)
                    }
                }
            }
        }
    }

    companion object {
        /**
         * 获取消息类型
         */
        fun getItemViewType(obj: Any, isMine: Boolean): Int {
            /**默认是失败类型 */
            var type = RenderType.MESSAGE_TYPE_INVALID
            if (obj is Int) {
                type = RenderType.MESSAGE_TYPE_TIME_TITLE
            } else if (obj is MessageEntity) {
                when (obj.displayType) {
                    DBConstant.SHOW_AUDIO_TYPE -> {
                        type = if (isMine) {
                            RenderType.MESSAGE_TYPE_MINE_AUDIO
                        } else {
                            RenderType.MESSAGE_TYPE_OTHER_AUDIO
                        }
                    }
                    DBConstant.SHOW_IMAGE_TYPE -> {
                        val imageMessage = obj as ImageMessage
                        if (CommonUtil.gifCheck(imageMessage.url)) {
                            type = if (isMine)
                                RenderType.MESSAGE_TYPE_MINE_GIF_IMAGE
                            else
                                RenderType.MESSAGE_TYPE_OTHER_GIF_IMAGE
                        } else {
                            type = if (isMine)
                                RenderType.MESSAGE_TYPE_MINE_IMAGE
                            else
                                RenderType.MESSAGE_TYPE_OTHER_IMAGE
                        }
                    }
                    DBConstant.SHOW_ORIGIN_TEXT_TYPE -> {
                        val txtMsg = obj as TextMessage
                        if (obj.isGIfEmo) {
                            type = if (isMine)
                                RenderType.MESSAGE_TYPE_MINE_GIF
                            else
                                RenderType.MESSAGE_TYPE_OTHER_GIF
                        } else if (txtMsg.contentEntity.infoType == DBConstant.SHOW_REVOKE_TYPE) {
                            type = if (isMine)
                                RenderType.MESSAGE_TYPE_MINE_REVOKE
                            else
                                RenderType.MESSAGE_TYPE_OTHER_REVOKE
                        } else {
                            type = if (isMine)
                                RenderType.MESSAGE_TYPE_MINE_TEXT
                            else
                                RenderType.MESSAGE_TYPE_OTHER_TEXT
                        }
                    }
                    DBConstant.SHOW_MIX_TEXT -> {
                    }
                    DBConstant.SHOW_VIDEO_TYPE -> {
                        type = if (isMine) {
                            RenderType.MESSAGE_TYPE_MINE_VIDEO
                        } else {
                            RenderType.MESSAGE_TYPE_OTHER_VIDEO
                        }
                    }
                    DBConstant.SHOW_REVOKE_TYPE -> {    //撤回
                        type = if (isMine) {
                            RenderType.MESSAGE_TYPE_MINE_REVOKE
                        } else {
                            RenderType.MESSAGE_TYPE_OTHER_REVOKE
                        }
                    }
                    else -> {
                    }
                }
            }
            return type.ordinal
        }

        //显示别人View方法    1.文本 2.图片 3.语音 4.视频
        private fun setShowOtherView(holder: ViewHolder, position: Int) {
            holder.apply {
                when (position) {
                    1 -> {
                        getView<TextView>(R.id.tvMyMessageChatRvOtherText).visibility = View.VISIBLE
                        getView<ImageView>(R.id.ivMyMessageChatRvOtherImage).visibility = View.GONE
                        getView<LinearLayout>(R.id.llMyMessageChatRvOtherAudio).visibility = View.GONE
                        getView<RelativeLayout>(R.id.rlMyMessageChatRvOtherVideo).visibility = View.GONE

                    }
                    2 -> {
                        getView<TextView>(R.id.tvMyMessageChatRvOtherText).visibility = View.GONE
                        getView<ImageView>(R.id.ivMyMessageChatRvOtherImage).visibility = View.VISIBLE
                        getView<LinearLayout>(R.id.llMyMessageChatRvOtherAudio).visibility = View.GONE
                        getView<RelativeLayout>(R.id.rlMyMessageChatRvOtherVideo).visibility = View.GONE

                    }
                    3 -> {
                        getView<TextView>(R.id.tvMyMessageChatRvOtherText).visibility = View.GONE
                        getView<ImageView>(R.id.ivMyMessageChatRvOtherImage).visibility = View.GONE
                        getView<LinearLayout>(R.id.llMyMessageChatRvOtherAudio).visibility = View.VISIBLE
                        getView<RelativeLayout>(R.id.rlMyMessageChatRvOtherVideo).visibility = View.GONE

                    }
                    4 -> {
                        getView<TextView>(R.id.tvMyMessageChatRvOtherText).visibility = View.GONE
                        getView<ImageView>(R.id.ivMyMessageChatRvOtherImage).visibility = View.GONE
                        getView<LinearLayout>(R.id.llMyMessageChatRvOtherAudio).visibility = View.GONE
                        getView<RelativeLayout>(R.id.rlMyMessageChatRvOtherVideo).visibility = View.VISIBLE

                    }
                }
            }
        }

        //显示自己View方法    1.文本 2.图片 3.语音 4.视频
        private fun setShowMineView(holder: ViewHolder, position: Int) {
            holder.apply {
                when (position) {
                    1 -> {
                        getView<TextView>(R.id.tvMyMessageChatRvMineText).visibility = View.VISIBLE
                        getView<ImageView>(R.id.ivMyMessageChatRvMineImage).visibility = View.GONE
                        getView<LinearLayout>(R.id.llMyMessageChatRvMineAudio).visibility = View.GONE
                        getView<RelativeLayout>(R.id.rlMyMessageChatRvMineVideo).visibility = View.GONE

                    }
                    2 -> {
                        getView<TextView>(R.id.tvMyMessageChatRvMineText).visibility = View.GONE
                        getView<ImageView>(R.id.ivMyMessageChatRvMineImage).visibility = View.VISIBLE
                        getView<LinearLayout>(R.id.llMyMessageChatRvMineAudio).visibility = View.GONE
                        getView<RelativeLayout>(R.id.rlMyMessageChatRvMineVideo).visibility = View.GONE

                    }
                    3 -> {
                        getView<TextView>(R.id.tvMyMessageChatRvMineText).visibility = View.GONE
                        getView<ImageView>(R.id.ivMyMessageChatRvMineImage).visibility = View.GONE
                        getView<LinearLayout>(R.id.llMyMessageChatRvMineAudio).visibility = View.VISIBLE
                        getView<RelativeLayout>(R.id.rlMyMessageChatRvMineVideo).visibility = View.GONE

                    }
                    4 -> {
                        getView<TextView>(R.id.tvMyMessageChatRvMineText).visibility = View.GONE
                        getView<ImageView>(R.id.ivMyMessageChatRvMineImage).visibility = View.GONE
                        getView<LinearLayout>(R.id.llMyMessageChatRvMineAudio).visibility = View.GONE
                        getView<RelativeLayout>(R.id.rlMyMessageChatRvMineVideo).visibility = View.VISIBLE

                    }
                }
            }
        }
    }
}
