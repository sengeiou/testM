package com.app.common.greendao.ope

import android.text.TextUtils
import com.app.common.greendao.DbManager
import com.app.common.greendao.bean.CacheInfoBean
import com.app.common.greendao.gen.CacheInfoBeanDao


/**
 * 缓存操作
 * Created by wangru
 * Date: 2018/1/11  16:03
 * mail: 1902065822@qq.com
 * describe:
 */

object CacheInfoDaoOpe {

    private val cacheInfoDao: CacheInfoBeanDao
        get() = DbManager.instance.daoSession.cacheInfoBeanDao


    fun getVersionCodeByKey(key: String): String? {
        val cacheInfo = getCacheInfoByKey(key)
        return cacheInfo?.version
    }

    fun insertOrUpdateVersion(cacheInfoBean: CacheInfoBean?) {
        if (cacheInfoBean != null && !TextUtils.isEmpty(cacheInfoBean.key)) {
            insertOrUpdateVersionByKey(cacheInfoBean.key, cacheInfoBean)
        }
    }

    fun insertOrUpdateVersionByKey(key: String?, cacheInfoBean: CacheInfoBean?) {
        if (key == null || cacheInfoBean == null) {
            return
        }
        cacheInfoBean.key = key
        val cacheInfo = getCacheInfoByKey(key)
        if (cacheInfo != null) {
            cacheInfoBean._id = cacheInfo._id
        }
        cacheInfoDao.save(cacheInfoBean)
    }

    fun getCacheInfoByKey(key: String): CacheInfoBean? {
        return cacheInfoDao.queryBuilder().where(CacheInfoBeanDao.Properties.Key.eq(key)).limit(1).unique()
    }

    fun getCacheInfoDataByKey(key: String?): String? {
        if (key == null) return null
        val cacheInfo = getCacheInfoByKey(key)
        return cacheInfo?.data
    }

    fun deleteCacheInfoByKey(key: String?) {
        if (key == null) return
        cacheInfoDao.queryBuilder().where(CacheInfoBeanDao.Properties.Key.eq(key)).unique()?.let {
            cacheInfoDao.deleteInTx(it)
        }
    }

}
