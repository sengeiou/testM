package com.qingmeng.mengmeng.utils.loginshare.bean

/**
 * Created by zq on 2018/9/5
 */
data class WxTokenBean(var access_token: String, val expires_in: String, val refresh_token: String,
                       val openid: String, val scope: String, val unionid: String)

data class WxInfoBean(var openid: String, var headimgurl: String, var nickname: String, var sex: Int = 0, var province: String = "",
                       var city: String = "", var country: String = "", var unionid: String = "", var privilege: List<Any> = ArrayList())

data class WxBean(var code: String = "")

