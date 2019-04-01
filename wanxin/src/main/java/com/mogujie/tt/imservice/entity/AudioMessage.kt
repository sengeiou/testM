package com.mogujie.tt.imservice.entity

import com.mogujie.tt.config.DBConstant
import com.mogujie.tt.config.MessageConstant
import com.mogujie.tt.config.MessageExtConst
import com.mogujie.tt.db.entity.MessageEntity
import com.mogujie.tt.db.entity.PeerEntity
import com.mogujie.tt.db.entity.UserEntity
import java.io.Serializable

/**
 * @author : yingmu on 14-12-31.
 * @email : yingmu@mogujie.com.
 */
class AudioMessage() : TextMessage(), Serializable {
    var audioPath = ""
        get() = getAttributeString(MessageExtConst.AUDIO_PATH)
        set(value) {
            setAttribute(MessageExtConst.AUDIO_PATH, value)
            field = value
        }
    var audioUrl = ""
        get() = getAttributeString(MessageExtConst.AUDIO_URL)
        set(value) {
            setAttribute(MessageExtConst.AUDIO_URL, value)
            field = value
        }
    var audiolength = 0
        get() = getAttributeInt(MessageExtConst.AUDIO_LENGTH)
        set(value) {
            setAttribute(MessageExtConst.AUDIO_LENGTH, value)
            field = value
        }
    //语音读取状态
    var readStatus = 0
        get() = getAttributeInt(MessageExtConst.READSTATUS)
        set(value) {
            setAttribute(MessageExtConst.READSTATUS, value)
            field = value
        }
    //语音发送状态
    var sendStatus: Int = 0
        get() = getAttributeInt(MessageExtConst.SENDSTATUS)
        set(value) {
            setAttribute(MessageExtConst.SENDSTATUS, value)
            field = value
        }

    override fun getInfo(): String = DBConstant.DISPLAY_FOR_AUDIO

    constructor(entity: MessageEntity) : this() {
        parseMessage(entity)
    }

    companion object {
        //接受到网络包，解析成本地的数据
        fun parseFromNet(entity: MessageEntity): AudioMessage {
            val audioMessage = AudioMessage(entity)
            audioMessage.setDisplayType(DBConstant.SHOW_AUDIO_TYPE)
            audioMessage.sendStatus = MessageConstant.UP_OSS_UNREAD
            audioMessage.setStatus(MessageConstant.MSG_SUCCESS)
            return audioMessage
        }

        fun parseFromDB(entity: MessageEntity): AudioMessage {
            if (entity.displayType != DBConstant.SHOW_AUDIO_TYPE) {
                throw RuntimeException("#brandMessage# parseFromDB,not SHOW_BRAND_TYPE")
            }
            return AudioMessage(entity)
        }

        fun buildForSend(audioLen: Float, audioSavePath: String, fromUser: UserEntity, peerEntity: PeerEntity): AudioMessage {
            var tLen = (audioLen + 0.5).toInt()
            tLen = if (tLen < 1) 1 else tLen
            if (tLen < audioLen) {
                ++tLen
            }

            val nowTime = (System.currentTimeMillis() / 1000).toInt()
            val audioMessage = AudioMessage()
            audioMessage.setFromId(fromUser.peerId)
            audioMessage.setToId(peerEntity.peerId)
            audioMessage.setCreated(nowTime)
            audioMessage.setUpdated(nowTime)
            val peerType = peerEntity.type
            val msgType = if (peerType == DBConstant.SESSION_TYPE_GROUP)
                DBConstant.MSG_TYPE_GROUP_AUDIO
            else
                DBConstant.MSG_TYPE_SINGLE_AUDIO
            audioMessage.setMsgType(msgType)

            audioMessage.audioPath = audioSavePath
            audioMessage.audiolength = tLen
            //自己发送的就把消息状态设置成已读
            audioMessage.readStatus = MessageConstant.UP_OSS_READED
            audioMessage.setDisplayType(DBConstant.SHOW_AUDIO_TYPE)
            audioMessage.sendStatus = MessageConstant.UP_OSS_LOADING
            audioMessage.setStatus(MessageConstant.MSG_SENDING)
            audioMessage.buildSessionKey(true)
            return audioMessage
        }

        fun buildForSend(audioLen: Float, audioSavePath: String, fromUser: UserEntity, peerId: Int, peerType: Int): AudioMessage {
            var tLen = (audioLen + 0.5).toInt()
            tLen = if (tLen < 1) 1 else tLen
            if (tLen < audioLen) {
                ++tLen
            }

            val nowTime = (System.currentTimeMillis() / 1000).toInt()
            val audioMessage = AudioMessage()
            audioMessage.setFromId(fromUser.peerId)
            audioMessage.setToId(peerId)
            audioMessage.setCreated(nowTime)
            audioMessage.setUpdated(nowTime)
            val peerType = peerType
            val msgType = if (peerType == DBConstant.SESSION_TYPE_GROUP)
                DBConstant.MSG_TYPE_GROUP_AUDIO
            else
                DBConstant.MSG_TYPE_SINGLE_AUDIO
            audioMessage.setMsgType(msgType)

            audioMessage.audioPath = audioSavePath
            audioMessage.audiolength = tLen
            //自己发送的就把消息状态设置成已读
            audioMessage.readStatus = MessageConstant.UP_OSS_READED
            audioMessage.setDisplayType(DBConstant.SHOW_AUDIO_TYPE)
            audioMessage.sendStatus = MessageConstant.UP_OSS_LOADING
            audioMessage.setStatus(MessageConstant.MSG_SENDING)
            audioMessage.buildSessionKey(true)
            return audioMessage
        }
    }
}
