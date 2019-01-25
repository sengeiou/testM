package com.qingmeng.mengmeng.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.io.Serializable

/**
 * Created by mingyue
 * Date: 2019/1/16
 * mail: 153705849@qq.com
 * describe:
 */
data class HotSearchBean(var hotSearchesList: ArrayList<HotSearchesList>, var version: String   ) : Serializable {
    fun setVersion() {
        hotSearchesList.forEach { it.version = version }
    }
}
@Entity
class HotSearchesList(@Id var cacheId: Long, var id: Int, var name: String,var version:String)

@Entity
class SearchHistoryList(){
    @Id  var cacheId: Long =0
    var id: Long=1
    var name: String=""
}