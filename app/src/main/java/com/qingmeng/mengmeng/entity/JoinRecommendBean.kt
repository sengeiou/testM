package com.qingmeng.mengmeng.entity

import android.text.TextUtils
import com.google.gson.Gson
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.utils.SharedSingleton

class JoinRecommendBean(var data: ArrayList<JoinBean>) {
    fun upDate(type: Int) {
        SharedSingleton.instance.setString("${IConstants.JOIN_RECOMMEND}$type", Gson().toJson(this))
    }

    companion object {
        fun fromString(type: Int): JoinRecommendBean {
            val stringBean = SharedSingleton.instance.getString("${IConstants.JOIN_RECOMMEND}$type")
            return if (TextUtils.isEmpty(stringBean)) {
                JoinRecommendBean(ArrayList())
            } else {
                Gson().fromJson<JoinRecommendBean>(stringBean, JoinRecommendBean::class.java)
            }
        }
    }

    class JoinBean(var id: Int, var name: String, var logo: String, var appCover: String, var capitalName: String, var isFakeBrand: Int)
}