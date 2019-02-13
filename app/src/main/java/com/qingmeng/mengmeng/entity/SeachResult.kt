package com.qingmeng.mengmeng.entity

data class SeachResult(
        val data: List<SearchDto>
)

data class SearchDto(
        val capitalName: String,
        val directStoreNum: Int,
        val id: Int,
        val joinStoreNum: Int,
        val logo: String,
        val name: String,
        val status: Int,
        val affiliateSupport: List<String>
)


