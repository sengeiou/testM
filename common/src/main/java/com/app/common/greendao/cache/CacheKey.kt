package com.app.common.greendao.cache

import android.text.TextUtils

/**
 * Created by wangru
 * Date: 2018/5/10  17:35
 * mail: 1902065822@qq.com
 * describe:
 */

object CacheKey {
    fun getKey(url: String, token: String): String {
        return getKey(url, null, token)
    }

    @JvmOverloads
    fun getKey(url: String, map: Map<String, String>? = null, token: String? = null): String {
        val stringBuilder = StringBuilder(url)
        if (!TextUtils.isEmpty(token)) {
            stringBuilder.append("#token=" + token!!)
        }
        if (map != null) {
            for (key in map.keys) {
                stringBuilder.append("#" + key + "=" + map[key])
            }
        }
        return stringBuilder.toString()
    }
}
