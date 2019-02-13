package com.qingmeng.mengmeng.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

data class SeachConditionTypeBean(
        //加盟模式
        var joinModes: List<ConditionTypeBean>)

@Entity
data class ConditionTypeBean(
        @Id
        var cacheId: Long = 0,
        val id: Int,
        val name: String,
        var checkState: Boolean = false
)