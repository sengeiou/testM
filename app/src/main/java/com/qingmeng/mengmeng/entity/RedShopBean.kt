package com.qingmeng.mengmeng.entity

import android.text.TextUtils
import com.google.gson.Gson
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.utils.SharedSingleton

/**
 * Created by fyf on 2019/1/26
 * 红铺Bean
 */
class RedShopBean(var data: ArrayList<RedShopLeftBean>) {

    fun upDate(type: Int) {
        SharedSingleton.instance.setString("${IConstants.RED_SHOP}$type", Gson().toJson(this))
    }

    companion object {
        fun fromString(type: Int): RedShopBean {
            val stringBean = SharedSingleton.instance.getString("${IConstants.RED_SHOP}$type")
            return if (TextUtils.isEmpty(stringBean)) {
                RedShopBean(ArrayList())
            } else {
                Gson().fromJson<RedShopBean>(stringBean, RedShopBean::class.java)
            }
        }
    }

}