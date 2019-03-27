package com.mogujie.tt.imservice.entity

import com.mogujie.tt.config.DBConstant
import com.mogujie.tt.config.MessageConstant
import com.mogujie.tt.config.MessageExtConst
import com.mogujie.tt.db.entity.MessageEntity
import com.mogujie.tt.db.entity.PeerEntity
import com.mogujie.tt.db.entity.UserEntity
import com.mogujie.tt.imservice.manager.buildSendMessage
import java.io.Serializable

class VideoMessage() : TextMessage(), Serializable {
    var path = ""
        get() = getAttributeString(MessageExtConst.VIDEO_PATH)
        set(value) {
            setAttribute(MessageExtConst.VIDEO_PATH, value)
            field = value
        }
    var url = ""
        get() = getAttributeString(MessageExtConst.VIDEO_URL)
        set(value) {
            setAttribute(MessageExtConst.VIDEO_URL, value)
            field = value
        }
    //本地缩略图地址
    var thumbPath = ""
        get() = getAttributeString(MessageExtConst.VIDEO_THUMB_PATH)
        set(value) {
            setAttribute(MessageExtConst.VIDEO_THUMB_PATH, value)
            field = value
        }
    //网络缩略图地址
    var thumbUrl = ""
        get() = getAttributeString(MessageExtConst.VIDEO_THUMB_URL)
        set(value) {
            setAttribute(MessageExtConst.VIDEO_THUMB_URL, value)
            field = value
        }
    var videolength = 0
        get() = getAttributeInt(MessageExtConst.VIDEO_DURATION)
        set(value) {
            setAttribute(MessageExtConst.VIDEO_DURATION, value)
            field = value
        }
    //发送状态
    var sendStatus: Int = 0
        get() = getAttributeInt(MessageExtConst.SENDSTATUS)
        set(value) {
            setAttribute(MessageExtConst.SENDSTATUS, value)
            field = value
        }

    override fun getInfo(): String = DBConstant.DISPLAY_FOR_VIDEO

    constructor(entity: MessageEntity) : this() {
        parseMessage(entity)
    }

    companion object {
        fun parseFromNet(entity: MessageEntity): VideoMessage {
            val videoMessage = VideoMessage(entity)
            videoMessage.setDisplayType(DBConstant.SHOW_VIDEO_TYPE)
            videoMessage.sendStatus = MessageConstant.UP_OSS_UNREAD
            videoMessage.setStatus(MessageConstant.MSG_SUCCESS)
            return videoMessage
        }

        fun parseFromDB(entity: MessageEntity): VideoMessage {
            if (entity.displayType != DBConstant.SHOW_VIDEO_TYPE) {
                throw RuntimeException("#videoMessage# parseFromDB,not SHOW_video_TYPE")
            }
            return VideoMessage(entity)
        }

        // 消息页面，发送视频消息
        fun buildForSend(videoLen: Float?, videoLocalPath: String?, fromUser: UserEntity?, sessionKey: String?): VideoMessage? {
            if (videoLen == null || videoLocalPath == null || fromUser == null || sessionKey == null) {
                return null
            }
            val peerEntity = PeerEntity.getPeerEntity(sessionKey)
            return VideoMessage().apply {
                buildSendMessage(fromUser, peerEntity, "[视频]", DBConstant.SHOW_VIDEO_TYPE)
                sendStatus = MessageConstant.UP_OSS_LOADING
                status = MessageConstant.MSG_SENDING
                path = videoLocalPath
                videolength = videoLen.toInt()
            }
        }
    }
}
