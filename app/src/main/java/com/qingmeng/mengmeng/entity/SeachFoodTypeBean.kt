package com.qingmeng.mengmeng.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

data class SeachFoodTypeBean(
        val foodType: List<FoodType>,
        val version: String
) {
    fun setVersion() {
        foodType.forEach { it.version = version

        }

    }
}


data class FoodType(

        var foodTypeDto: List<FoodTypeDto>,
        var fahterId: Int,
        var icon: String,
        var id: Int,
        var logo: String,
        var name: String,
        var version: String
)


data class FoodTypeDto(
        var fahterId: Int,
        var icon: String,
        var id: Int,
        var logo: String,
        var name: String,
        var version: String
)