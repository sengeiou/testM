package com.mogujie.tt.imservice.manager

import com.mogujie.tt.config.DBConstant
import com.mogujie.tt.config.MessageConstant
import com.mogujie.tt.db.entity.MessageEntity
import com.mogujie.tt.db.entity.PeerEntity
import com.mogujie.tt.db.entity.UserEntity
import com.mogujie.tt.imservice.entity.*

/**
 * Created by wr
 * Date: 2019/1/14  11:18
 * mail: 1902065822@qq.com
 * describe:
 */
object IMMessageExt {
    fun parseFromDB(message: MessageEntity) = message.parseFromDB()
}

/**
 * 构造发送消息
 */
fun MessageEntity.buildSendMessage(fromUser: UserEntity, peerEntity: PeerEntity, info: String?, displayType: Int): MessageEntity {
    val nowTime = (System.currentTimeMillis() / 1000).toInt()
    this.fromId = fromUser.peerId
    this.toId = peerEntity.peerId
    this.updated = nowTime
    this.created = nowTime
    this.displayType = displayType
    val peerType = peerEntity.type
    this.msgType = if (peerType == DBConstant.SESSION_TYPE_GROUP) DBConstant.MSG_TYPE_GROUP_TEXT else DBConstant.MSG_TYPE_SINGLE_TEXT
    this.status = MessageConstant.MSG_SENDING
    this.info = info
    this.nickname = fromUser.mainName
    this.buildSessionKey(true)
    return this
}

fun MessageEntity.parseFromDB(): MessageEntity? {
    return when (this.displayType) {
        DBConstant.SHOW_MIX_TEXT -> MixMessage.parseFromDB(this)
        DBConstant.SHOW_AUDIO_TYPE -> AudioMessage.parseFromDB(this)
        DBConstant.SHOW_IMAGE_TYPE -> ImageMessage.parseFromDB(this)
        DBConstant.SHOW_ORIGIN_TEXT_TYPE -> TextMessage.parseFromDB(this)
        DBConstant.SHOW_VIDEO_TYPE -> VideoMessage.parseFromDB(this)
        DBConstant.SHOW_BRAND_TYPE -> BrandMessage.parseFromDB(this)
        else -> TextMessage.parseFromDB(this)
    }
}