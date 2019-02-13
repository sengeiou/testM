package com.qingmeng.mengmeng.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

data class SeachConditionBean(
        //加盟模式
        var joinModes: List<ConditionBean>
        )

@Entity
data class ConditionBean(
        @Id
        var cacheId: Long = 0,
        val id: Int,
        val name: String,
        var checkState: Boolean = false
)