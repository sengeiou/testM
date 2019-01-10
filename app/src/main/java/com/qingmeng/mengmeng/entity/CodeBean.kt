package com.qingmeng.mengmeng.entity

import com.google.gson.Gson
import org.json.JSONObject

class CodeBean(var success: Int, var challenge: String, var gt: String, var new_captcha: Boolean) {
    fun toJson(): JSONObject {
        return JSONObject(Gson().toJson(this))
    }
}