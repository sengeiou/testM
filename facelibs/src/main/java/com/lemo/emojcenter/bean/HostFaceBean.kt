package com.lemo.emojcenter.bean

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Description:
 * Author:wxw
 * Date:2018/2/6.
 */
class HostFaceBean {

    @SerializedName("keys")
    var keys: List<EmojInfoBean>? = null
    @SerializedName("collects")
    var collects: List<CollectsBean>? = null

    init {
        keys = ArrayList()
        collects = ArrayList()
    }

}
