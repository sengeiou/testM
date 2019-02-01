package com.qingmeng.mengmeng.entity

import android.text.TextUtils
import com.google.gson.Gson
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.utils.SharedSingleton

/**
 * Created by fyf on 2019/1/27
 * 搜索结果页面筛选栏Bean
 */
class SeachConBean(var data: ArrayList<ConditionBean>) {

    fun upDate(type: Int) {
        SharedSingleton.instance.setString("${IConstants.SEACH_RESULT}$type", Gson().toJson(this))
    }

    companion object {
        fun fromString(type: Int): SeachConBean {
            val stringBean = SharedSingleton.instance.getString("${IConstants.SEACH_RESULT}$type")
            return if (TextUtils.isEmpty(stringBean)) {
                SeachConBean(ArrayList())
            } else {
                Gson().fromJson<SeachConBean>(stringBean, SeachConBean::class.java)
            }
        }
    }

}