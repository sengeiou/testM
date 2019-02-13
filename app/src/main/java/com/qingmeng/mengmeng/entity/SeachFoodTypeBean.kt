package com.qingmeng.mengmeng.entity

data class SeachFoodTypeBean(
        val foodType: List<FoodType>,
        val version: String
)

data class FoodType(
        var foodTypeDto: List<FoodTypeDto>,
        var fahterId: Int,
        var icon: String,
        var id: Int,
        var logo: String,
        var name: String,
        var version: String,
        var checkState: Boolean = false
)

data class FoodTypeDto(
        var fahterId: Int,
        var icon: String,
        var id: Int,
        var logo: String,
        var name: String

)