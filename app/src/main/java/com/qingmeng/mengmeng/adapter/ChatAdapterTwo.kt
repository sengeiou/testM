package com.qingmeng.mengmeng.adapter

import AppManager
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.app.common.extensions.setWidthHeightExt
import com.bumptech.glide.Glide
import com.lemo.emojcenter.utils.EmotionUtils
import com.lemo.emojcenter.utils.SpanStringUtils
import com.luck.picture.lib.PictureSelector
import com.mogujie.tt.config.DBConstant
import com.mogujie.tt.config.IntentConstant
import com.mogujie.tt.config.MessageConstant
import com.mogujie.tt.config.MessageExtConst
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
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.activity.MyMessageChatActivity
import com.qingmeng.mengmeng.utils.*
import com.qingmeng.mengmeng.utils.imageLoader.GlideLoader
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 *  Description :聊天多个样式的Adapter

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/2/14
 */
class ChatAdapterTwo(private val context: Context, var msgObjectList: ArrayList<Any>, val audioClick: (audioMessage: AudioMessage, imageView: ImageView) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mImService: IMService? = null
    private var loginUser: UserEntity? = null
    private var currentPop: MessageOperatePopup? = null    //弹出气泡
    private val mDefaultTimeDifference = 120               //默认时间差值
    private var mCallBack: CallBack? = null

    override fun getItemViewType(position: Int): Int {
        return getItemViewType(msgObjectList[position])
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.let {
            when (RenderType.values()[getItemViewType(position)]) {
                RenderType.MESSAGE_TYPE_TIME_TITLE, RenderType.MESSAGE_TYPE_OTHER_REVOKE, RenderType.MESSAGE_TYPE_MINE_REVOKE -> {  //时间
                    (it as TimeViewHolder).bindViewHolder(msgObjectList[position])
                }
                RenderType.MESSAGE_TYPE_INVALID -> {    //默认失败类型

                }
                RenderType.MESSAGE_TYPE_BRAND -> {    //品牌详情
                    (it as BrandViewHolder).bindViewHolder(position)
                }
                /**
                 * 别人消息
                 */
                RenderType.MESSAGE_TYPE_OTHER_TEXT, RenderType.MESSAGE_TYPE_OTHER_GIF -> {    //别人文本(emoji表情)
                    (it as OtherTextViewHolder).bindViewHolder(position)
                }
                RenderType.MESSAGE_TYPE_OTHER_IMAGE -> { //别人图片
                    (it as OtherImageViewHolder).bindViewHolder(position)
                }
                RenderType.MESSAGE_TYPE_OTHER_GIF_IMAGE -> { //别人gif
                    (it as OtherGifViewHolder).bindViewHolder(position)
                }
                RenderType.MESSAGE_TYPE_OTHER_AUDIO -> {    //别人语音
                    (it as OtherAudioViewHolder).bindViewHolder(position)
                }
                RenderType.MESSAGE_TYPE_OTHER_VIDEO -> {    //别人视频
                    (it as OtherVideoViewHolder).bindViewHolder(position)
                }
                RenderType.MESSAGE_TYPE_OTHER_BRAND -> {    //别人品牌详情
                    (it as OtherBrandViewHolder).bindViewHolder(position)
                }
                /**
                 * 自己消息
                 */
                RenderType.MESSAGE_TYPE_MINE_TEXT, RenderType.MESSAGE_TYPE_MINE_GIF -> {  //自己文本(emoji表情)
                    (it as MineTextViewHolder).bindViewHolder(position)
                }
                RenderType.MESSAGE_TYPE_MINE_IMAGE -> {   //自己图片
                    (it as MineImageViewHolder).bindViewHolder(position)
                }
                RenderType.MESSAGE_TYPE_MINE_GIF_IMAGE -> {   //自己gif
                    (it as MineGifViewHolder).bindViewHolder(position)
                }
                RenderType.MESSAGE_TYPE_MINE_AUDIO -> { //自己语音
                    (it as MineAudioViewHolder).bindViewHolder(position)
                }
                RenderType.MESSAGE_TYPE_MINE_VIDEO -> { //自己视频
                    (it as MineVideoViewHolder).bindViewHolder(position)
                }
                RenderType.MESSAGE_TYPE_MINE_BRAND -> { //品牌详情
                    (it as MineBrandViewHolder).bindViewHolder(position)
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
            RenderType.MESSAGE_TYPE_INVALID -> {    //默认失败类型
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_defalut, viewGroup, false)
                DefaultViewHolder(view)
            }
            RenderType.MESSAGE_TYPE_BRAND -> {    //公共品牌详情
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_brand, viewGroup, false)
                BrandViewHolder(view)
            }
            /**
             * 别人消息
             */
            RenderType.MESSAGE_TYPE_OTHER_TEXT, RenderType.MESSAGE_TYPE_OTHER_GIF -> {    //别人文本(emoji表情)
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_other_text, viewGroup, false)
                OtherTextViewHolder(view, viewGroup)
            }
            RenderType.MESSAGE_TYPE_OTHER_IMAGE -> { //别人图片
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_other_image, viewGroup, false)
                OtherImageViewHolder(view, viewGroup)
            }
            RenderType.MESSAGE_TYPE_OTHER_GIF_IMAGE -> { //别人gif
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_other_gif, viewGroup, false)
                OtherGifViewHolder(view, viewGroup)
            }
            RenderType.MESSAGE_TYPE_OTHER_AUDIO -> {    //别人语音
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_other_audio, viewGroup, false)
                OtherAudioViewHolder(view, viewGroup)
            }
            RenderType.MESSAGE_TYPE_OTHER_VIDEO -> {    //别人视频
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_other_video, viewGroup, false)
                OtherVideoViewHolder(view, viewGroup)
            }
            RenderType.MESSAGE_TYPE_OTHER_BRAND -> {    //别人品牌详情
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_other_brand, viewGroup, false)
                OtherBrandViewHolder(view, viewGroup)
            }
            /**
             * 自己消息
             */
            RenderType.MESSAGE_TYPE_MINE_TEXT, RenderType.MESSAGE_TYPE_MINE_GIF -> {  //自己文本(emoji表情)
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_mine_text, viewGroup, false)
                MineTextViewHolder(view, viewGroup)
            }
            RenderType.MESSAGE_TYPE_MINE_IMAGE -> {   //自己图片
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_mine_image, viewGroup, false)
                MineImageViewHolder(view, viewGroup)
            }
            RenderType.MESSAGE_TYPE_MINE_GIF_IMAGE -> {   //自己gif
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_mine_gif, viewGroup, false)
                MineGifViewHolder(view, viewGroup)
            }
            RenderType.MESSAGE_TYPE_MINE_AUDIO -> { //自己语音
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_mine_audio, viewGroup, false)
                MineAudioViewHolder(view, viewGroup)
            }
            RenderType.MESSAGE_TYPE_MINE_VIDEO -> { //自己视频
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_mine_video, viewGroup, false)
                MineVideoViewHolder(view, viewGroup)
            }
            RenderType.MESSAGE_TYPE_MINE_BRAND -> {    //自己品牌详情
                view = LayoutInflater.from(context).inflate(R.layout.activity_my_message_chat_item_mine_brand, viewGroup, false)
                MineBrandViewHolder(view, viewGroup)
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
                llMyMessageChatRvAllTime.setMarginExt(top = 60)
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
     * 失败类型
     */
    inner class DefaultViewHolder(view: View) : RecyclerView.ViewHolder(view)

    /**
     * 品牌详情
     */
    inner class BrandViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val llMyMessageChatRvAllBrand = itemView.findViewById<LinearLayout>(R.id.llMyMessageChatRvAllBrand)
        private val llMyMessageChatRvBrand = itemView.findViewById<LinearLayout>(R.id.llMyMessageChatRvBrand)
        private val ivMyMessageChatRvLogo = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvLogo)
        private val tvMyMessageChatRvBrandName = itemView.findViewById<TextView>(R.id.tvMyMessageChatRvBrandName)
        private val tvMyMessageChatRvInvestmentAmount = itemView.findViewById<TextView>(R.id.tvMyMessageChatRvInvestmentAmount)
        private val tvMyMessageChatRvBrandSend = itemView.findViewById<TextView>(R.id.tvMyMessageChatRvBrandSend)
        fun bindViewHolder(position: Int) {
            val brandMessage = msgObjectList[position] as BrandMessage
            if (position == 0) {
                llMyMessageChatRvAllBrand.setMarginExt(top = 60)
            } else {
                llMyMessageChatRvAllBrand.setMarginExt(top = 0)
            }
            GlideLoader.load(AppManager.instance.currentActivity(), brandMessage.brandLogo, ivMyMessageChatRvLogo, placeholder = R.drawable.default_img_icon)
            tvMyMessageChatRvBrandName.text = brandMessage.brandName
            if (brandMessage.brandValue.isNullOrBlank()) {
                tvMyMessageChatRvInvestmentAmount.text = "面议"
            } else {
                tvMyMessageChatRvInvestmentAmount.text = brandMessage.brandValue
            }
            //品牌详情
            llMyMessageChatRvBrand.setOnClickListener {
                mCallBack?.onBrandClick(position)
            }
            //发送品牌
            tvMyMessageChatRvBrandSend.setOnClickListener {
                mCallBack?.onSendBrandClick(position)
            }
        }
    }

    /**
     * ---------------------------------------------别人消息---------------------------------------------
     */

    /**
     * 别人文本(表情)
     */
    inner class OtherTextViewHolder(view: View, viewGroup: ViewGroup) : RecyclerView.ViewHolder(view) {
        private val parent = viewGroup
        private val ivMyMessageChatRvOtherTextHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvOtherTextHead)
        private val tvMyMessageChatRvOtherTextText = itemView.findViewById<TextView>(R.id.tvMyMessageChatRvOtherTextText)
        fun bindViewHolder(position: Int) {
            val textMessage = msgObjectList[position] as TextMessage
            setHeadImage(textMessage, ivMyMessageChatRvOtherTextHead)
            tvMyMessageChatRvOtherTextText.let {
                it.text = SpanStringUtils.getEmotionContent(EmotionUtils.EMOTION_CLASSIC_TYPE, AppManager.instance.currentActivity(), textMessage.info, it, true)
                it.setOnLongClickListener {
                    showPopWindow(position, parent, it)
                    true
                }
            }
        }
    }

    /**
     * 别人图片
     */
    inner class OtherImageViewHolder(view: View, viewGroup: ViewGroup) : RecyclerView.ViewHolder(view) {
        private val parent = viewGroup
        private val ivMyMessageChatRvOtherImageHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvOtherImageHead)
        private val ivMyMessageChatRvOtherImageImage = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvOtherImageImage)
        fun bindViewHolder(position: Int) {
            val imageMessage = msgObjectList[position] as ImageMessage
            setHeadImage(imageMessage, ivMyMessageChatRvOtherImageHead)
            ivMyMessageChatRvOtherImageImage.let {
                val imageSize = ImageUtils.getImageSizeWrap(imageMessage.width, imageMessage.height, context.dp2px(50), context.dp2px(150), context.dp2px(50), context.dp2px(150))
                it.setWidthHeightExt(imageSize.width, imageSize.height)
                //有本地的先加载本地的
                if (FileUtil.isFileExist(imageMessage.path)) {
                    GlideLoader.load(AppManager.instance.currentActivity(), imageMessage.path, it, placeholder = R.drawable.default_img_icon, roundRadius = 15)
                } else {
                    GlideLoader.load(AppManager.instance.currentActivity(), imageMessage.url, it, roundRadius = 15, placeholder = R.drawable.default_img_icon)
                }
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
     * 别人gif
     */
    inner class OtherGifViewHolder(view: View, viewGroup: ViewGroup) : RecyclerView.ViewHolder(view) {
        private val parent = viewGroup
        private val ivMyMessageChatRvOtherGifHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvOtherGifHead)
        private val ivMyMessageChatRvOtherGifImage = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvOtherGifImage)
        fun bindViewHolder(position: Int) {
            val imageMessage = msgObjectList[position] as ImageMessage
            setHeadImage(imageMessage, ivMyMessageChatRvOtherGifHead)
            ivMyMessageChatRvOtherGifImage.let {
                //有本地的先加载本地的
                if (FileUtil.isFileExist(imageMessage.path)) {
                    GlideLoader.load(AppManager.instance.currentActivity(), imageMessage.path, it, placeholder = R.drawable.default_img_icon, roundRadius = 15)
                } else {
                    GlideLoader.load(AppManager.instance.currentActivity(), imageMessage.url, it, placeholder = R.drawable.default_img_icon, roundRadius = 15)
                }
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
     * 别人语音
     */
    inner class OtherAudioViewHolder(view: View, viewGroup: ViewGroup) : RecyclerView.ViewHolder(view) {
        private val parent = viewGroup
        private val ivMyMessageChatRvOtherAudioHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvOtherAudioHead)
        private val tvMyMessageChatRvOtherAudioTime = itemView.findViewById<TextView>(R.id.tvMyMessageChatRvOtherAudioTime)
        private val llMyMessageChatRvOtherAudio = itemView.findViewById<LinearLayout>(R.id.llMyMessageChatRvOtherAudio)
        private val tvMyMessageChatRvOtherAudio = itemView.findViewById<TextView>(R.id.tvMyMessageChatRvOtherAudio)
        private val ivMyMessageChatRvOtherAudioImage = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvOtherAudioImage)
        fun bindViewHolder(position: Int) {
            val audioMessage = msgObjectList[position] as AudioMessage
            setHeadImage(audioMessage, ivMyMessageChatRvOtherAudioHead)
            tvMyMessageChatRvOtherAudioTime.text = "${audioMessage.audiolength}\""
            tvMyMessageChatRvOtherAudio.text = audioLengthToContent(audioMessage.audiolength)
            llMyMessageChatRvOtherAudio.let {
                it.setOnClickListener {
                    audioClick(audioMessage, ivMyMessageChatRvOtherAudioImage)
                    audioMessage.readStatus = MessageConstant.UP_OSS_READED
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
     * 别人视频
     */
    inner class OtherVideoViewHolder(view: View, viewGroup: ViewGroup) : RecyclerView.ViewHolder(view) {
        private val parent = viewGroup
        private val ivMyMessageChatRvOtherVideoHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvOtherVideoHead)
        private val ivMyMessageChatRvOtherVideoCover = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvOtherVideoCover)
        private val rlMyMessageChatRvOtherVideo = itemView.findViewById<RelativeLayout>(R.id.rlMyMessageChatRvOtherVideo)
        private val tvMyMessageChatRvOtherVideoTime = itemView.findViewById<TextView>(R.id.tvMyMessageChatRvOtherVideoTime)
        fun bindViewHolder(position: Int) {
            val videoMessage = msgObjectList[position] as VideoMessage
            setHeadImage(videoMessage, ivMyMessageChatRvOtherVideoHead)
            //视频地址 有本地的用本地的
            val videoUrl = if (!TextUtils.isEmpty(videoMessage.path) && File(videoMessage.path).exists()) {
                videoMessage.path
            } else {
                videoMessage.url
            }
            //设置封面
            if (videoMessage.thumbPath == "null" && videoMessage.thumbUrl == "null") {    //没有封面  取视频第一帧
                val bitmap = GlideCacheUtils.createVideoThumbnail(context, videoUrl, MediaStore.Images.Thumbnails.MINI_KIND)
                ivMyMessageChatRvOtherVideoCover.setImageBitmap(bitmap)
            } else {    //有地址 就直接加载就行了
                if (FileUtil.isFileExist(videoMessage.thumbPath)) {
                    Glide.with(AppManager.instance.currentActivity()).load(videoMessage.thumbPath).into(ivMyMessageChatRvOtherVideoCover)
//                    GlideLoader.load(AppManager.instance.currentActivity(), videoMessage.thumbPath, ivMyMessageChatRvOtherVideoCover, placeholder = R.drawable.default_img_icon, roundRadius = 15)
                } else {
                    Glide.with(AppManager.instance.currentActivity()).load(videoMessage.thumbPath).into(ivMyMessageChatRvOtherVideoCover)
//                    GlideLoader.load(AppManager.instance.currentActivity(), videoMessage.thumbUrl, ivMyMessageChatRvOtherVideoCover, placeholder = R.drawable.default_img_icon, roundRadius = 15)
                }
            }
            //时间
            tvMyMessageChatRvOtherVideoTime.text = "${videoMessage.videolength}s"
            rlMyMessageChatRvOtherVideo.let {
                it.setOnClickListener {
                    PictureSelector.create(context as Activity).externalPictureVideo(videoUrl)
//                    val videoUri = Uri.parse(videoUrl)
//                    videoUri?.let {
//                        //调用系统自带的播放器
//                        val intent = Intent(Intent.ACTION_VIEW)
//                        intent.setDataAndType(it, "video/mp4")
//                        context.startActivity(intent)
//                    }
                }
                it.setOnLongClickListener {
                    showPopWindow(position, parent, it)
                    true
                }
            }
        }
    }

    /**
     * 别人品牌详情
     */
    inner class OtherBrandViewHolder(view: View, viewGroup: ViewGroup) : RecyclerView.ViewHolder(view) {
        private val parent = viewGroup
        private val ivMyMessageChatRvOtherBrandHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvOtherBrandHead)
        private val llMyMessageChatRvOtherBrand = itemView.findViewById<LinearLayout>(R.id.llMyMessageChatRvOtherBrand)
        private val ivMyMessageChatRvOtherBrandLogo = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvOtherBrandLogo)
        private val tvMyMessageChatRvOtherBrandName = itemView.findViewById<TextView>(R.id.tvMyMessageChatRvOtherBrandName)
        private val tvMyMessageChatRvOtherBrandAmount = itemView.findViewById<TextView>(R.id.tvMyMessageChatRvOtherBrandAmount)
        fun bindViewHolder(position: Int) {
            val brandMessage = msgObjectList[position] as BrandMessage
            setHeadImage(brandMessage, ivMyMessageChatRvOtherBrandHead)
            GlideLoader.load(AppManager.instance.currentActivity(), brandMessage.brandLogo, ivMyMessageChatRvOtherBrandLogo, placeholder = R.drawable.default_img_icon)
            tvMyMessageChatRvOtherBrandName.text = brandMessage.brandName
            if (brandMessage.brandValue.isNullOrBlank()) {
                tvMyMessageChatRvOtherBrandAmount.text = "面议"
            } else {
                tvMyMessageChatRvOtherBrandAmount.text = brandMessage.brandValue
            }
            //点击事件
            llMyMessageChatRvOtherBrand.let {
                it.setOnClickListener {
                    mCallBack?.onBrandClick(position)
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
     * 自己文本(表情)
     */
    inner class MineTextViewHolder(view: View, viewGroup: ViewGroup) : RecyclerView.ViewHolder(view) {
        private val parent = viewGroup
        private val ivMyMessageChatRvMineTextHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineTextHead)
        private val tvMyMessageChatRvMineTextText = itemView.findViewById<TextView>(R.id.tvMyMessageChatRvMineTextText)
        private val pbMyMessageChatRvMineTextProgress = itemView.findViewById<ProgressBar>(R.id.pbMyMessageChatRvMineTextProgress)
        private val ivMyMessageChatRvMineTextFail = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineTextFail)
        fun bindViewHolder(position: Int) {
            val textMessage = msgObjectList[position] as TextMessage
            setHeadImage(textMessage, ivMyMessageChatRvMineTextHead)
            tvMyMessageChatRvMineTextText.let {
                it.text = SpanStringUtils.getEmotionContent(EmotionUtils.EMOTION_CLASSIC_TYPE, AppManager.instance.currentActivity(), textMessage.info, it, true)
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
     * 自己图片
     */
    inner class MineImageViewHolder(view: View, viewGroup: ViewGroup) : RecyclerView.ViewHolder(view) {
        private val parent = viewGroup
        private val ivMyMessageChatRvMineImageHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineImageHead)
        private val ivMyMessageChatRvMineImageImage = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineImageImage)
        private val pbMyMessageChatRvMineImageProgress = itemView.findViewById<ProgressBar>(R.id.pbMyMessageChatRvMineImageProgress)
        private val ivMyMessageChatRvMineImageFail = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineImageFail)
        fun bindViewHolder(position: Int) {
            val imageMessage = msgObjectList[position] as ImageMessage
            setHeadImage(imageMessage, ivMyMessageChatRvMineImageHead)
            ivMyMessageChatRvMineImageImage.let {
                val imageSize = ImageUtils.getImageSizeWrap(imageMessage.width, imageMessage.height, context.dp2px(50), context.dp2px(150), context.dp2px(50), context.dp2px(150))
                it.setWidthHeightExt(imageSize.width, imageSize.height)
                //有本地的先加载本地的
                if (FileUtil.isFileExist(imageMessage.path)) {
                    GlideLoader.load(AppManager.instance.currentActivity(), imageMessage.path, it, placeholder = R.drawable.default_img_icon, roundRadius = 15)
                } else {
                    GlideLoader.load(AppManager.instance.currentActivity(), imageMessage.url, it, placeholder = R.drawable.default_img_icon, roundRadius = 15)
                }
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
            when (imageMessage.status) {
                MessageConstant.MSG_SENDING -> {  //发送中
                    pbMyMessageChatRvMineImageProgress.visibility = View.VISIBLE
                    ivMyMessageChatRvMineImageFail.visibility = View.GONE
                }
                MessageConstant.MSG_SUCCESS -> {   //发送成功
                    pbMyMessageChatRvMineImageProgress.visibility = View.GONE
                    ivMyMessageChatRvMineImageFail.visibility = View.GONE
                }
                MessageConstant.MSG_FAILURE -> {   //发送失败
                    pbMyMessageChatRvMineImageProgress.visibility = View.GONE
                    ivMyMessageChatRvMineImageFail.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * 自己gif
     */
    inner class MineGifViewHolder(view: View, viewGroup: ViewGroup) : RecyclerView.ViewHolder(view) {
        private val parent = viewGroup
        private val ivMyMessageChatRvMineGifHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineGifHead)
        private val ivMyMessageChatRvMineGifImage = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineGifImage)
        private val pbMyMessageChatRvMineGifProgress = itemView.findViewById<ProgressBar>(R.id.pbMyMessageChatRvMineGifProgress)
        private val ivMyMessageChatRvMineGifFail = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineGifFail)
        fun bindViewHolder(position: Int) {
            val imageMessage = msgObjectList[position] as ImageMessage
            setHeadImage(imageMessage, ivMyMessageChatRvMineGifHead)
            ivMyMessageChatRvMineGifImage.let {
                //有本地的先加载本地的
                if (FileUtil.isFileExist(imageMessage.path)) {
                    GlideLoader.load(AppManager.instance.currentActivity(), imageMessage.path, it, placeholder = R.drawable.default_img_icon, roundRadius = 15)
                } else {
                    GlideLoader.load(AppManager.instance.currentActivity(), imageMessage.url, it, placeholder = R.drawable.default_img_icon, roundRadius = 15)
                }
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
            when (imageMessage.status) {
                MessageConstant.MSG_SENDING -> {  //发送中
                    pbMyMessageChatRvMineGifProgress.visibility = View.VISIBLE
                    ivMyMessageChatRvMineGifFail.visibility = View.GONE
                }
                MessageConstant.MSG_SUCCESS -> {   //发送成功
                    pbMyMessageChatRvMineGifProgress.visibility = View.GONE
                    ivMyMessageChatRvMineGifFail.visibility = View.GONE
                }
                MessageConstant.MSG_FAILURE -> {   //发送失败
                    pbMyMessageChatRvMineGifProgress.visibility = View.GONE
                    ivMyMessageChatRvMineGifFail.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * 自己语音
     */
    inner class MineAudioViewHolder(view: View, viewGroup: ViewGroup) : RecyclerView.ViewHolder(view) {
        private val parent = viewGroup
        private val ivMyMessageChatRvMineAudioHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineAudioHead)
        private val tvMyMessageChatRvMineAudioTime = itemView.findViewById<TextView>(R.id.tvMyMessageChatRvMineAudioTime)
        private val llMyMessageChatRvMineAudio = itemView.findViewById<LinearLayout>(R.id.llMyMessageChatRvMineAudio)
        private val viewMyMessageChatRvMineAudio = itemView.findViewById<TextView>(R.id.tvMyMessageChatRvMineAudio)
        private val ivMyMessageChatRvMineAudioImage = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineAudioImage)
        private val pbMyMessageChatRvMineAudioProgress = itemView.findViewById<ProgressBar>(R.id.pbMyMessageChatRvMineAudioProgress)
        private val ivMyMessageChatRvMineAudioFail = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineAudioFail)
        fun bindViewHolder(position: Int) {
            val audioMessage = msgObjectList[position] as AudioMessage
            setHeadImage(audioMessage, ivMyMessageChatRvMineAudioHead)
            tvMyMessageChatRvMineAudioTime.text = "${audioMessage.audiolength}\""
            viewMyMessageChatRvMineAudio.text = audioLengthToContent(audioMessage.audiolength)
            llMyMessageChatRvMineAudio.let {
                it.setOnClickListener {
                    audioClick(audioMessage, ivMyMessageChatRvMineAudioImage)
                    audioMessage.readStatus = MessageConstant.UP_OSS_READED
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
     * 自己视频
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
            setHeadImage(videoMessage, ivMyMessageChatRvMineVideoHead)
            //视频地址 有本地的用本地的
            val videoUrl = if (!TextUtils.isEmpty(videoMessage.path) && File(videoMessage.path).exists()) {
                videoMessage.path
            } else {
                videoMessage.url
            }
            //设置封面
            if (videoMessage.thumbPath == "null" && videoMessage.thumbUrl == "null") {    //没有封面  取视频第一帧
                val bitmap = GlideCacheUtils.createVideoThumbnail(context, videoUrl, MediaStore.Images.Thumbnails.MINI_KIND)
                ivMyMessageChatRvMineVideoCover.setImageBitmap(bitmap)
            } else {    //有地址 就直接加载就行了
                if (FileUtil.isFileExist(videoMessage.thumbPath)) {
                    Glide.with(AppManager.instance.currentActivity()).load(videoMessage.thumbPath).into(ivMyMessageChatRvMineVideoCover)
//                    GlideLoader.load(AppManager.instance.currentActivity(), videoMessage.thumbPath, ivMyMessageChatRvMineVideoCover, placeholder = R.drawable.default_img_icon, roundRadius = 15)
                } else {
                    Glide.with(AppManager.instance.currentActivity()).load(videoMessage.thumbUrl).into(ivMyMessageChatRvMineVideoCover)
//                    GlideLoader.load(AppManager.instance.currentActivity(), videoMessage.thumbUrl, ivMyMessageChatRvMineVideoCover, placeholder = R.drawable.default_img_icon, roundRadius = 15)
                }
            }
            //时间
            tvMyMessageChatRvMineVideoTime.text = "${videoMessage.videolength}s"
            rlMyMessageChatRvMineVideo.let {
                it.setOnClickListener {
                    PictureSelector.create(context as Activity).externalPictureVideo(videoUrl)
//                    val videoUri = Uri.parse(videoUrl)
//                    videoUri?.let {
//                        //调用系统自带的播放器
//                        val intent = Intent(Intent.ACTION_VIEW)
//                        intent.setDataAndType(it, "video/mp4")
//                        context.startActivity(intent)
//                    }
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
            when (videoMessage.status) {
                MessageConstant.MSG_SENDING -> {  //发送中
                    pbMyMessageChatRvMineVideoProgress.visibility = View.VISIBLE
                    ivMyMessageChatRvMineVideoFail.visibility = View.GONE
                }
                MessageConstant.MSG_SUCCESS -> {   //发送成功
                    pbMyMessageChatRvMineVideoProgress.visibility = View.GONE
                    ivMyMessageChatRvMineVideoFail.visibility = View.GONE
                }
                MessageConstant.MSG_FAILURE -> {   //发送失败
                    pbMyMessageChatRvMineVideoProgress.visibility = View.GONE
                    ivMyMessageChatRvMineVideoFail.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * 自己品牌详情
     */
    inner class MineBrandViewHolder(view: View, viewGroup: ViewGroup) : RecyclerView.ViewHolder(view) {
        private val parent = viewGroup
        private val ivMyMessageChatRvMineBrandHead = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineBrandHead)
        private val llMyMessageChatRvMineBrand = itemView.findViewById<LinearLayout>(R.id.llMyMessageChatRvMineBrand)
        private val ivMyMessageChatRvMineBrandLogo = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineBrandLogo)
        private val tvMyMessageChatRvMineBrandName = itemView.findViewById<TextView>(R.id.tvMyMessageChatRvMineBrandName)
        private val tvMyMessageChatRvMineBrandAmount = itemView.findViewById<TextView>(R.id.tvMyMessageChatRvMineBrandAmount)
        private val pbMyMessageChatRvMineBrandProgress = itemView.findViewById<ProgressBar>(R.id.pbMyMessageChatRvMineBrandProgress)
        private val ivMyMessageChatRvMineBrandFail = itemView.findViewById<ImageView>(R.id.ivMyMessageChatRvMineBrandFail)
        fun bindViewHolder(position: Int) {
            val brandMessage = msgObjectList[position] as BrandMessage
            setHeadImage(brandMessage, ivMyMessageChatRvMineBrandHead)
            GlideLoader.load(AppManager.instance.currentActivity(), brandMessage.brandLogo, ivMyMessageChatRvMineBrandLogo, placeholder = R.drawable.default_img_icon)
            tvMyMessageChatRvMineBrandName.text = brandMessage.brandName
            if (brandMessage.brandValue.isNullOrBlank()) {
                tvMyMessageChatRvMineBrandAmount.text = "面议"
            } else {
                tvMyMessageChatRvMineBrandAmount.text = brandMessage.brandValue
            }
            //点击事件
            llMyMessageChatRvMineBrand.let {
                it.setOnClickListener {
                    mCallBack?.onBrandClick(position)
                }
                it.setOnLongClickListener {
                    showPopWindow(position, parent, it)
                    true
                }
            }
            //失败点击
            ivMyMessageChatRvMineBrandFail.setOnClickListener {
                showPopWindow(position, parent, it)
            }
            //消息发送状态
            when (brandMessage.status) {
                MessageConstant.MSG_SENDING -> {  //发送中
                    pbMyMessageChatRvMineBrandProgress.visibility = View.VISIBLE
                    ivMyMessageChatRvMineBrandFail.visibility = View.GONE
                }
                MessageConstant.MSG_SUCCESS -> {   //发送成功
                    pbMyMessageChatRvMineBrandProgress.visibility = View.GONE
                    ivMyMessageChatRvMineBrandFail.visibility = View.GONE
                }
                MessageConstant.MSG_FAILURE -> {   //发送失败
                    pbMyMessageChatRvMineBrandProgress.visibility = View.GONE
                    ivMyMessageChatRvMineBrandFail.visibility = View.VISIBLE
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
        //撤回的消息
        if (msg.infoType == DBConstant.SHOW_REVOKE_TYPE) {
            updateRevokeMsg(msg)
        }
        mLayoutManager?.let {
            notifyDataSetChanged()
            it.scrollToPosition(msgObjectList.lastIndex)
        }
    }

    /**
     * 删除一条消息
     */
    fun removeMsg(messageEntity: MessageEntity) {
        //根据消息Id删除数据库内容
        mImService!!.dbInterface!!.deleteMessageByMsgId(messageEntity.msgId)
        msgObjectList.remove(messageEntity)
        notifyDataSetChanged()
    }

    /**
     * 撤回一条消息
     */
    fun revokeMsg(entity: MessageEntity, newEntity: MessageEntity, position: Int) {
        newEntity.created = entity.created
        msgObjectList.add(position, newEntity)
        msgObjectList.remove(entity)
        notifyDataSetChanged()
    }

    /**
     * 修改新收到的消息
     */
    private fun updateRevokeMsg(entity: MessageEntity) {
        val txtMsg = entity as TextMessage
        msgObjectList.forEachIndexed { index, msg ->
            if (msg is MessageEntity && msg.msgId == txtMsg.getAttributeInt(MessageExtConst.MSGID)) {
                txtMsg.created = msg.created
                msgObjectList.remove(entity)
                msgObjectList.add(index, txtMsg)
                msgObjectList.remove(msg)
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
        if (textMessage.fromId == loginUser?.peerId) {  //自己
            userEntity = loginUser
        } else {
            userEntity = mImService?.contactManager?.findContact(textMessage.fromId)
        }
        return userEntity ?: UserEntity()
    }

    /**
     * 下拉载入历史消息,从最上面开始添加
     */
    fun loadHistoryList(historyList: List<MessageEntity>?, mLayoutManager: LinearLayoutManager? = null) {
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
        mLayoutManager?.scrollToPositionWithOffset(chatList.lastIndex + 1, 0)
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
                    } else if (txtMsg.contentEntity.infoType == DBConstant.SHOW_REVOKE_TYPE) {  //撤回
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
                DBConstant.SHOW_REVOKE_TYPE -> {  //撤回
                    type = if (isMine)
                        RenderType.MESSAGE_TYPE_MINE_REVOKE
                    else
                        RenderType.MESSAGE_TYPE_OTHER_REVOKE
                }
                DBConstant.SHOW_BRAND_TYPE -> { //品牌详情
                    val brandMsg = obj as BrandMessage
                    //没有fromId或toId 则说明是公共的
                    if (brandMsg.fromId == 0 || brandMsg.toId == 0) {   //公共
                        type = RenderType.MESSAGE_TYPE_BRAND
                    } else {    //我的或他人的
                        type = if (isMine) {
                            RenderType.MESSAGE_TYPE_MINE_BRAND
                        } else {
                            RenderType.MESSAGE_TYPE_OTHER_BRAND
                        }
                    }
                }
                else -> {
                }
            }
        }
        return type.ordinal
    }

    /**
     * 设置头像
     */
    private fun setHeadImage(message: MessageEntity, imageView: ImageView) {
        //直接加载头像（此消息是假消息）
        if (message.fromId == 0) {
            GlideLoader.load(AppManager.instance.currentActivity(), MyMessageChatActivity.mAvatar, imageView, placeholder = R.drawable.default_img_icon, roundRadius = 15)
        } else {
            val isMine = message.fromId == loginUser?.peerId
            val userEntity = getUserEntity(message)
            var avatarUrl = userEntity.avatar
            if (isMine && TextUtils.isEmpty(avatarUrl)) {
                avatarUrl = MainApplication.instance.user.userInfo.avatar
            }
            GlideLoader.load(AppManager.instance.currentActivity(), avatarUrl, imageView, placeholder = R.drawable.default_img_icon, roundRadius = 15)
        }
    }

    /**
     * 根据时间长度返回一个空字符串
     */
    private fun audioLengthToContent(length: Int): String {
        val num = if (length <= 40) length else 40
        var str = ""
        for (i in 0..num) {
            str += " "
        }
        return str
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
                when (mType) {
                    DBConstant.SHOW_IMAGE_TYPE -> { //图片
                        val imageMessage = mMsgInfo as ImageMessage
                        if (!File(imageMessage.path).exists()) {
                            ToastUtil.showShort(com.leimo.wanxin.R.string.image_path_unavaluable)
                            return
                        }
                    }
                    DBConstant.SHOW_AUDIO_TYPE -> { //语音
                        val audioMessage = mMsgInfo as AudioMessage
                        if (!File(audioMessage.audioPath).exists() || audioMessage.sendContent!!.size < 4) {
                            ToastUtil.showShort("路径错误")
                            return
                        }
                    }
                    DBConstant.SHOW_VIDEO_TYPE -> { //视频
                        val videoMessage = mMsgInfo as VideoMessage
                        if (!File(videoMessage.path).exists()) {
                            ToastUtil.showShort(com.leimo.wanxin.R.string.image_path_unavaluable)
                            return
                        }
                    }
                }
                if (mImService != null) {
                    mMsgInfo.status = MessageConstant.MSG_SENDING
                    msgObjectList.removeAt(mPosition)
                    addItem(mMsgInfo)
                    notifyDataSetChanged()
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
            mCallBack?.onRevokeClick(position)
        }

        //删除
        override fun onDeleteClick(position: Int) {
            mCallBack?.onDeleteClick(position)
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
        val bResend = message.status == MessageConstant.MSG_FAILURE || message.status == MessageConstant.MSG_SENDING
        //消息是否在2分钟之内创建的
        val bRevoke = (System.currentTimeMillis() / 1000) - message.created < mDefaultTimeDifference
        if (message.fromId == 0) {
            popup.show(view, message.displayType, bResend, isMine, bRevoke, -1)
        } else {
            popup.show(view, message.displayType, bResend, isMine, bRevoke, position)
        }
    }

    fun setCallBack(callBack: CallBack) {
        mCallBack = callBack
    }

    interface CallBack {
        fun onRevokeClick(position: Int)

        fun onDeleteClick(position: Int)

        fun onBrandClick(position: Int)

        fun onSendBrandClick(position: Int)
    }
}