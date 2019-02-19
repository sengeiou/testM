package com.lemo.emojcenter.bean

import com.lemo.emojcenter.FaceConfigInfo
import com.lemo.emojcenter.constant.FaceEmojType

import java.io.File
import java.io.Serializable

class EmojBean : Serializable {
    var emojType: FaceEmojType? = null
    var id: Int = 0
    var pathLocal: String? = null
    var pathLocalThumb: String? = null
    var url: String? = null
    var urlThumb: String? = null
    //表情名 eg: 欢迎
    var name: String? = null
    //经典表情
    //res id eg: R.id.e_smile
    var iconRes: Int = 0
    //表情对应文字 eg：[笑脸]
    var emojiText: String? = null

    var width: Int = 0
    var height: Int = 0

    constructor() {

    }

    constructor(emojiText: String, iconRes: Int) {
        this.emojType = FaceEmojType.NORMAL
        this.emojiText = emojiText
        this.iconRes = iconRes
    }

    constructor(collectsBean: CollectsBean) {
        this.emojType = FaceEmojType.COLLECTION
        this.id = collectsBean.id
        this.url = collectsBean.master
        this.urlThumb = collectsBean.cover
        this.pathLocal = FaceConfigInfo.getCollectPathByUrl(collectsBean.master!!)
    }

    constructor(faceId: String, faceItem: EmojInfoBean.ItemBean) {
        this.emojType = FaceEmojType.EXPRESSION
        this.id = faceItem.id
        this.url = faceItem.master
        this.urlThumb = faceItem.thumb
        this.name = faceItem.keyword
        this.pathLocal = FaceConfigInfo.getDirEmoj(faceId) + File.separator + faceItem.name + ".gif"
        this.pathLocalThumb = FaceConfigInfo.getDirEmoj(faceId) + File.separator + faceItem.name + ".png"
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
