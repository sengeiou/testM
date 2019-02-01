package com.qingmeng.mengmeng.entity

data class SeachJoinAreaBean(
        val fatherDtos: List<FatherDto>,
        val version: String
) {
    fun setVersion() {
        fatherDtos.forEach {
            it.version = version
        }
    }
}

data class FatherDto(
        val cityFilter: List<CityFilter>,
        val fatherId: Int,
        val id: Int,
        val isMunicipality: Int,
        val level: Int,
        val name: String,
        var version: String
)

data class CityFilter(
        val fatherId: Int,
        val id: Int,
        val isMunicipality: Int,
        val level: Int,
        val name: String
)