package com.qingmeng.mengmeng.entity

data class SeachFoodTypeBean(
        val foodType: List<FoodType>,
        val version: String
)

data class FoodType(
        val foodTypeDto: List<FoodTypeDto>,
        val fahterId: Int,
        val icon: String,
        val id: Int,
        val logo: String,
        val name: String
)
data class FoodTypeDto(
        val fahterId: Int,
        val icon: String,
        val id: Int,
        val logo: String,
        val name: String
)