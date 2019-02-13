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

    class NewsBean(var newsId: Int,
                   var banner: String,
                   var tittle: String,
                   var createTime: String,
                   var content: String,
                   var formatTime: String,
                   var articleUrl: String
            //banner 数据
//                   var bannerid: Int,
//                   var icon: String,
//                   var describe: String,
//                   var imgUrl: String,
//                   var type: Int,
//                   var skipId: Int,
//                   var skipType: Int,
//                   var fatherSkipId: Int
    )
}