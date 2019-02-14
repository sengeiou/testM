package com.qingmeng.mengmeng.entity

class SeachJoinAreaBean(
        var fatherDtos: List<FatherDto>,
        val version: String
) {
    fun setVersion() {
        fatherDtos.forEach {
            it.version = version
        }
    }
}

data class FatherDto(
        var cityFilter: List<CityFilter>,
        var fatherId: Int,
        var id: Int,
        var isMunicipality: Int,
        var level: Int,
        var name: String,
        var version: String,
        var checkState: Boolean = false
)

data class CityFilter(
        var fatherId: Int,
        var id: Int,
        var isMunicipality: Int,
        var level: Int,
        var name: String
)