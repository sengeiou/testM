package com.lemo.emojcenter.bean

import com.google.gson.annotations.SerializedName

/**
 * Created by wangru
 * Date: 2018/3/28  19:50
 * mail: 1902065822@qq.com
 * describe:
 */

class CollectsBean {
    /**
     * id : 406
     * userId : 10
     * appId : 1
     * master : http://test.rrzuzu.com/face/app/2018-02-11/201802111734511.jpg
     * cover : http://test.rrzuzu.com/face/app/2018-02-11/201802111734511.jpg
     * sortBy : 1
     * createTime : 1518341693000
     * updateTime : 1518341793000
     */

    @SerializedName("id")
    var id: Int = 0
    @SerializedName("userId")
    var userId: Int = 0
    @SerializedName("appId")
    var appId: Int = 0
    @SerializedName("master")
    var master: String? = null
    @SerializedName("cover")
    var cover: String? = null
    @SerializedName("sortBy")
    var sortBy: Int = 0
    @SerializedName("createTime")
    var createTime: Long = 0
    @SerializedName("updateTime")
    var updateTime: Long = 0
    @SerializedName("imageWidth")
    var imageWidth: Int = 0
    @SerializedName("imageHeight")
    var imageHeight: Int = 0

    constructor() {

    }

    constructor(myAddEmojBean: MyAddEmojBean) {
        appId = myAddEmojBean.appId
        cover = myAddEmojBean.cover
        id = myAddEmojBean.id
        master = myAddEmojBean.master
        sortBy = myAddEmojBean.sortBy
    }
}
