package com.qingmeng.mengmeng.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

class StaticDataBean(var systemStatic: ArrayList<StaticBean>, var version: String) {
    fun setVersion() {
        systemStatic.forEach { it.version = version }
    }
}

@Entity
class StaticBean(@Id var cacheId: Long,
                 var id: Int,
                 var title: String,
                 var icon: String,
                 var describe: String,
                 var type: Long,
                 var skipId: Int,
                 var skipType: Int,
                 var version: String)