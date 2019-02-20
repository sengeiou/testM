package com.lemo.emojcenter.bean


import com.lemo.emojcenter.constant.FaceEmojType
import com.lemo.emojcenter.constant.FaceLocalConstant
import java.util.*

/**
 * Created by zejian
 * Time  16/1/5 下午5:57
 * Email shinezejian@163.com
 * Description:底部tab图片对象
 */
class EmojGroupBean(faceId: String, path: String, url: String) {
    var id: Int = 0
    var flag: String? = null//说明文本
    var icon: String? = null//图标
    var url: String? = null//网络url
    var isSelected = false//是否被选中
    var faceId: String
    var emojType: FaceEmojType? = null
    var emojiconList: MutableList<EmojBean> = ArrayList()

    init {
        if (faceId == FaceLocalConstant.FACE_ID_EMOJ) {
            this.flag = "经典笑脸"
            this.icon = "笑脸"
            this.isSelected = true
            this.faceId = FaceLocalConstant.FACE_ID_EMOJ
            this.url = url
        } else if (faceId == FaceLocalConstant.FACE_ID_COLLECT) {
            this.flag = "收藏"
            this.icon = "collection"
            this.isSelected = false
            this.faceId = FaceLocalConstant.FACE_ID_COLLECT
            this.url = url
        } else {
            this.faceId = faceId
            this.icon = path
            this.flag = "其他笑脸" + faceId
            this.isSelected = false
            this.url = url
        }
    }

}
