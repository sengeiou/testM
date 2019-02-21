package com.qingmeng.mengmeng.adapter

import AppManager
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.lemo.emojcenter.utils.EmotionUtils
import com.lemo.emojcenter.utils.SpanStringUtils
import com.mogujie.tt.config.DBConstant
import com.mogujie.tt.config.IntentConstant
import com.mogujie.tt.config.MessageConstant
import com.mogujie.tt.config.MessageExtConst
import com.mogujie.tt.db.DBInterface
import com.mogujie.tt.db.entity.MessageEntity
import com.mogujie.tt.db.entity.UserEntity
import com.mogujie.tt.imservice.entity.*
import com.mogujie.tt.imservice.service.IMService
import com.mogujie.tt.ui.activity.PreviewMessageImagesActivity
import com.mogujie.tt.ui.helper.AudioPlayerHandler
import com.mogujie.tt.ui.helper.Emoparser
import com.mogujie.tt.ui.widget.SpeekerToast
import com.mogujie.tt.ui.widget.message.MessageOperatePopup
import com.mogujie.tt.ui.widget.message.RenderType
import com.mogujie.tt.utils.CommonUtil
import com.mogujie.tt.utils.DateUtil
import com.mogujie.tt.utils.FileUtil
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.utils.imageLoader.GlideLoader
import com.qingmeng.mengmeng.utils.setMarginExt
import java.io.File
import java.util.*

/**
 *  Description :聊天多个样式的Adapter

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/2/14
 */
class ChatAdapterTwo(private val context: Context, var msgObjectList: ArrayList<Any>, val audioClick: (audioMessage: AudioMessage) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mImService: IMService? = null
    private var loginUser: UserEntity? = null
    private var currentPop: MessageOperatePopup? = null    //弹出气泡
    private val mDefaultTimeDifference = 120               //默认时间差值
    private var mPopCallBack: PopCallBack? = null

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
                    (it as OtherTextViewHolder).bindViewHolder(position)
                }
                RenderType.MESSAGE_TYPE_OTHER_IMAGE -> { //图片
                    (it as OtherImageViewHolder).bindViewHolder(position)
                }
                RenderType.MESSAGE_TYPE_OTHER_GIF -> { //gif
                    (it as OtherGifViewHolder).bindViewHolder(position)
                }
                RenderType.MESSAGE_TYPE_OTHER_AUDIO -> {    //语音
                    (it as OtherAudioViewHolder).bindViewHolder(position)
                }
                RenderType.MESSAGE_TYPE_OTHER_VIDEO -> {    //视频
                    (it as OtherVideoViewHolder).bindViewHolder(position)
                }
            /**
             * 自己消息
             */
                RenderType.MESSAGE_TYPE_MINE_TEXT, RenderType.MESSAGE_TYPE_MINE_GIF_IMAGE -> {  //文本(表情)
                    (it as MineTextViewHolder).bindViewHolder(position)
                }
                RenderType.MESSAGE_TYPE_MINE_IMAGE -> {   //图片
                    (it as MineImageViewHolder).bindViewHolder(position)
                }
                RenderType.MESSAGE_TYPE_MINE_GIF -> {   //gif
                    (it as MineGifViewHolder).bindViewHolder(position)
                }
                RenderType.MESSAGE_TYPE_MINE_AUDIO -> { //语音
                    (it as MineAudioViewHolder).bindViewHolder(position)
                }
                RenderType.MESSAGE_TYPE_MINE_VIDEO -> { //视频
                    (it as MineVideoViewHolder).bindViewHolder(position)
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
                OtherTextViewHolder(view, viewGroup)
            }
            RenderType.MESSAGE_TYPE_OTHER_IMAGE -> { //图片
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_other_image, viewGroup, false)
                OtherImageViewHolder(view, viewGroup)
            }
            RenderType.MESSAGE_TYPE_OTHER_GIF -> { //gif
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_other_gif, viewGroup, false)
                OtherGifViewHolder(view, viewGroup)
            }
            RenderType.MESSAGE_TYPE_OTHER_AUDIO -> {    //语音
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_other_audio, viewGroup, false)
                OtherAudioViewHolder(view, viewGroup)
            }
            RenderType.MESSAGE_TYPE_OTHER_VIDEO -> {    //视频
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_other_video, viewGroup, false)
                OtherVideoViewHolder(view, viewGroup)
            }
        /**
         * 自己消息
         */
            RenderType.MESSAGE_TYPE_MINE_TEXT, RenderType.MESSAGE_TYPE_MINE_GIF_IMAGE -> {  //文本(表情)
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_mine_text, viewGroup, false)
                MineTextViewHolder(view, viewGroup)
            }
            RenderType.MESSAGE_TYPE_MINE_IMAGE -> {   //图片
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_mine_image, viewGroup, false)
                MineImageViewHolder(view, viewGroup)
            }
            RenderType.MESSAGE_TYPE_MINE_GIF -> {   //gif
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_mine_gif, viewGroup, false)
                MineGifViewHolder(view, viewGroup)
            }
            RenderType.MESSAGE_TYPE_MINE_AUDIO -> { //语音
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_mine_audio, viewGroup, false)
                MineAudioViewHolder(view, viewGroup)
            }
            RenderType.MESSAGE_TYPE_MINE_VIDEO -> { //视频
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_mine_video, viewGroup, false)
                MineVideoViewHolder(view, viewGroup)
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
            if (msgObjectList[0] == item) {
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
                    tvMyMessageChatRvTime.text = "您撤回了一条消息"
                }
                RenderType.MESSAGE_TYPE_OTHER_REVOKE -> {    //别人撤回
                    tvMyMessageChatRvTime.text = "对方撤回了一条消息"
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
            if (msgObjectList[0] == item) {
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
    inner class OtherTextViewHolder(view: View, viewGroup: ViewGroup) : RecyclerView.ViewHolder(view) {
        private val parent = viewGroup
        private val ivMyMessageChatRvOtherTextHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvOtherTextHead)
        private val tvMyMessageChatRvOtherTextText = itemView.findViewById<TextView>(R.id.tvMyMessageChatRvOtherTextText)
        fun bindViewHolder(position: Int) {
            val textMessage = msgObjectList[position] as TextMessage
            val userEntity = getUserEntity(textMessage)
            GlideLoader.load(AppManager.instance.currentActivity(), userEntity.avatar, ivMyMessageChatRvOtherTextHead, placeholder = R.drawable.default_img_icon, roundRadius = 15)
            tvMyMessageChatRvOtherTextText.let {
                it.text = SpanStringUtils.getEmotionContent(EmotionUtils.EMOTION_CLASSIC_TYPE, AppManager.instance.currentActivity(), textMessage.info, it)
                it.setOnLongClickListener {
                    showPopWindow(position, parent, it)
                    true
                }
            }
        }
    }

    /**
     * 图片
     */
    inner class OtherImageViewHolder(view: View, viewGroup: ViewGroup) : RecyclerView.ViewHolder(view) {
        private val parent = viewGroup
        private val ivMyMessageChatRvOtherImageHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvOtherImageHead)
        private val ivMyMessageChatRvOtherImageImage = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvOtherImageImage)
        fun bindViewHolder(position: Int) {
            val imageMessage = msgObjectList[position] as ImageMessage
            val userEntity = getUserEntity(imageMessage)
            GlideLoader.load(AppManager.instance.currentActivity(), userEntity.avatar, ivMyMessageChatRvOtherImageHead, placeholder = R.drawable.default_img_icon, roundRadius = 15)
            ivMyMessageChatRvOtherImageImage.let {
                GlideLoader.load(AppManager.instance.currentActivity(), imageMessage.url, it, roundRadius = 15)
                it.setOnClickListener {
                    val i = Intent(context, PreviewMessageImagesActivity::class.java)
                    val bundle = Bundle()
                    bundle.putSerializable(IntentConstant.CUR_MESSAGE, imageMessage)
                    i.putExtras(bundle)
                    context.startActivity(i)
                    (context as Activity).overridePendingTransition(R.anim.tt_image_enter, R.anim.tt_stay)
                }
                it.setOnLongClickListener {
                    showPopWindow(position, parent, it)
                    true
                }
            }
        }
    }

    /**
     * gif
     */
    inner class OtherGifViewHolder(view: View, viewGroup: ViewGroup) : RecyclerView.ViewHolder(view) {
        private val parent = viewGroup
        private val ivMyMessageChatRvOtherGifHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvOtherGifHead)
        private val ivMyMessageChatRvOtherGifImage = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvOtherGifImage)
        fun bindViewHolder(position: Int) {
            val imageMessage = msgObjectList[position] as ImageMessage
            val userEntity = getUserEntity(imageMessage)
            GlideLoader.load(AppManager.instance.currentActivity(), userEntity.avatar, ivMyMessageChatRvOtherGifHead, placeholder = R.drawable.default_img_icon, roundRadius = 15)
            ivMyMessageChatRvOtherGifImage.let {
                GlideLoader.load(AppManager.instance.currentActivity(), imageMessage.url, it, roundRadius = 15)
                it.setOnClickListener {
                    val i = Intent(context, PreviewMessageImagesActivity::class.java)
                    val bundle = Bundle()
                    bundle.putSerializable(IntentConstant.CUR_MESSAGE, imageMessage)
                    i.putExtras(bundle)
                    context.startActivity(i)
                    (context as Activity).overridePendingTransition(R.anim.tt_image_enter, R.anim.tt_stay)
                }
                it.setOnLongClickListener {
                    showPopWindow(position, parent, it)
                    true
                }
            }
        }
    }

    /**
     * 语音
     */
    inner class OtherAudioViewHolder(view: View, viewGroup: ViewGroup) : RecyclerView.ViewHolder(view) {
        private val parent = viewGroup
        private val ivMyMessageChatRvOtherAudioHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvOtherAudioHead)
        private val tvMyMessageChatRvOtherAudioTime = itemView.findViewById<TextView>(R.id.tvMyMessageChatRvOtherAudioTime)
        private val llMyMessageChatRvOtherAudio = itemView.findViewById<LinearLayout>(R.id.llMyMessageChatRvOtherAudio)
        fun bindViewHolder(position: Int) {
            val audioMessage = msgObjectList[position] as AudioMessage
            val userEntity = getUserEntity(audioMessage)
            GlideLoader.load(AppManager.instance.currentActivity(), userEntity.avatar, ivMyMessageChatRvOtherAudioHead, placeholder = R.drawable.default_img_icon, roundRadius = 15)
            tvMyMessageChatRvOtherAudioTime.text = "${audioMessage.audiolength}\""
            llMyMessageChatRvOtherAudio.let {
                it.setOnClickListener {
                    audioClick(audioMessage)
                    audioMessage.readStatus = MessageConstant.AUDIO_READED
                    mImService!!.dbInterface.insertOrUpdateMessage(audioMessage)
                }
                it.setOnLongClickListener {
                    showPopWindow(position, parent, it)
                    true
                }
            }
        }
    }

    /**
     * 视频
     */
    inner class OtherVideoViewHolder(view: View, viewGroup: ViewGroup) : RecyclerView.ViewHolder(view) {
        private val parent = viewGroup
        private val ivMyMessageChatRvOtherVideoHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvOtherVideoHead)
        private val ivMyMessageChatRvOtherVideoCover = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvOtherVideoCover)
        private val rlMyMessageChatRvOtherVideo = itemView.findViewById<RelativeLayout>(R.id.rlMyMessageChatRvOtherVideo)
        private val tvMyMessageChatRvOtherVideoTime = itemView.findViewById<TextView>(R.id.tvMyMessageChatRvOtherVideoTime)
        fun bindViewHolder(position: Int) {
            val videoMessage = msgObjectList[position] as VideoMessage
            val userEntity = getUserEntity(videoMessage)
            GlideLoader.load(AppManager.instance.currentActivity(), userEntity.avatar, ivMyMessageChatRvOtherVideoHead, placeholder = R.drawable.default_img_icon, roundRadius = 15)
            //封面
            GlideLoader.load(AppManager.instance.currentActivity(), videoMessage.thumbUrl, ivMyMessageChatRvOtherVideoCover, roundRadius = 15)
            //时间
            tvMyMessageChatRvOtherVideoTime.text = "${videoMessage.videolength}s"
            rlMyMessageChatRvOtherVideo.let {
                it.setOnClickListener {
                    val url = if (!TextUtils.isEmpty(videoMessage.path) && File(videoMessage.path).exists()) {
                        videoMessage.path
                    } else {
                        videoMessage.url
                    }
                    val uri = Uri.parse(url)
                    uri?.let {
                        //调用系统自带的播放器
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(it, "video/mp4")
                        context.startActivity(intent)
                    }
                }
                it.setOnLongClickListener {
                    showPopWindow(position, parent, it)
                    true
                }
            }
        }
    }

    /**
     * ---------------------------------------------自己消息---------------------------------------------
     */

    /**
     * 文本(表情)
     */
    inner class MineTextViewHolder(view: View, viewGroup: ViewGroup) : RecyclerView.ViewHolder(view) {
        private val parent = viewGroup
        private val ivMyMessageChatRvMineTextHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineTextHead)
        private val tvMyMessageChatRvMineTextText = itemView.findViewById<TextView>(R.id.tvMyMessageChatRvMineTextText)
        private val pbMyMessageChatRvMineTextProgress = itemView.findViewById<ProgressBar>(R.id.pbMyMessageChatRvMineTextProgress)
        private val ivMyMessageChatRvMineTextFail = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineTextFail)
        fun bindViewHolder(position: Int) {
            val textMessage = msgObjectList[position] as TextMessage
            val userEntity = getUserEntity(textMessage)
            GlideLoader.load(AppManager.instance.currentActivity(), userEntity.avatar, ivMyMessageChatRvMineTextHead, placeholder = R.drawable.default_img_icon, roundRadius = 15)
            tvMyMessageChatRvMineTextText.let {
                it.text = SpanStringUtils.getEmotionContent(EmotionUtils.EMOTION_CLASSIC_TYPE, AppManager.instance.currentActivity(), textMessage.info, it)
                it.setOnLongClickListener {
                    showPopWindow(position, parent, it)
                    true
                }
            }
            //失败点击
            ivMyMessageChatRvMineTextFail.setOnClickListener {
                showPopWindow(position, parent, it)
            }
            //消息发送状态
            when (textMessage.status) {
                MessageConstant.MSG_SENDING -> {  //发送中
                    pbMyMessageChatRvMineTextProgress.visibility = View.VISIBLE
                    ivMyMessageChatRvMineTextFail.visibility = View.GONE
                }
                MessageConstant.MSG_SUCCESS -> {   //发送成功
                    pbMyMessageChatRvMineTextProgress.visibility = View.GONE
                    ivMyMessageChatRvMineTextFail.visibility = View.GONE
                }
                MessageConstant.MSG_FAILURE -> {   //发送失败
                    pbMyMessageChatRvMineTextProgress.visibility = View.GONE
                    ivMyMessageChatRvMineTextFail.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * 图片
     */
    inner class MineImageViewHolder(view: View, viewGroup: ViewGroup) : RecyclerView.ViewHolder(view) {
        private val parent = viewGroup
        private val ivMyMessageChatRvMineImageHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineImageHead)
        private val ivMyMessageChatRvMineImageImage = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineImageImage)
        private val pbMyMessageChatRvMineImageProgress = itemView.findViewById<ProgressBar>(R.id.pbMyMessageChatRvMineImageProgress)
        private val ivMyMessageChatRvMineImageFail = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineImageFail)
        fun bindViewHolder(position: Int) {
            val imageMessage = msgObjectList[position] as ImageMessage
            val userEntity = getUserEntity(imageMessage)
            GlideLoader.load(AppManager.instance.currentActivity(), userEntity.avatar, ivMyMessageChatRvMineImageHead, placeholder = R.drawable.default_img_icon, roundRadius = 15)
            ivMyMessageChatRvMineImageImage.let {
                //有本地的先加载本地的
                if (FileUtil.isFileExist(imageMessage.path)) {
                    GlideLoader.load(AppManager.instance.currentActivity(), imageMessage.path, it, roundRadius = 15)
                }
                GlideLoader.load(AppManager.instance.currentActivity(), imageMessage.url, it, roundRadius = 15)
                it.setOnClickListener {
                    val i = Intent(context, PreviewMessageImagesActivity::class.java)
                    val bundle = Bundle()
                    bundle.putSerializable(IntentConstant.CUR_MESSAGE, imageMessage)
                    i.putExtras(bundle)
                    context.startActivity(i)
                    (context as Activity).overridePendingTransition(R.anim.tt_image_enter, R.anim.tt_stay)
                }
                it.setOnLongClickListener {
                    showPopWindow(position, parent, it)
                    true
                }
            }
            //失败点击
            ivMyMessageChatRvMineImageFail.setOnClickListener {
                showPopWindow(position, parent, it)
            }
            //图片发送状态
            when (imageMessage.loadStatus) {
                MessageConstant.IMAGE_UNLOAD -> {

                }
                MessageConstant.IMAGE_LOADING -> {  //发送中
                    pbMyMessageChatRvMineImageProgress.visibility = View.VISIBLE
                    ivMyMessageChatRvMineImageFail.visibility = View.GONE
                }
                MessageConstant.IMAGE_LOADED_SUCCESS -> {   //发送成功
                    pbMyMessageChatRvMineImageProgress.visibility = View.GONE
                    ivMyMessageChatRvMineImageFail.visibility = View.GONE
                }
                MessageConstant.IMAGE_LOADED_FAILURE -> {   //发送失败
                    pbMyMessageChatRvMineImageProgress.visibility = View.GONE
                    ivMyMessageChatRvMineImageFail.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * gif
     */
    inner class MineGifViewHolder(view: View, viewGroup: ViewGroup) : RecyclerView.ViewHolder(view) {
        private val parent = viewGroup
        private val ivMyMessageChatRvMineGifHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineGifHead)
        private val ivMyMessageChatRvMineGifImage = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineGifImage)
        private val pbMyMessageChatRvMineGifProgress = itemView.findViewById<ProgressBar>(R.id.pbMyMessageChatRvMineGifProgress)
        private val ivMyMessageChatRvMineGifFail = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineGifFail)
        fun bindViewHolder(position: Int) {
            val imageMessage = msgObjectList[position] as ImageMessage
            val userEntity = getUserEntity(imageMessage)
            GlideLoader.load(AppManager.instance.currentActivity(), userEntity.avatar, ivMyMessageChatRvMineGifHead, placeholder = R.drawable.default_img_icon, roundRadius = 15)
            ivMyMessageChatRvMineGifImage.let {
                //有本地的先加载本地的
                if (FileUtil.isFileExist(imageMessage.path)) {
                    GlideLoader.load(AppManager.instance.currentActivity(), imageMessage.path, it, roundRadius = 15)
                }
                GlideLoader.load(AppManager.instance.currentActivity(), imageMessage.url, it, roundRadius = 15)
                it.setOnClickListener {
                    val i = Intent(context, PreviewMessageImagesActivity::class.java)
                    val bundle = Bundle()
                    bundle.putSerializable(IntentConstant.CUR_MESSAGE, imageMessage)
                    i.putExtras(bundle)
                    context.startActivity(i)
                    (context as Activity).overridePendingTransition(R.anim.tt_image_enter, R.anim.tt_stay)
                }
                it.setOnLongClickListener {
                    showPopWindow(position, parent, it)
                    true
                }
            }
            //失败点击
            ivMyMessageChatRvMineGifFail.setOnClickListener {
                showPopWindow(position, parent, it)
            }
            //图片发送状态
            when (imageMessage.loadStatus) {
                MessageConstant.IMAGE_UNLOAD -> {

                }
                MessageConstant.IMAGE_LOADING -> {  //发送中
                    pbMyMessageChatRvMineGifProgress.visibility = View.VISIBLE
                    ivMyMessageChatRvMineGifFail.visibility = View.GONE
                }
                MessageConstant.IMAGE_LOADED_SUCCESS -> {   //发送成功
                    pbMyMessageChatRvMineGifProgress.visibility = View.GONE
                    ivMyMessageChatRvMineGifFail.visibility = View.GONE
                }
                MessageConstant.IMAGE_LOADED_FAILURE -> {   //发送失败
                    pbMyMessageChatRvMineGifProgress.visibility = View.GONE
                    ivMyMessageChatRvMineGifFail.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * 语音
     */
    inner class MineAudioViewHolder(view: View, viewGroup: ViewGroup) : RecyclerView.ViewHolder(view) {
        private val parent = viewGroup
        private val ivMyMessageChatRvMineAudioHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineAudioHead)
        private val tvMyMessageChatRvMineAudioTime = itemView.findViewById<TextView>(R.id.tvMyMessageChatRvMineAudioTime)
        private val llMyMessageChatRvMineAudio = itemView.findViewById<LinearLayout>(R.id.llMyMessageChatRvMineAudio)
        private val pbMyMessageChatRvMineAudioProgress = itemView.findViewById<ProgressBar>(R.id.pbMyMessageChatRvMineAudioProgress)
        private val ivMyMessageChatRvMineAudioFail = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineAudioFail)
        fun bindViewHolder(position: Int) {
            val audioMessage = msgObjectList[position] as AudioMessage
            val userEntity = getUserEntity(audioMessage)
            GlideLoader.load(AppManager.instance.currentActivity(), userEntity.avatar, ivMyMessageChatRvMineAudioHead, placeholder = R.drawable.default_img_icon, roundRadius = 15)
            tvMyMessageChatRvMineAudioTime.text = "${audioMessage.audiolength}\""
            llMyMessageChatRvMineAudio.let {
                it.setOnClickListener {
                    audioClick(audioMessage)
                    audioMessage.readStatus = MessageConstant.AUDIO_READED
                    mImService!!.dbInterface.insertOrUpdateMessage(audioMessage)
                }
                it.setOnLongClickListener {
                    showPopWindow(position, parent, it)
                    true
                }
            }
            //失败点击
            ivMyMessageChatRvMineAudioFail.setOnClickListener {
                showPopWindow(position, parent, it)
            }
            //语音发送状态
            when (audioMessage.status) {
                MessageConstant.MSG_SENDING -> {  //发送中
                    pbMyMessageChatRvMineAudioProgress.visibility = View.VISIBLE
                    ivMyMessageChatRvMineAudioFail.visibility = View.GONE
                }
                MessageConstant.MSG_SUCCESS -> {   //发送成功
                    pbMyMessageChatRvMineAudioProgress.visibility = View.GONE
                    ivMyMessageChatRvMineAudioFail.visibility = View.GONE
                }
                MessageConstant.MSG_FAILURE -> {   //发送失败
                    pbMyMessageChatRvMineAudioProgress.visibility = View.GONE
                    ivMyMessageChatRvMineAudioFail.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * 视频
     */
    inner class MineVideoViewHolder(view: View, viewGroup: ViewGroup) : RecyclerView.ViewHolder(view) {
        private val parent = viewGroup
        private val ivMyMessageChatRvMineVideoHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineVideoHead)
        private val ivMyMessageChatRvMineVideoCover = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineVideoCover)
        private val rlMyMessageChatRvMineVideo = itemView.findViewById<RelativeLayout>(R.id.rlMyMessageChatRvMineVideo)
        private val tvMyMessageChatRvMineVideoTime = itemView.findViewById<TextView>(R.id.tvMyMessageChatRvMineVideoTime)
        private val pbMyMessageChatRvMineVideoProgress = itemView.findViewById<ProgressBar>(R.id.pbMyMessageChatRvMineVideoProgress)
        private val ivMyMessageChatRvMineVideoFail = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineVideoFail)
        fun bindViewHolder(position: Int) {
            val videoMessage = msgObjectList[position] as VideoMessage
            val userEntity = getUserEntity(videoMessage)
            GlideLoader.load(AppManager.instance.currentActivity(), userEntity.avatar, ivMyMessageChatRvMineVideoHead, placeholder = R.drawable.default_img_icon, roundRadius = 15)
            //封面 有本地的加载本地的
            val url = if (FileUtil.isFileExist(videoMessage.thumbPath)) {
                videoMessage.thumbPath
            } else {
                videoMessage.thumbUrl
            }
            GlideLoader.load(AppManager.instance.currentActivity(), url, ivMyMessageChatRvMineVideoCover, roundRadius = 15)
            //时间
            tvMyMessageChatRvMineVideoTime.text = "${videoMessage.videolength}s"
            rlMyMessageChatRvMineVideo.let {
                it.setOnClickListener {
                    val url = if (!TextUtils.isEmpty(videoMessage.path) && File(videoMessage.path).exists()) {
                        videoMessage.path
                    } else {
                        videoMessage.url
                    }
                    val uri = Uri.parse(url)
                    uri?.let {
                        //调用系统自带的播放器
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(it, "video/mp4")
                        context.startActivity(intent)
                    }
                }
                it.setOnLongClickListener {
                    showPopWindow(position, parent, it)
                    true
                }
            }
            //失败点击
            ivMyMessageChatRvMineVideoFail.setOnClickListener {
                showPopWindow(position, parent, it)
            }
            //视频发送状态
            when (videoMessage.readStatus) {
                MessageConstant.VIDEO_UNREAD -> {   //未查看

                }
                MessageConstant.VIDEO_READED -> {   //已查看

                }
                MessageConstant.VIDEO_LOADING -> {  //发送中
                    pbMyMessageChatRvMineVideoProgress.visibility = View.VISIBLE
                    ivMyMessageChatRvMineVideoFail.visibility = View.GONE
                }
                MessageConstant.VIDEO_LOADED_SUCCESS -> {   //发送成功
                    pbMyMessageChatRvMineVideoProgress.visibility = View.GONE
                    ivMyMessageChatRvMineVideoFail.visibility = View.GONE
                }
                MessageConstant.VIDEO_LOADED_FAILURE -> {   //发送失败
                    pbMyMessageChatRvMineVideoProgress.visibility = View.GONE
                    ivMyMessageChatRvMineVideoFail.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * 设置service
     */
    fun setImService(mImService: IMService?, loginUser: UserEntity?) {
        this.mImService = mImService
        this.loginUser = loginUser
    }

    /**
     * 添加历史消息
     */
    fun addItem(msg: MessageEntity, mLayoutManager: LinearLayoutManager? = null) {
        val nextTime = msg.created
        if (msgObjectList.size > 0) {
            val objectMessage = msgObjectList[msgObjectList.lastIndex]
            if (objectMessage is MessageEntity) {
                val preTime = objectMessage.created
                val needTime = DateUtil.needDisplayTime(preTime, nextTime)
                if (needTime) {
                    msgObjectList.add(nextTime)
                }
            }
        } else {
            val message = msg.created
            msgObjectList.add(message)
        }
        /**消息的判断 */
        if (msg.displayType == DBConstant.SHOW_MIX_TEXT) {
            val mixMessage = msg as MixMessage
            msgObjectList.addAll(mixMessage.getMsgList())
        } else {
            msgObjectList.add(msg)
        }
        if (msg is ImageMessage) {
            ImageMessage.addToImageMessageList(msg)
        }
        mLayoutManager?.scrollToPosition(msgObjectList.lastIndex)
    }

    /**
     * 删除一条消息
     */
    fun removeMsg(messageEntity: MessageEntity) {
        //根据消息Id删除数据库内容
        DBInterface.instance().deleteMessageByMsgId(messageEntity.msgId)
        msgObjectList.remove(messageEntity)
        notifyDataSetChanged()
    }

    /**
     * 撤回一条消息
     */
    fun revokeMsg(entity: MessageEntity) {
        entity.displayType = DBConstant.SHOW_REVOKE_TYPE
        //根据消息修改数据库内容
        DBInterface.instance().insertOrUpdateMessage(entity)
        notifyDataSetChanged()
    }

    /**
     * 修改新收到的消息
     */
    fun updateRevokeMsg(entity: MessageEntity) {
        val txtMsg = entity as TextMessage
        for (objectList in msgObjectList) {
            if (objectList is MessageEntity) {
                val messageEntity = objectList
                if (messageEntity.msgId == txtMsg.getAttributeInt(MessageExtConst.MSGID)) {
                    objectList.displayType = DBConstant.SHOW_REVOKE_TYPE
                    //根据消息修改数据库内容
                    DBInterface.instance().insertOrUpdateMessage(messageEntity)
                }
            }
        }
    }

    /**
     * 是否是gif图
     */
    private fun isMsgGif(msg: MessageEntity): Boolean {
        val content = msg.info
        // @YM 临时处理  牙牙表情与消息混合出现的消息丢失
        return if (TextUtils.isEmpty(content) || !(content.startsWith("[") && content.endsWith("]"))) {
            false
        } else Emoparser.getInstance(context)!!.isMessageGif(msg.info)
    }

    /**
     * 获取第一个Message类
     */
    fun getTopMsgEntity(): MessageEntity? {
        if (msgObjectList.size <= 0) {
            return null
        }
        for (result in msgObjectList) {
            if (result is MessageEntity) {
                return result
            }
        }
        return null
    }

    /**
     * 时间比较
     */
    class MessageTimeComparator : Comparator<MessageEntity> {
        override fun compare(lhs: MessageEntity, rhs: MessageEntity): Int {
            return if (lhs.created == rhs.created) {
                lhs.msgId - rhs.msgId
            } else lhs.created - rhs.created
        }
    }


    /**
     * 取用户信息
     */
    private fun getUserEntity(textMessage: MessageEntity): UserEntity {
        val userEntity: UserEntity?
        if (textMessage.fromId == loginUser?.peerId) {//自己
            userEntity = loginUser
        } else {
            userEntity = mImService?.contactManager?.findContact(textMessage.fromId)
        }
        return userEntity!!
    }

    /**
     * 下拉载入历史消息,从最上面开始添加
     */
    fun loadHistoryList(historyList: List<MessageEntity>?, mLayoutManager: LinearLayoutManager? = null, isPullDownToRefresh: Boolean = false) {
        if (null == historyList || historyList.isEmpty()) {
            return
        }
        Collections.sort(historyList, MessageTimeComparator())
        val chatList = ArrayList<Any>()
        var preTime = 0
        var nextTime = 0
        val idset = HashSet<Int>()
        for (msg in historyList) {
            idset.add(msg.fromId)
            if (msg.displayType == DBConstant.MSG_TYPE_SINGLE_TEXT) {
                if (isMsgGif(msg)) {
                    msg.isGIfEmo = true
                }
            }
            nextTime = msg.created
            val needTimeBubble = DateUtil.needDisplayTime(preTime, nextTime)
            if (needTimeBubble) {
                val `in` = nextTime
                chatList.add(`in`)
            }
            preTime = nextTime
            if (msg.displayType == DBConstant.SHOW_MIX_TEXT) {
                val mixMessage = msg as MixMessage
                chatList.addAll(mixMessage.getMsgList())
            } else {
                chatList.add(msg)
            }
        }
        // 如果是历史消息，从头开始加
        msgObjectList.addAll(0, chatList)
        getImageList()
        notifyDataSetChanged()
        if (isPullDownToRefresh) {  //加载历史
            mLayoutManager?.scrollToPositionWithOffset(chatList.lastIndex + 1, 0)
        } else {    //直接添加历史
            mLayoutManager?.scrollToPosition(msgObjectList.lastIndex)
        }
    }

    /**
     * 获取图片消息列表
     */
    private fun getImageList() {
        for (i in msgObjectList.indices.reversed()) {
            val item = msgObjectList[i]
            if (item is ImageMessage) {
                ImageMessage.addToImageMessageList(item)
            }
        }
    }

    /**
     * 临时处理，一定要干掉
     */
    fun hidePopup() {
        if (currentPop != null) {
            currentPop?.hidePopup()
        }
    }

    /**
     * msgId 是消息ID
     * localId是本地的ID
     * position 是list 的位置
     *
     * 只更新item的状态
     * 刷新单条记录
     */
    fun updateItemState(position: Int, messageEntity: MessageEntity) {
        //更新DB
        //更新单条记录
        mImService?.dbInterface?.insertOrUpdateMessage(messageEntity)
        notifyDataSetChanged()
    }

    /**
     * 对于混合消息的特殊处理
     */
    fun updateItemState(messageEntity: MessageEntity) {
        val dbId = messageEntity.id!!
        val msgId = messageEntity.msgId
        val len = msgObjectList.size
        for (index in len - 1 downTo 1) {
            val objectMessage = msgObjectList[index]
            if (objectMessage is MessageEntity) {
                if (objectMessage is ImageMessage) {
                    ImageMessage.addToImageMessageList(objectMessage)
                }
                if (objectMessage.id == dbId && objectMessage.msgId == msgId) {
                    msgObjectList[index] = messageEntity
                    break
                }
            }
        }
        notifyDataSetChanged()
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

    /**
     * pop点击事件的定义
     */
    private fun getPopMenu(parent: ViewGroup, listener: MessageOperatePopup.OnItemClickListener): MessageOperatePopup {
        val popupView = MessageOperatePopup.instance(context, parent)
        currentPop = popupView
        popupView.setOnItemClickListener(listener)
        return popupView
    }

    private inner class OperateItemClickListener(private val mMsgInfo: MessageEntity, private val mPosition: Int) : MessageOperatePopup.OnItemClickListener {
        val mType: Int = mMsgInfo.displayType
        //复制
        @SuppressLint("NewApi")
        override fun onCopyClick() {
            try {
                val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                    val data = ClipData.newPlainText("data", mMsgInfo.info)
                    manager.primaryClip = data
                } else {
                    manager.text = mMsgInfo.info
                }
            } catch (e: Exception) {

            }
        }

        //重新发送
        override fun onResendClick() {
            try {
                if (mType == DBConstant.SHOW_AUDIO_TYPE || mType == DBConstant.SHOW_ORIGIN_TEXT_TYPE) {
                    if (mMsgInfo.displayType == DBConstant.SHOW_AUDIO_TYPE) {
                        if (mMsgInfo.sendContent.size < 4) {
                            return
                        }
                    }
                } else if (mType == DBConstant.SHOW_IMAGE_TYPE) {
                    // 之前的状态是什么 上传没有成功继续上传
                    // 上传成功，发送消息
                    val imageMessage = mMsgInfo as ImageMessage
                    if (TextUtils.isEmpty(imageMessage.path)) {
                        Toast.makeText(context, context.getString(com.leimo.wanxin.R.string.image_path_unavaluable), Toast.LENGTH_LONG).show()
                        return
                    }
                }
                mMsgInfo.status = MessageConstant.MSG_SENDING
                msgObjectList.removeAt(mPosition)
                addItem(mMsgInfo)
                if (mImService != null) {
                    mImService?.messageManager?.resendMessage(mMsgInfo)
                }
            } catch (e: Exception) {

            }
        }

        //扬声器
        override fun onSpeakerClick() {
            val audioPlayerHandler = AudioPlayerHandler.getInstance()
            if (audioPlayerHandler.getAudioMode(context) == android.media.AudioManager.MODE_NORMAL) {
                audioPlayerHandler.setAudioMode(android.media.AudioManager.MODE_IN_CALL, context)
                SpeekerToast.show(context, context.getText(com.leimo.wanxin.R.string.audio_in_call), Toast.LENGTH_SHORT)
            } else {
                audioPlayerHandler.setAudioMode(android.media.AudioManager.MODE_NORMAL, context)
                SpeekerToast.show(context, context.getText(com.leimo.wanxin.R.string.audio_in_speeker), Toast.LENGTH_SHORT)
            }
        }

        //撤回
        override fun onRevokeClick(position: Int) {
            if (mPopCallBack != null) {
                mPopCallBack!!.onRevokeClick(position)
            }
        }

        //删除
        override fun onDeleteClick(position: Int) {
            if (mPopCallBack != null) {
                mPopCallBack!!.onDeleteClick(position)
            }
        }
    }

    /**
     * 显示气泡
     */
    private fun showPopWindow(position: Int, parent: ViewGroup, view: View) {
        val message = (msgObjectList[position] as MessageEntity)
        // 创建一个pop对象，然后 分支判断状态，然后显示需要的内容
        val isMine = message.fromId == loginUser?.peerId
        val popup = getPopMenu(parent, OperateItemClickListener(message, position))
        val bResend = message.status == MessageConstant.MSG_FAILURE
        //消息是否在2分钟之内创建的
        val bRevoke = (System.currentTimeMillis() / 1000) - message.created < mDefaultTimeDifference
        popup.show(view, message.displayType, bResend, isMine, bRevoke, position)
    }

    fun setPopCallBack(popCallBack: PopCallBack) {
        mPopCallBack = popCallBack
    }

    interface PopCallBack {
        fun onRevokeClick(position: Int)

        fun onDeleteClick(position: Int)
    }
}