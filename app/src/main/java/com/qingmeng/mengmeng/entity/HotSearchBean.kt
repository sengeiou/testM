package com.qingmeng.mengmeng.entity

import java.io.Serializable

/**
 * Created by mingyue
 * Date: 2019/1/16
 * mail: 153705849@qq.com
 * describe:
 */
data class HotSearchBean(var hotSearchesList: ArrayList<HotSearchesList>, var version: String = "") : Serializable {
    class HotSearchesList(var id: Int, var name: String)
}