package com.qingmeng.mengmeng.adapter

import AppManager
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.mogujie.tt.config.DBConstant
import com.mogujie.tt.db.entity.MessageEntity
import com.mogujie.tt.db.entity.UserEntity
import com.mogujie.tt.imservice.entity.AudioMessage
import com.mogujie.tt.imservice.entity.ImageMessage
import com.mogujie.tt.imservice.entity.TextMessage
import com.mogujie.tt.imservice.entity.VideoMessage
import com.mogujie.tt.imservice.service.IMService
import com.mogujie.tt.ui.widget.message.RenderType
import com.mogujie.tt.utils.CommonUtil
import com.mogujie.tt.utils.DateUtil
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.utils.imageLoader.GlideLoader
import com.qingmeng.mengmeng.utils.setMarginExt
import java.util.*

/**
 *  Description :聊天多个样式的Adapter

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/2/14
 */
class ChatAdapter1(private val context: Context, private var msgObjectList: ArrayList<Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mImService: IMService? = null
    private var loginUser: UserEntity? = null

    override fun getItemViewType(position: Int): Int {
        return getItemViewType(msgObjectList[position])
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.let {
            when (RenderType.values()[getItemViewType(position)]) {
                RenderType.MESSAGE_TYPE_TIME_TITLE, RenderType.MESSAGE_TYPE_OTHER_REVOKE, RenderType.MESSAGE_TYPE_MINE_REVOKE -> {  //时间
                    (it as TimeViewHolder).bindViewHolder(msgObjectList[position])
                }
                RenderType.MESSAGE_TYPE_INVALID -> {    //品牌详情
                    (it as BrandViewHolder).bindViewHolder(msgObjectList[position])
                }
            /**
             * 别人消息
             */
                RenderType.MESSAGE_TYPE_OTHER_TEXT, RenderType.MESSAGE_TYPE_OTHER_GIF_IMAGE -> {    //文本(表情)
                    (it as OtherTextViewHolder).bindViewHolder(msgObjectList[position])
                }
                RenderType.MESSAGE_TYPE_OTHER_IMAGE, RenderType.MESSAGE_TYPE_OTHER_GIF -> { //图片(gif)
                    (it as OtherImageViewHolder).bindViewHolder(msgObjectList[position])
                }
                RenderType.MESSAGE_TYPE_OTHER_AUDIO -> {    //语音
                    (it as OtherAudioViewHolder).bindViewHolder(msgObjectList[position])
                }
                RenderType.MESSAGE_TYPE_OTHER_VIDEO -> {    //视频
                    (it as OtherVideoViewHolder).bindViewHolder(msgObjectList[position])
                }
            /**
             * 自己消息
             */
                RenderType.MESSAGE_TYPE_MINE_TEXT, RenderType.MESSAGE_TYPE_MINE_GIF_IMAGE -> {  //文本(表情)
                    (it as MineTextViewHolder).bindViewHolder(msgObjectList[position])
                }
                RenderType.MESSAGE_TYPE_MINE_IMAGE, RenderType.MESSAGE_TYPE_MINE_GIF -> {   //图片(gif)
                    (it as MineImageViewHolder).bindViewHolder(msgObjectList[position])
                }
                RenderType.MESSAGE_TYPE_MINE_AUDIO -> { //语音
                    (it as MineAudioViewHolder).bindViewHolder(msgObjectList[position])
                }
                RenderType.MESSAGE_TYPE_MINE_VIDEO -> { //视频
                    (it as MineVideoViewHolder).bindViewHolder(msgObjectList[position])
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return msgObjectList.size
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return when (RenderType.values()[viewType]) {
            RenderType.MESSAGE_TYPE_TIME_TITLE, RenderType.MESSAGE_TYPE_OTHER_REVOKE, RenderType.MESSAGE_TYPE_MINE_REVOKE -> {  //时间
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_time, viewGroup, false)
                TimeViewHolder(view)
            }
            RenderType.MESSAGE_TYPE_INVALID -> {    //品牌详情
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_brand, viewGroup, false)
                BrandViewHolder(view)
            }
        /**
         * 别人消息
         */
            RenderType.MESSAGE_TYPE_OTHER_TEXT, RenderType.MESSAGE_TYPE_OTHER_GIF_IMAGE -> {    //文本(表情)
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_other_text, viewGroup, false)
                OtherTextViewHolder(view)
            }
            RenderType.MESSAGE_TYPE_OTHER_IMAGE, RenderType.MESSAGE_TYPE_OTHER_GIF -> { //图片(gif)
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_other_image, viewGroup, false)
                OtherImageViewHolder(view)
            }
            RenderType.MESSAGE_TYPE_OTHER_AUDIO -> {    //语音
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_other_audio, viewGroup, false)
                OtherAudioViewHolder(view)
            }
            RenderType.MESSAGE_TYPE_OTHER_VIDEO -> {    //视频
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_other_video, viewGroup, false)
                OtherVideoViewHolder(view)
            }
        /**
         * 自己消息
         */
            RenderType.MESSAGE_TYPE_MINE_TEXT, RenderType.MESSAGE_TYPE_MINE_GIF_IMAGE -> {  //文本(表情)
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_mine_text, viewGroup, false)
                MineTextViewHolder(view)
            }
            RenderType.MESSAGE_TYPE_MINE_IMAGE, RenderType.MESSAGE_TYPE_MINE_GIF -> {   //图片(gif)
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_mine_image, viewGroup, false)
                MineImageViewHolder(view)
            }
            RenderType.MESSAGE_TYPE_MINE_AUDIO -> { //语音
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_mine_audio, viewGroup, false)
                MineAudioViewHolder(view)
            }
            RenderType.MESSAGE_TYPE_MINE_VIDEO -> { //视频
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_mine_video, viewGroup, false)
                MineVideoViewHolder(view)
            }
        }
    }

    /**
     * ---------------------------------------------ViewHolder---------------------------------------------
     */

    /**
     * 时间
     */
    inner class TimeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val llMyMessageChatRvAllTime = itemView.findViewById<LinearLayout>(R.id.llMyMessageChatRvAllTime)
        private val tvMyMessageChatRvTime = itemView.findViewById<TextView>(R.id.tvMyMessageChatRvTime)
        fun bindViewHolder(item: Any) {
            if (ChatAdapter.msgObjectList[0] == item) {
                llMyMessageChatRvAllTime.setMarginExt(top = 30)
            } else {
                llMyMessageChatRvAllTime.setMarginExt(top = 0)
            }
            when (RenderType.values()[getItemViewType(item)]) {
                RenderType.MESSAGE_TYPE_TIME_TITLE -> {    //时间
                    val timeBubble = item as Int
                    val timeStamp = timeBubble.toLong()
                    val msgTimeDate = Date(timeStamp * 1000)
                    tvMyMessageChatRvTime.text = DateUtil.getTimeDiffDesc(msgTimeDate)
                }
                RenderType.MESSAGE_TYPE_MINE_REVOKE -> {    //自己撤回
                    tvMyMessageChatRvTime.text = "对方撤回了一条消息"
                }
                RenderType.MESSAGE_TYPE_OTHER_REVOKE -> {    //别人撤回
                    tvMyMessageChatRvTime.text = "您撤回了一条消息"
                }
            }
        }
    }

    /**
     * 品牌详情
     */
    inner class BrandViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val llMyMessageChatRvAllBrand = itemView.findViewById<LinearLayout>(R.id.llMyMessageChatRvAllBrand)
        fun bindViewHolder(item: Any) {
            if (ChatAdapter.msgObjectList[0] == item) {
                llMyMessageChatRvAllBrand.setMarginExt(top = 30)
            } else {
                llMyMessageChatRvAllBrand.setMarginExt(top = 0)
            }
        }
    }

    /**
     * ---------------------------------------------别人消息---------------------------------------------
     */

    /**
     * 文本(表情)
     */
    inner class OtherTextViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivMyMessageChatRvOtherTextHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvOtherTextHead)
        private val tvMyMessageChatRvOtherTextText = itemView.findViewById<TextView>(R.id.tvMyMessageChatRvOtherTextText)
        fun bindViewHolder(item: Any) {
            val textMessage = item as TextMessage
            val userEntity = ChatAdapter.getUserEntity(textMessage)
            GlideLoader.load(AppManager.instance.currentActivity(), userEntity.avatar, ivMyMessageChatRvOtherTextHead, placeholder = R.mipmap.my_settings_aboutus_icon, roundRadius = 15)
            tvMyMessageChatRvOtherTextText.text = textMessage.info
        }
    }

    /**
     * 图片(gif)
     */
    inner class OtherImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivMyMessageChatRvOtherImageHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvOtherImageHead)
        private val ivMyMessageChatRvOtherImageImage = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvOtherImageImage)
        fun bindViewHolder(item: Any) {
            val imageMessage = item as ImageMessage
            val userEntity = ChatAdapter.getUserEntity(imageMessage)
            GlideLoader.load(AppManager.instance.currentActivity(), userEntity.avatar, ivMyMessageChatRvOtherImageHead, placeholder = R.mipmap.my_settings_aboutus_icon, roundRadius = 15)
            GlideLoader.load(AppManager.instance.currentActivity(), imageMessage.url, ivMyMessageChatRvOtherImageImage, roundRadius = 15)
        }
    }

    /**
     * 语音
     */
    inner class OtherAudioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivMyMessageChatRvOtherAudioHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvOtherAudioHead)
        private val tvMyMessageChatRvOtherAudioTime = itemView.findViewById<TextView>(R.id.tvMyMessageChatRvOtherAudioTime)
        fun bindViewHolder(item: Any) {
            val audioMessage = item as AudioMessage
            val userEntity = ChatAdapter.getUserEntity(audioMessage)
            GlideLoader.load(AppManager.instance.currentActivity(), userEntity.avatar, ivMyMessageChatRvOtherAudioHead, placeholder = R.mipmap.my_settings_aboutus_icon, roundRadius = 15)
            tvMyMessageChatRvOtherAudioTime.text = "${audioMessage.audiolength}\""
        }
    }

    /**
     * 视频
     */
    inner class OtherVideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivMyMessageChatRvOtherVideoHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvOtherVideoHead)
        private val ivMyMessageChatRvOtherVideoCover = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvOtherVideoCover)
        private val tvMyMessageChatRvOtherVideoTime = itemView.findViewById<TextView>(R.id.tvMyMessageChatRvOtherVideoTime)
        fun bindViewHolder(item: Any) {
            val videoMessage = item as VideoMessage
            val userEntity = ChatAdapter.getUserEntity(videoMessage)
            GlideLoader.load(AppManager.instance.currentActivity(), userEntity.avatar, ivMyMessageChatRvOtherVideoHead, placeholder = R.mipmap.my_settings_aboutus_icon, roundRadius = 15)
            //封面
            GlideLoader.load(AppManager.instance.currentActivity(), videoMessage.thumbUrl, ivMyMessageChatRvOtherVideoCover, roundRadius = 15)
//            //时间
//            tvMyMessageChatRvOtherVideoTime.text = ""
        }
    }

    /**
     * ---------------------------------------------自己消息---------------------------------------------
     */

    /**
     * 文本(表情)
     */
    inner class MineTextViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivMyMessageChatRvMineTextHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineTextHead)
        private val tvMyMessageChatRvMineTextText = itemView.findViewById<TextView>(R.id.tvMyMessageChatRvMineTextText)
        fun bindViewHolder(item: Any) {
            val textMessage = item as TextMessage
            val userEntity = ChatAdapter.getUserEntity(textMessage)
            GlideLoader.load(AppManager.instance.currentActivity(), userEntity.avatar, ivMyMessageChatRvMineTextHead, placeholder = R.mipmap.my_settings_aboutus_icon, roundRadius = 15)
            tvMyMessageChatRvMineTextText.text = textMessage.info
        }
    }

    /**
     * 图片(gif)
     */
    inner class MineImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivMyMessageChatRvMineImageHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineImageHead)
        private val ivMyMessageChatRvMineImageImage = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineImageImage)
        fun bindViewHolder(item: Any) {
            val imageMessage = item as ImageMessage
            val userEntity = ChatAdapter.getUserEntity(imageMessage)
            GlideLoader.load(AppManager.instance.currentActivity(), userEntity.avatar, ivMyMessageChatRvMineImageHead, placeholder = R.mipmap.my_settings_aboutus_icon, roundRadius = 15)
            GlideLoader.load(AppManager.instance.currentActivity(), imageMessage.url, ivMyMessageChatRvMineImageImage, roundRadius = 15)
        }
    }

    /**
     * 语音
     */
    inner class MineAudioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivMyMessageChatRvMineAudioHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineAudioHead)
        private val tvMyMessageChatRvMineAudioTime = itemView.findViewById<TextView>(R.id.tvMyMessageChatRvMineAudioTime)
        fun bindViewHolder(item: Any) {
            val audioMessage = item as AudioMessage
            val userEntity = ChatAdapter.getUserEntity(audioMessage)
            GlideLoader.load(AppManager.instance.currentActivity(), userEntity.avatar, ivMyMessageChatRvMineAudioHead, placeholder = R.mipmap.my_settings_aboutus_icon, roundRadius = 15)
            tvMyMessageChatRvMineAudioTime.text = "${audioMessage.audiolength}\""
        }
    }

    /**
     * 视频
     */
    inner class MineVideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivMyMessageChatRvMineVideoHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineVideoHead)
        private val ivMyMessageChatRvMineVideoCover = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineVideoCover)
        private val tvMyMessageChatRvMineVideoTime = itemView.findViewById<TextView>(R.id.tvMyMessageChatRvMineVideoTime)
        fun bindViewHolder(item: Any) {
            val videoMessage = item as VideoMessage
            val userEntity = ChatAdapter.getUserEntity(videoMessage)
            GlideLoader.load(AppManager.instance.currentActivity(), userEntity.avatar, ivMyMessageChatRvMineVideoHead, placeholder = R.mipmap.my_settings_aboutus_icon, roundRadius = 15)
            //封面
            GlideLoader.load(AppManager.instance.currentActivity(), videoMessage.thumbUrl, ivMyMessageChatRvMineVideoCover, roundRadius = 15)
//            //时间
//            tvMyMessageChatRvMineVideoTime.text = ""
        }
    }

    /**
     * 设置数据
     */
    fun setData(imService: IMService, userEntity: UserEntity) {
        this.mImService = imService
        this.loginUser = userEntity
    }

    /**
     * 获取消息类型
     */
    fun getItemViewType(obj: Any): Int {
        /**默认是失败类型 */
        var type = RenderType.MESSAGE_TYPE_INVALID
        if (obj is Int) {
            type = RenderType.MESSAGE_TYPE_TIME_TITLE
        } else if (obj is MessageEntity) {
            val isMine = obj.fromId == loginUser?.peerId
            when (obj.displayType) {
                DBConstant.SHOW_AUDIO_TYPE -> { //语音
                    type = if (isMine) {
                        RenderType.MESSAGE_TYPE_MINE_AUDIO
                    } else {
                        RenderType.MESSAGE_TYPE_OTHER_AUDIO
                    }
                }
                DBConstant.SHOW_IMAGE_TYPE -> { //图片
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
                DBConstant.SHOW_ORIGIN_TEXT_TYPE -> {   //文本
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
                DBConstant.SHOW_VIDEO_TYPE -> { //视频
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
}