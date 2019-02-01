package com.qingmeng.mengmeng.entity

import android.text.TextUtils
import com.google.gson.Gson
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.utils.SharedSingleton

class NewsPagerBean(var data: ArrayList<NewsPagerList>) {
    fun upDate(type: Int) {
        SharedSingleton.instance.setString("${IConstants.NEWS_PAGER}$type", Gson().toJson(this))
    }

    companion object {
        fun fromString(type: Int): NewsPagerBean {
            val stringBean = SharedSingleton.instance.getString("${IConstants.NEWS_PAGER}$type")
            return if (TextUtils.isEmpty(stringBean)) {
                NewsPagerBean(ArrayList())
            } else {
                Gson().fromJson<NewsPagerBean>(stringBean, NewsPagerBean::class.java)
            }
        }
    }

    // class JoinBean(var id: Int, var name: String, var logo: String, var capitalName: String)
}