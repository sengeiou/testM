package com.qingmeng.mengmeng.entity

/**
 * Created by fyf on 2019/1/21
 * 红铺1级菜单列表Bean
 */
data class RedShopLeftListBean(
//        val typeList: RedShopLeftBean,
        val popularBrands: PopularBrands,
        val type: Type,
        val version: String
)

//右边
data class PopularBrands(
        val hotBrands: List<RedShopLeftBean>
)

data class Type(
        val typeList: List<RedShopLeftBean>
)

data class RedShopLeftBean(
        val id: Int,
        val name: String,
        val fahterId: Int,
        val logo: String,
        var checkState: Boolean
)















