package com.qingmeng.mengmeng.entity

import com.qingmeng.mengmeng.utils.DESUtil

class OssDataBean(var endpoint: String,
                  var buckName: String,
                  var folder: String,
                  var oss: OssBean,
                  var domain: String)

class OssBean {
    var securityToken: String = ""
        get() = DESUtil.decryptECB(field, key)
    var accessKeyId: String= ""
        get() = DESUtil.decryptECB(field, key)
    var accessKeySecret: String= ""
        get() = DESUtil.decryptECB(field, key)
    var expiration: String= ""
        get() = DESUtil.decryptECB(field, key)
    private val key = "llIU0x02"

}