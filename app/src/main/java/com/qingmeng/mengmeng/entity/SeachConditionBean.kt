package com.qingmeng.mengmeng.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

data class SeachConditionBean(
        //加盟模式
        val joinModes: List<JoinModes>
)

@Entity
data class JoinModes(
        @Id
        var cacheId: Long = 0,
        var id: Int,
        var name: String,
        var checkState: Boolean = false
)