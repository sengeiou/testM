package com.qingmeng.mengmeng.entity
import com.google.gson.annotations.SerializedName


/**
 * Created by wr
 * Date: 2019/7/8  16:13
 * mail: 1902065822@qq.com
 * describe:
 */
data class WordBean(
    @SerializedName("code")
    val code: Int,
    @SerializedName("filterBefore")
    val filterBefore: String,
    @SerializedName("keywords")
    val keywords: String,
    @SerializedName("text")
    val text: String
)