package com.qingmeng.mengmeng.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

data class SeachConditionBean(
        //投资金额
        var capitalList: List<ConditionBean>,
        //加盟模式
        var joinModes: List<ConditionBean>,
        var version: String)

// {
//    fun setVersion() {
//        capitalList.forEach {
//            it.version = version
//            it.type = 1
//        }
//        joinModes.forEach {
//            it.version = version
//            it.type = 2
//        }
//    }
//}

@Entity
data class ConditionBean(
        @Id
        var cacheId: Long = 0,
        val id: Int,
        val name: String,
//        var version: String,
        //1 为加盟金额 2 为加盟模式
        var type: Long
)