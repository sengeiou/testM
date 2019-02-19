package com.lemo.emojcenter.bean

import com.google.gson.annotations.SerializedName

/**
 * Description:
 * Author:wxw
 * Date:2018/2/9.
 */
class UpLoadBean {


    /**
     * appId : 1
     * sortBy : 0
     * id : 291
     * userId : 1
     */

    @SerializedName("appId")
    var appId: Int = 0
    @SerializedName("sortBy")
    var sortBy: Int = 0
    @SerializedName("id")
    var id: Int = 0
    @SerializedName("userId")
    var userId: Int = 0
}
