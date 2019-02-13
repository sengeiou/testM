package com.qingmeng.mengmeng.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
class CacheBean(var key: String, var value: String, @Id var id: Long = 0)