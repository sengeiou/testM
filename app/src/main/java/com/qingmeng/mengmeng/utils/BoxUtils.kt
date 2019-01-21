package com.qingmeng.mengmeng.utils

import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.entity.*

object BoxUtils {
    private val boxStore = MainApplication.boxStore
    private val bannerBox = boxStore.boxFor(Banner::class.java)
    private val staticBox = boxStore.boxFor(StaticBean::class.java)
    private val hotsearchBox = boxStore.boxFor(HotSearchesList::class.java)
    private val searchBox = boxStore.boxFor(SearchHistoryList::class.java)
    //保存banner到数据库
    fun saveBanners(banners: MutableList<Banner>) {
        boxStore.runInTxAsync({ bannerBox.put(banners) }, { _, _ -> })
    }

    /**
     * 根据类型获取banner信息
     *
     * @param type 1、首页；3、头报 5.登录banner 6.引导页
     */
    fun getBannersByType(type: Long): MutableList<Banner> {
        return bannerBox.query().equal(Banner_.type, type).build().find()
    }

    //删除已过期banner
    fun removeBanners(banners: MutableList<Banner>) {
        boxStore.runInTxAsync({ bannerBox.remove(banners) }, { _, _ -> })
    }

    //保存静态数据到数据库
    fun saveStatic(statics: MutableList<StaticBean>) {
        boxStore.runInTxAsync({ staticBox.put(statics) }, { _, _ -> })
    }
    //保存热门数据到数据库
    fun saveHotSearch(statics: MutableList<HotSearchesList>) {
        boxStore.runInTxAsync({ hotsearchBox.put(statics) }, { _, _ -> })
    }
    //根据版本号获取热门数据
    fun getHotSearch(version :String):MutableList<HotSearchesList>{
        return hotsearchBox.query().equal(HotSearchesList_.version,version).build().find()
    }
    //根据cacheId获取热门数据
    fun getIdHotSearch(cacheId :Long):MutableList<HotSearchesList>{
        return hotsearchBox.query().equal(HotSearchesList_.cacheId,cacheId).build().find()
    }
    //删除已过期热门数据
    fun removeHotSearch(search: MutableList<HotSearchesList>) {
        boxStore.runInTxAsync({ hotsearchBox.remove(search) }, { _, _ -> })
    }
    //保存搜索记录到数据库
    fun saveSearch(search: SearchHistoryList) {
        boxStore.runInTxAsync({ searchBox.put(search) }, { _, _ -> })
    }

    //根据name获取搜索记录
    fun getnameSearch(name :String):MutableList<SearchHistoryList>{
        return searchBox.query().equal(SearchHistoryList_.name,name).build().find()
    }
    //根据Id获取搜索记录
    fun getIdSearch(id :Long):MutableList<SearchHistoryList>{
        return searchBox.query().equal(SearchHistoryList_.id,id).build().find()
    }
    //删除搜索记录
    fun removeSearchs(search: MutableList<SearchHistoryList>) {
        boxStore.runInTxAsync({ searchBox.remove(search) }, { _, _ -> })
    }


    /**
     * 根据类型获取静态数据
     *
     * @param type 1.首页banner8个icon 2.首页列表模块 3.列表筛选标题 4.综合排序 5.反馈类型
     */
    fun getStaticByType(type: Long): MutableList<StaticBean> {
        return staticBox.query().equal(StaticBean_.type, type).build().find()
    }


    //删除已过期静态数据
    fun removeStatic(statics: MutableList<StaticBean>) {
        boxStore.runInTxAsync({ staticBox.remove(statics) }, { _, _ -> })
    }


}