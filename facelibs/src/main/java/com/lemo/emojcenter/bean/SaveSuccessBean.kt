package com.lemo.emojcenter.bean

import com.google.gson.annotations.SerializedName

/**
 * Description:
 * Author:wxw
 * Date:2018/2/7.
 */
class SaveSuccessBean {

    /**
     * sortBy : 0
     * id : 194
     * userId : 1
     */

    @SerializedName("sortBy")
    var sortBy: Int = 0
    @SerializedName("id")
    var id: Int = 0
    @SerializedName("userId")
    var userId: Int = 0
}
