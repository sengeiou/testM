package com.mogujie.tt.imservice.entity

import com.google.gson.Gson
import com.mogujie.tt.Security
import com.mogujie.tt.config.DBConstant
import com.mogujie.tt.config.MessageConstant
import com.mogujie.tt.db.entity.MessageEntity
import com.mogujie.tt.db.entity.PeerEntity
import com.mogujie.tt.db.entity.UserEntity
import com.mogujie.tt.imservice.manager.buildSendMessage
import com.mogujie.tt.imservice.support.SequenceNumberMaker
import java.io.Serializable
import java.io.UnsupportedEncodingException

/**
 * @author : yingmu on 14-12-31.
 * @email : yingmu@mogujie.com.
 */
open class TextMessage : MessageEntity, Serializable {
    val contentEntity: ContentEntity
        get() = Gson().fromJson(getContent(), ContentEntity::class.java)

    constructor() {
        msgId = SequenceNumberMaker.getInstance().makelocalUniqueMsgId()
    }

    constructor(entity: MessageEntity) {
        parseMessage(entity)
    }

    override fun getContent(): String {
        val contentEntity = ContentEntity()
        contentEntity.info = getInfo()
        contentEntity.extInfo = extContent
        contentEntity.infoType = getDisplayType()
        contentEntity.nickname = getNickname()
        contentEntity.isSpecial = getSpecial()
        return Gson().toJson(contentEntity)
    }

    override fun getSendContent(): ByteArray? {
        try {
            /** 加密 */
            val sendContent = String(Security.getInstance().EncryptMsg(getContent()))
            return sendContent.toByteArray(charset("utf-8"))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        return null
    }

    companion object {
        /**
         * 接受到网络包，解析成本地的数据
         */
        fun parseFromNet(entity: MessageEntity): TextMessage = TextMessage(entity).apply {
            setStatus(MessageConstant.MSG_SUCCESS)
            setDisplayType(entity.infoType)
        }

        fun parseFromDB(entity: MessageEntity): TextMessage = TextMessage(entity)

        fun buildForSend(content: String?, fromUser: UserEntity?, sessionKey: String?): TextMessage? {
            if (content == null || fromUser == null || sessionKey == null) {
                return null
            }
            val peerEntity = PeerEntity.getPeerEntity(sessionKey)
            return TextMessage().apply { buildSendMessage(fromUser, peerEntity, content, DBConstant.SHOW_ORIGIN_TEXT_TYPE) }
        }
    }
}

