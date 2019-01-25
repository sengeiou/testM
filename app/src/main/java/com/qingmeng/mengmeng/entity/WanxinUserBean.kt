package com.qingmeng.mengmeng.entity

import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.utils.SharedSingleton
import java.io.Serializable

/**
 * 完信用户bean
 */
data class WanxinUserBean(var uId: Int = 0, var token: String = "") : Serializable {
    fun upDate() {
        SharedSingleton.instance.setString(IConstants.WANXIN_USER, toString())
    }

    override fun toString(): String = SharedSingleton.object2String(this)

    companion object {
        fun fromString(): WanxinUserBean {
            val any = SharedSingleton.string2Object(SharedSingleton.instance.getString(IConstants.WANXIN_USER))
            return if (any != null && any is WanxinUserBean) any else WanxinUserBean()
        }
    }

}