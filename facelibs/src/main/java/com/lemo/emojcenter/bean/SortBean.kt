package com.lemo.emojcenter.bean

import com.google.gson.annotations.SerializedName

/**
 * Description :
 *
 *
 * Author:fangxu
 *
 *
 * Email:634804858@qq.com
 *
 *
 * Date: 2018/2/1
 */

class SortBean {

    /**
     * id : 1
     * sortBy : 1
     */

    @SerializedName("id")
    var id: Int = 0
    @SerializedName("userId")
    var userId:String?=null
    @SerializedName("sortBy")
    var sortBy: Int = 0
}
