package com.qingmeng.mengmeng.adapter

import AppManager
import android.widget.LinearLayout
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
import com.qingmeng.mengmeng.adapter.util.ItemViewDelegate
import com.qingmeng.mengmeng.adapter.util.ViewHolder
import com.qingmeng.mengmeng.utils.imageLoader.GlideLoader
import com.qingmeng.mengmeng.utils.setMarginExt
import java.util.*

/**
 *  Description :聊天多个样式的Adapter

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/9
 */
class ChatAdapter {
    /**
     * 默认布局
     */
    class DefaultLayout : ItemViewDelegate<Any> {
        override fun getItemViewLayoutId(): Int {
            return R.layout.activity_my_message_chat_item_defalut
        }

        override fun isForViewType(item: Any, position: Int): Boolean {
            return true
        }

        override fun convert(holder: ViewHolder, item: Any, position: Int, payloads: List<Any>?) {
            holder.apply {
                when (RenderType.values()[ChatAdapter.getItemViewType(item)]) {
                    RenderType.MESSAGE_TYPE_TIME_TITLE, RenderType.MESSAGE_TYPE_OTHER_REVOKE, RenderType.MESSAGE_TYPE_MINE_REVOKE -> {  //时间
                        if (ChatAdapter.msgObjectList[0] == item) {
                            getView<LinearLayout>(R.id.llMyMessageChatRvAllTime).setMarginExt(top = 30)
                        } else {
                            getView<LinearLayout>(R.id.llMyMessageChatRvAllTime).setMarginExt(top = 0)
                        }
                        when (RenderType.values()[ChatAdapter.getItemViewType(item)]) {
                            RenderType.MESSAGE_TYPE_TIME_TITLE -> {    //时间
                                val timeBubble = item as Int
                                val timeStamp = timeBubble.toLong()
                                val msgTimeDate = Date(timeStamp * 1000)
                                setText(R.id.tvMyMessageChatRvTime, DateUtil.getTimeDiffDesc(msgTimeDate))
                            }
                            RenderType.MESSAGE_TYPE_MINE_REVOKE -> {    //自己撤回
                                setText(R.id.tvMyMessageChatRvTime, "您撤回了一条消息")
                            }
                            RenderType.MESSAGE_TYPE_OTHER_REVOKE -> {    //别人撤回
                                setText(R.id.tvMyMessageChatRvTime, "对方撤回了一条消息")
                            }
                        }
                    }
                    RenderType.MESSAGE_TYPE_INVALID -> {    //品牌详情
                        if (ChatAdapter.msgObjectList[0] == item) {
                            getView<LinearLayout>(R.id.llMyMessageChatRvAllBrand).setMarginExt(top = 30)
                        } else {
                            getView<LinearLayout>(R.id.llMyMessageChatRvAllBrand).setMarginExt(top = 0)
                        }
                    }
                /**
                 * ---------------------------------------------别人消息---------------------------------------------
                 */
                    RenderType.MESSAGE_TYPE_OTHER_TEXT, RenderType.MESSAGE_TYPE_OTHER_GIF_IMAGE -> {  //文本(表情)
                        val textMessage = item as TextMessage
                        val userEntity = ChatAdapter.getUserEntity(textMessage)
                        GlideLoader.load(AppManager.instance.currentActivity(), userEntity.avatar, getView(R.id.ivMyMessageChatRvOtherTextHead), placeholder = R.mipmap.my_settings_aboutus_icon, roundRadius = 15)
                        setText(R.id.tvMyMessageChatRvOtherTextText, textMessage.info)
                    }
                    RenderType.MESSAGE_TYPE_OTHER_IMAGE, RenderType.MESSAGE_TYPE_OTHER_GIF -> {  //图片(gif)
                        val imageMessage = item as ImageMessage
                        val userEntity = ChatAdapter.getUserEntity(imageMessage)
                        GlideLoader.load(AppManager.instance.currentActivity(), userEntity.avatar, getView(R.id.ivMyMessageChatRvOtherImageHead), placeholder = R.mipmap.my_settings_aboutus_icon, roundRadius = 15)
                        GlideLoader.load(AppManager.instance.currentActivity(), imageMessage.url, getView(R.id.ivMyMessageChatRvOtherImageImage), roundRadius = 15)
                    }
                    RenderType.MESSAGE_TYPE_OTHER_AUDIO -> {  //语音
                        val audioMessage = item as AudioMessage
                        val userEntity = ChatAdapter.getUserEntity(audioMessage)
                        GlideLoader.load(AppManager.instance.currentActivity(), userEntity.avatar, getView(R.id.ivMyMessageChatRvOtherAudioHead), placeholder = R.mipmap.my_settings_aboutus_icon, roundRadius = 15)
                        setText(R.id.tvMyMessageChatRvOtherAudioTime, "${audioMessage.audioLength}\"")
                    }
                    RenderType.MESSAGE_TYPE_OTHER_VIDEO -> {  //视频
                        val videoMessage = item as VideoMessage
                        val userEntity = ChatAdapter.getUserEntity(videoMessage)
                        GlideLoader.load(AppManager.instance.currentActivity(), userEntity.avatar, getView(R.id.ivMyMessageChatRvOtherVideoHead), placeholder = R.mipmap.my_settings_aboutus_icon, roundRadius = 15)
                        //封面
                        GlideLoader.load(AppManager.instance.currentActivity(), videoMessage.thumbUrl, getView(R.id.ivMyMessageChatRvOtherVideoCover), roundRadius = 15)
//                        //时间
//                        setText(R.id.tvMyMessageChatRvOtherVideoTime,"")
                    }
                /**
                 * ---------------------------------------------自己消息---------------------------------------------
                 */
                    RenderType.MESSAGE_TYPE_MINE_TEXT, RenderType.MESSAGE_TYPE_MINE_GIF_IMAGE -> {  //文本(表情)
                        val textMessage = item as TextMessage
                        val userEntity = ChatAdapter.getUserEntity(textMessage)
                        GlideLoader.load(AppManager.instance.currentActivity(), userEntity.avatar, getView(R.id.ivMyMessageChatRvMineTextHead), placeholder = R.mipmap.my_settings_aboutus_icon, roundRadius = 15)
                        setText(R.id.tvMyMessageChatRvMineTextText, textMessage.info)
                    }
                    RenderType.MESSAGE_TYPE_MINE_IMAGE, RenderType.MESSAGE_TYPE_MINE_GIF -> {  //图片(gif)
                        val imageMessage = item as ImageMessage
                        val userEntity = ChatAdapter.getUserEntity(imageMessage)
                        GlideLoader.load(AppManager.instance.currentActivity(), userEntity.avatar, getView(R.id.ivMyMessageChatRvMineImageHead), placeholder = R.mipmap.my_settings_aboutus_icon, roundRadius = 15)
                        GlideLoader.load(AppManager.instance.currentActivity(), imageMessage.url, getView(R.id.ivMyMessageChatRvMineImageImage), roundRadius = 15)
                    }
                    RenderType.MESSAGE_TYPE_MINE_AUDIO -> {  //语音
                        val audioMessage = item as AudioMessage
                        val userEntity = ChatAdapter.getUserEntity(audioMessage)
                        GlideLoader.load(AppManager.instance.currentActivity(), userEntity.avatar, getView(R.id.ivMyMessageChatRvMineAudioHead), placeholder = R.mipmap.my_settings_aboutus_icon, roundRadius = 15)
                        setText(R.id.tvMyMessageChatRvMineAudioTime, "${audioMessage.audioLength}\"")
                    }
                    RenderType.MESSAGE_TYPE_MINE_VIDEO -> {  //视频
                        val videoMessage = item as VideoMessage
                        val userEntity = ChatAdapter.getUserEntity(videoMessage)
                        GlideLoader.load(AppManager.instance.currentActivity(), userEntity.avatar, getView(R.id.ivMyMessageChatRvMineVideoHead), placeholder = R.mipmap.my_settings_aboutus_icon, roundRadius = 15)
                        //封面
                        GlideLoader.load(AppManager.instance.currentActivity(), videoMessage.thumbUrl, getView(R.id.ivMyMessageChatRvMineVideoCover), roundRadius = 15)
//                        //时间
//                        setText(R.id.tvMyMessageChatRvMineVideoTime,"")
                    }
                }
            }
        }
    }

    /**
     * 时间布局
     */
    class TimeLayout : ItemViewDelegate<Any> {
        private var renderType: RenderType? = null
        override fun getItemViewLayoutId(): Int {
            return R.layout.activity_my_message_chat_item_time
        }

        override fun isForViewType(item: Any, position: Int): Boolean {
            renderType = RenderType.values()[ChatAdapter.getItemViewType(item)]
            return renderType == RenderType.MESSAGE_TYPE_TIME_TITLE
                    || renderType == RenderType.MESSAGE_TYPE_OTHER_REVOKE
                    || renderType == RenderType.MESSAGE_TYPE_MINE_REVOKE
        }

        override fun convert(holder: ViewHolder, item: Any, position: Int, payloads: List<Any>?) {

        }
    }

    /**
     * 品牌详情
     */
    class BrandLayout : ItemViewDelegate<Any> {
        override fun getItemViewLayoutId(): Int {
            return R.layout.activity_my_message_chat_item_brand
        }

        override fun isForViewType(item: Any, position: Int): Boolean {
            return RenderType.values()[ChatAdapter.getItemViewType(item)] == RenderType.MESSAGE_TYPE_INVALID
        }

        override fun convert(holder: ViewHolder, item: Any, position: Int, payloads: List<Any>?) {

        }
    }

    /**
     * ---------------------------------------------别人消息---------------------------------------------
     */

    /**
     * 文本(表情)
     */
    class OtherTextLayout : ItemViewDelegate<Any> {
        override fun getItemViewLayoutId(): Int {
            return R.layout.activity_my_message_chat_item_other_text
        }

        override fun isForViewType(item: Any, position: Int): Boolean {
            return RenderType.values()[ChatAdapter.getItemViewType(item)] == RenderType.MESSAGE_TYPE_OTHER_TEXT
                    || RenderType.values()[ChatAdapter.getItemViewType(item)] == RenderType.MESSAGE_TYPE_OTHER_GIF_IMAGE
        }

        override fun convert(holder: ViewHolder, item: Any, position: Int, payloads: List<Any>?) {

        }
    }

    /**
     * 图片(gif)
     */
    class OtherImageLayout : ItemViewDelegate<Any> {
        override fun getItemViewLayoutId(): Int {
            return R.layout.activity_my_message_chat_item_other_image
        }

        override fun isForViewType(item: Any, position: Int): Boolean {
            return RenderType.values()[ChatAdapter.getItemViewType(item)] == RenderType.MESSAGE_TYPE_OTHER_IMAGE
                    || RenderType.values()[ChatAdapter.getItemViewType(item)] == RenderType.MESSAGE_TYPE_OTHER_GIF
        }

        override fun convert(holder: ViewHolder, item: Any, position: Int, payloads: List<Any>?) {

        }
    }

    /**
     * 语音
     */
    class OtherAudioLayout : ItemViewDelegate<Any> {
        override fun getItemViewLayoutId(): Int {
            return R.layout.activity_my_message_chat_item_other_audio
        }

        override fun isForViewType(item: Any, position: Int): Boolean {
            return RenderType.values()[ChatAdapter.getItemViewType(item)] == RenderType.MESSAGE_TYPE_OTHER_AUDIO
        }

        override fun convert(holder: ViewHolder, item: Any, position: Int, payloads: List<Any>?) {

        }
    }

    /**
     * 视频
     */
    class OtherVideoLayout : ItemViewDelegate<Any> {
        override fun getItemViewLayoutId(): Int {
            return R.layout.activity_my_message_chat_item_other_video
        }

        override fun isForViewType(item: Any, position: Int): Boolean {
            return RenderType.values()[ChatAdapter.getItemViewType(item)] == RenderType.MESSAGE_TYPE_OTHER_VIDEO
        }

        override fun convert(holder: ViewHolder, item: Any, position: Int, payloads: List<Any>?) {

        }
    }

    /**
     * ---------------------------------------------自己消息---------------------------------------------
     */

    /**
     * 文本(表情)
     */
    class MineTextLayout : ItemViewDelegate<Any> {
        override fun getItemViewLayoutId(): Int {
            return R.layout.activity_my_message_chat_item_mine_text
        }

        override fun isForViewType(item: Any, position: Int): Boolean {
            return RenderType.values()[ChatAdapter.getItemViewType(item)] == RenderType.MESSAGE_TYPE_MINE_TEXT
                    || RenderType.values()[ChatAdapter.getItemViewType(item)] == RenderType.MESSAGE_TYPE_MINE_GIF_IMAGE
        }

        override fun convert(holder: ViewHolder, item: Any, position: Int, payloads: List<Any>?) {

        }
    }

    /**
     * 图片(gif)
     */
    class MineImageLayout : ItemViewDelegate<Any> {
        override fun getItemViewLayoutId(): Int {
            return R.layout.activity_my_message_chat_item_mine_image
        }

        override fun isForViewType(item: Any, position: Int): Boolean {
            return RenderType.values()[ChatAdapter.getItemViewType(item)] == RenderType.MESSAGE_TYPE_MINE_IMAGE
                    || RenderType.values()[ChatAdapter.getItemViewType(item)] == RenderType.MESSAGE_TYPE_MINE_GIF
        }

        override fun convert(holder: ViewHolder, item: Any, position: Int, payloads: List<Any>?) {

        }
    }

    /**
     * 语音
     */
    class MineAudioLayout : ItemViewDelegate<Any> {
        override fun getItemViewLayoutId(): Int {
            return R.layout.activity_my_message_chat_item_mine_audio
        }

        override fun isForViewType(item: Any, position: Int): Boolean {
            return RenderType.values()[ChatAdapter.getItemViewType(item)] == RenderType.MESSAGE_TYPE_MINE_AUDIO
        }

        override fun convert(holder: ViewHolder, item: Any, position: Int, payloads: List<Any>?) {

        }
    }

    /**
     * 视频
     */
    class MineVideoLayout : ItemViewDelegate<Any> {
        override fun getItemViewLayoutId(): Int {
            return R.layout.activity_my_message_chat_item_mine_video
        }

        override fun isForViewType(item: Any, position: Int): Boolean {
            return RenderType.values()[ChatAdapter.getItemViewType(item)] == RenderType.MESSAGE_TYPE_MINE_VIDEO
        }

        override fun convert(holder: ViewHolder, item: Any, position: Int, payloads: List<Any>?) {

        }
    }

    companion object {
        private var mImService: IMService? = null
        private var loginUser: UserEntity? = null
        var msgObjectList = ArrayList<Any>()

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

        /**
         * 取用户信息
         */
        fun getUserEntity(textMessage: MessageEntity): UserEntity {
            val userEntity: UserEntity?
            if (textMessage.fromId == loginUser?.peerId) {//自己
                userEntity = loginUser
            } else {
                userEntity = mImService?.contactManager?.findContact(textMessage.fromId)
            }
            return userEntity!!
        }

//        //显示别人View方法    1.文本 2.图片 3.语音 4.视频
//        private fun setShowOtherView(holder: ViewHolder, position: Int) {
//            holder.apply {
//                when (position) {
//                    1 -> {
//                        getView<TextView>(R.id.tvMyMessageChatRvOtherText).visibility = View.VISIBLE
//                        getView<ImageView>(R.id.ivMyMessageChatRvOtherImage).visibility = View.GONE
//                        getView<LinearLayout>(R.id.llMyMessageChatRvOtherAudio).visibility = View.GONE
//                        getView<RelativeLayout>(R.id.rlMyMessageChatRvOtherVideo).visibility = View.GONE
//                    }
//                    2 -> {
//                        getView<TextView>(R.id.tvMyMessageChatRvOtherText).visibility = View.GONE
//                        getView<ImageView>(R.id.ivMyMessageChatRvOtherImage).visibility = View.VISIBLE
//                        getView<LinearLayout>(R.id.llMyMessageChatRvOtherAudio).visibility = View.GONE
//                        getView<RelativeLayout>(R.id.rlMyMessageChatRvOtherVideo).visibility = View.GONE
//                    }
//                    3 -> {
//                        getView<TextView>(R.id.tvMyMessageChatRvOtherText).visibility = View.GONE
//                        getView<ImageView>(R.id.ivMyMessageChatRvOtherImage).visibility = View.GONE
//                        getView<LinearLayout>(R.id.llMyMessageChatRvOtherAudio).visibility = View.VISIBLE
//                        getView<RelativeLayout>(R.id.rlMyMessageChatRvOtherVideo).visibility = View.GONE
//                    }
//                    4 -> {
//                        getView<TextView>(R.id.tvMyMessageChatRvOtherText).visibility = View.GONE
//                        getView<ImageView>(R.id.ivMyMessageChatRvOtherImage).visibility = View.GONE
//                        getView<LinearLayout>(R.id.llMyMessageChatRvOtherAudio).visibility = View.GONE
//                        getView<RelativeLayout>(R.id.rlMyMessageChatRvOtherVideo).visibility = View.VISIBLE
//                    }
//                }
//            }
//        }
//
//        //显示自己View方法    1.文本 2.图片 3.语音 4.视频
//        private fun setShowMineView(holder: ViewHolder, position: Int) {
//            holder.apply {
//                when (position) {
//                    1 -> {
//                        getView<TextView>(R.id.tvMyMessageChatRvMineText).visibility = View.VISIBLE
//                        getView<ImageView>(R.id.ivMyMessageChatRvMineImage).visibility = View.GONE
//                        getView<LinearLayout>(R.id.llMyMessageChatRvMineAudio).visibility = View.GONE
//                        getView<RelativeLayout>(R.id.rlMyMessageChatRvMineVideo).visibility = View.GONE
//                    }
//                    2 -> {
//                        getView<TextView>(R.id.tvMyMessageChatRvMineText).visibility = View.GONE
//                        getView<ImageView>(R.id.ivMyMessageChatRvMineImage).visibility = View.VISIBLE
//                        getView<LinearLayout>(R.id.llMyMessageChatRvMineAudio).visibility = View.GONE
//                        getView<RelativeLayout>(R.id.rlMyMessageChatRvMineVideo).visibility = View.GONE
//                    }
//                    3 -> {
//                        getView<TextView>(R.id.tvMyMessageChatRvMineText).visibility = View.GONE
//                        getView<ImageView>(R.id.ivMyMessageChatRvMineImage).visibility = View.GONE
//                        getView<LinearLayout>(R.id.llMyMessageChatRvMineAudio).visibility = View.VISIBLE
//                        getView<RelativeLayout>(R.id.rlMyMessageChatRvMineVideo).visibility = View.GONE
//                    }
//                    4 -> {
//                        getView<TextView>(R.id.tvMyMessageChatRvMineText).visibility = View.GONE
//                        getView<ImageView>(R.id.ivMyMessageChatRvMineImage).visibility = View.GONE
//                        getView<LinearLayout>(R.id.llMyMessageChatRvMineAudio).visibility = View.GONE
//                        getView<RelativeLayout>(R.id.rlMyMessageChatRvMineVideo).visibility = View.VISIBLE
//                    }
//                }
//            }
//        }
    }
}
