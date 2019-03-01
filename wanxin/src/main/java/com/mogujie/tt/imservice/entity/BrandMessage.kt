package com.mogujie.tt.imservice.entity

import com.mogujie.tt.config.DBConstant
import com.mogujie.tt.config.MessageConstant
import com.mogujie.tt.config.MessageExtConst
import com.mogujie.tt.db.entity.MessageEntity
import com.mogujie.tt.db.entity.PeerEntity
import com.mogujie.tt.db.entity.UserEntity
import com.mogujie.tt.imservice.manager.buildSendMessage
import java.io.Serializable

class BrandMessage() : TextMessage(), Serializable {
    //品牌id
    var brandId = 0
        get() = getAttributeInt(MessageExtConst.BRAND_ID)
        set(value) {
            setAttribute(MessageExtConst.BRAND_ID, value)
            field = value
        }
    //logo
    var brandLogo = ""
        get() = getAttributeString(MessageExtConst.BRAND_LOGO)
        set(value) {
            setAttribute(MessageExtConst.BRAND_LOGO, value)
            field = value
        }
    //品牌名称
    var brandName = ""
        get() = getAttributeString(MessageExtConst.BRAND_NAME)
        set(value) {
            setAttribute(MessageExtConst.BRAND_NAME, value)
            field = value
        }
    //品牌金额
    var brandValue = ""
        get() = getAttributeString(MessageExtConst.BRAND_AMOUNT)
        set(value) {
            setAttribute(MessageExtConst.BRAND_AMOUNT, value)
            field = value
        }

    constructor(entity: MessageEntity) : this() {
        parseMessage(entity)
    }

    override fun getInfo(): String = "[品牌详情]"

    companion object {
        fun parseFromNet(entity: MessageEntity): BrandMessage {
            val brandMessage = BrandMessage(entity)
            brandMessage.setStatus(MessageConstant.MSG_SUCCESS)
            return brandMessage
        }

        fun parseFromDB(entity: MessageEntity): BrandMessage {
            if (entity.displayType != DBConstant.SHOW_BRAND_TYPE) {
                throw RuntimeException("#brandMessage# parseFromDB,not SHOW_BRAND_TYPE")
            }
            return BrandMessage(entity)
        }

        // 消息页面，发送品牌详情消息
        fun buildForSend(brandId: Int?, brandLogo: String?, brandName: String?, brandValue: String?, fromUser: UserEntity?, sessionKey: String?): BrandMessage? {
            if (brandId == null || brandLogo == null || brandName == null || brandValue == null || fromUser == null || sessionKey == null) {
                return null
            }
            val peerEntity = PeerEntity.getPeerEntity(sessionKey)
            return BrandMessage().apply {
                buildSendMessage(fromUser, peerEntity, "[品牌详情]", DBConstant.SHOW_BRAND_TYPE)
                this.brandId = brandId
                this.brandLogo = brandLogo
                this.brandName = brandName
                this.brandValue = brandValue
            }
        }
    }
}