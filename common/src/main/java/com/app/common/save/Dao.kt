package com.app.common.save

import com.app.common.greendao.cache.CacheDataRequest
import com.app.common.json.GsonConvert
import java.lang.reflect.Type
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @see T
 */
class Dao<T>(var type: Type, var key: String, var default: T? = null) : ReadWriteProperty<Any?, T?> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T? = GsonConvert.jsonToObj(CacheDataRequest.getCacheSynch(key), type)
            ?: default

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        if (value == null) {
            CacheDataRequest.delectCacheByKey(key)
        } else {
            CacheDataRequest.saveCacheBeanSynch(key, value)
        }
    }
}