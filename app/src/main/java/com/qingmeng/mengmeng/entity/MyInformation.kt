package com.qingmeng.mengmeng.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

/**
 *  Description :我的板块用户信息bean

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/14
 */

@Entity
class MyInformation {
    @Id
    var cacheId: Long = 0
    val id: Int = 0
    val avatar: String = ""
    val state: Int = 0
    val phone: String = ""
    val userName: String = ""
    val userType: Int = 0
    val myAttention: Int = 0
    val myFootprint: Int = 0
    val myComment: Int = 0
}