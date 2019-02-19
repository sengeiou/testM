package com.lemo.emojcenter.bean

import com.google.gson.annotations.SerializedName

/**
 * 所有表情包
 * Created by wangru
 * Date: 2018/3/28  17:01
 * mail: 1902065822@qq.com
 * describe:
 */

class AllEmojBean {

    @SerializedName("version")
    var version: Int = 0
    @SerializedName("faceDetailGetdetailsDtos")
    var emojList: List<EmojInfoBean>? = null

}
