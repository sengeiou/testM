package com.mogujie.tt.imservice.entity

import com.mogujie.tt.config.DBConstant
import com.mogujie.tt.config.MessageConstant
import com.mogujie.tt.db.entity.MessageEntity
import com.mogujie.tt.db.entity.PeerEntity
import com.mogujie.tt.db.entity.UserEntity
import com.mogujie.tt.imservice.manager.buildSendMessage

/**
 * Created by wr
 * Date: 2019/1/14  14:45
 * mail: 1902065822@qq.com
 * describe:
 */
class CmdMessage() : TextMessage() {

    constructor(entity: MessageEntity) : this() {
        parseMessage(entity)
    }

    override fun getSpecial(): Boolean = true

    companion object {
        /**
         * 接受到网络包，解析成本地的数据
         */
        fun parseFromNet(entity: MessageEntity): CmdMessage = CmdMessage(entity).apply {
            setStatus(MessageConstant.MSG_SUCCESS)
            setDisplayType(entity.infoType)
        }

        fun buildForSend(content: String?, fromUser: UserEntity?, sessionKey: String?): CmdMessage? {
            if (content == null || fromUser == null || sessionKey == null) {
                return null
            }
            val peerEntity = PeerEntity.getPeerEntity(sessionKey)
            return CmdMessage().apply { buildSendMessage(fromUser, peerEntity, content, DBConstant.SHOW_REVOKE_TYPE) }
        }
    }
}