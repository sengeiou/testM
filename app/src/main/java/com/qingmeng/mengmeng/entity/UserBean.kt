package com.qingmeng.mengmeng.entity

import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.utils.SharedSingleton
import java.io.Serializable

data class UserBean(var userInfo: UserInfo = UserInfo(), var token: String = "") : Serializable {
    fun upDate() {
        SharedSingleton.instance.setString(IConstants.USER, toString())
    }

    override fun toString(): String = SharedSingleton.object2String(this)

    companion object {
        fun fromString(): UserBean {
            val any = SharedSingleton.string2Object(SharedSingleton.instance.getString(IConstants.USER))
            return if (any != null && any is UserBean) any else UserBean()
        }
    }

    class UserInfo(var id: Int = 0, var type: Int = 0, var lastLoginTime: Long = 0, var phone: String = "",
                         var avatar: String = "", var nickname: String = "", var userName: String = "", var qqToken: String = "",
                         var qqOpenid: String = "", var weChatToken: String = "", var weChatOpenid: String = "",
                         var weChatUnionid: String = "", var wxName: String = "" ,var token: String="") : Serializable
}