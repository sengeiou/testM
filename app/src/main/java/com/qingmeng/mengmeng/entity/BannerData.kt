package com.qingmeng.mengmeng.entity



/**
 * Created by mingyue
 * Date: 2019/1/12
 * mail: 153705849@qq.com
 * describe:
 */
data class BannerData(
        val banners: List<Banner>,
        val version: String
)
data class Banner(
        val id: Int,
        val imgUrl: String,
        val interiorDetailsId: Int,
        val isDel: Boolean,
        val skipType: Int,
        val sort: Int,
        val title: String,
        val type: Int,
        val url: String
)