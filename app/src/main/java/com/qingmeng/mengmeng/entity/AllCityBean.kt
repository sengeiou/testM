package com.qingmeng.mengmeng.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

/**
 *  Description :所有城市bean

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/15
 */

@Entity
class AllCityBean {
    @Id
    var cacheId: Long = 0
    var version: String = ""
    var city: List<AllCity> = ArrayList()
    var cityString: String = ""
}

@Entity
data class AllCity(
        @Id
        var cacheId: Long,
        val id: String,
        val name: String,
        val fatherId: Int,
        val level: Int
)