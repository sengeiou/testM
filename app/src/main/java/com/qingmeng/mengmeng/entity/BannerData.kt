package com.qingmeng.mengmeng.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id


/**
 * Created by mingyue
 * Date: 2019/1/12
 * mail: 153705849@qq.com
 * describe:
 */
data class BannerData(var banners: List<Banner>, var version: String) {
    fun setVersion() {
        banners.forEach { it.version = version }
    }
}

@Entity
data class Banner(
        @Id var cacheId: Long,
        var id: Int,
        var imgUrl: String,
        var interiorDetailsId: Int,
        var isDel: Boolean,
        var skipType: Int,
        var sort: Int,
        var title: String,
        var type: Long,
        var url: String,
        var version: String
)