package com.mogujie.tt.imservice.entity

import android.util.Log
import com.app.common.extensions.imgGetHeightExt
import com.app.common.extensions.imgGetWidthExt
import com.mogujie.tt.config.DBConstant
import com.mogujie.tt.config.MessageConstant
import com.mogujie.tt.config.MessageExtConst
import com.mogujie.tt.db.entity.MessageEntity
import com.mogujie.tt.db.entity.PeerEntity
import com.mogujie.tt.db.entity.UserEntity
import com.mogujie.tt.imservice.manager.buildSendMessage
import com.mogujie.tt.ui.adapter.album.ImageItem
import java.io.File
import java.io.Serializable
import java.util.*

/**
 * @author : yingmu on 14-12-31.
 * @email : yingmu@mogujie.com.
 */
class ImageMessage() : TextMessage(), Serializable {
    var path: String = ""
        get() = getAttributeString(MessageExtConst.IMG_PATH)
        set(value) {
            setAttribute(MessageExtConst.IMG_PATH, value)
            setAttribute(MessageExtConst.IMG_WIDTH, value.imgGetWidthExt())
            setAttribute(MessageExtConst.IMG_HEIGHT, value.imgGetHeightExt())
            field = value
        }
    var url: String = ""
        get() = getAttributeString(MessageExtConst.IMG_URL)
        set(value) {
            setAttribute(MessageExtConst.IMG_URL, value)
            field = value
        }

    var width: Int = 0
        get() = getAttributeInt(MessageExtConst.IMG_WIDTH)

    var height: Int = 0
        get() = getAttributeInt(MessageExtConst.IMG_HEIGHT)

    var loadStatus: Int = 0
        get() = getAttributeInt(MessageExtConst.LOADSTATUS)
        set(value) {
            setAttribute(MessageExtConst.LOADSTATUS, value)
            field = value
        }

    override fun getInfo(): String = DBConstant.DISPLAY_FOR_IMAGE

    constructor(entity: MessageEntity) : this() {
        parseMessage(entity)
    }

    companion object {
        private val TAG = "ImageMessage"
        //存储图片消息
        private val imageMessageMap = java.util.HashMap<Long, ImageMessage>()
        private var imageList: ArrayList<ImageMessage> = ArrayList()
        @Synchronized
        fun addToImageMessageList(msg: ImageMessage?) {
            try {
                if (msg?.getId() != null) {
                    imageMessageMap[msg.getId()] = msg
                }
            } catch (e: Exception) {
            }
        }

        val imageMessageList: ArrayList<ImageMessage>
            get() {
                imageList = ArrayList()
                val it = imageMessageMap.keys.iterator()
                while (it.hasNext()) {
                    imageMessageMap[it.next()]?.let { imageList.add(it) }
                }
                imageList.sortWith(Comparator { image1, image2 ->
                    val a = image1.getUpdated()
                    val b = image2.getUpdated()
                    if (a == b) {
                        image2.getId()!!.compareTo(image1.getId())
                    } else b.compareTo(a)
                })
                return imageList
            }

        @Synchronized
        fun clearImageMessageList() {
            imageMessageMap.clear()
            imageMessageMap.clear()
        }

        // 消息页面，发送图片消息
        fun buildForSend(item: ImageItem, fromUser: UserEntity?, peerEntity: PeerEntity?): ImageMessage? {
            if (fromUser == null) {
                Log.e(TAG, "buildForSend: fromUser == null")
                return null
            }
            if (peerEntity == null) {
                Log.e(TAG, "buildForSend: peerEntity == null")
                return null
            }
            return ImageMessage().apply {
                buildSendMessage(fromUser, peerEntity, DBConstant.DISPLAY_FOR_IMAGE, DBConstant.SHOW_IMAGE_TYPE)
                loadStatus = MessageConstant.IMAGE_UNLOAD
                if (File(item.imagePath).exists()) {
                    path = item.imagePath
                } else {
                    if (File(item.thumbnailPath).exists()) {
                        path = item.thumbnailPath
                    } else {
                        // 找不到图片路径时使用加载失败的图片展示
                        path = ""
                    }
                }
            }
        }

        fun buildForSend(takePhotoSavePath: String, fromUser: UserEntity, peerEntity: PeerEntity) =
                ImageMessage().apply {
                    buildSendMessage(fromUser, peerEntity, content, DBConstant.SHOW_IMAGE_TYPE)
                    path = takePhotoSavePath
                    loadStatus = MessageConstant.IMAGE_UNLOAD
                }

        //接受到网络包，解析成本地的数据
        fun parseFromNet(entity: MessageEntity): ImageMessage {
            val imageMessage = ImageMessage(entity)
            imageMessage.loadStatus = MessageConstant.IMAGE_UNLOAD
            imageMessage.setStatus(MessageConstant.MSG_SUCCESS)
            return imageMessage
        }

        fun parseFromDB(entity: MessageEntity): ImageMessage {
            if (entity.displayType != DBConstant.SHOW_IMAGE_TYPE) {
                throw RuntimeException("#ImageMessage# parseFromDB,not SHOW_IMAGE_TYPE")
            }
            val imageMessage = ImageMessage(entity)
            imageMessage.setDisplayType(DBConstant.SHOW_IMAGE_TYPE)
            imageMessage.loadStatus = MessageConstant.IMAGE_LOADED_SUCCESS
            imageMessage.setStatus(MessageConstant.MSG_SUCCESS)
            return imageMessage
        }
    }
}
