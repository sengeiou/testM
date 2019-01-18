package com.qingmeng.mengmeng.utils

import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.entity.*

object BoxUtils {
    private val boxStore = MainApplication.boxStore
    private val bannerBox = boxStore.boxFor(Banner::class.java)
    private val staticBox = boxStore.boxFor(StaticBean::class.java)
    private val myInformationBox = boxStore.boxFor(MyInformation::class.java)   //我的板块信息

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

    //保存我的板块信息
    fun saveMyInformation(myInformation: MyInformation) {
        boxStore.runInTxAsync({ myInformationBox.put(myInformation) }, { _, _ -> })
    }

    //删除已过期的我的板块信息
    fun removeMyInformation(myInformation: MyInformation) {
        boxStore.runInTxAsync({ myInformationBox.remove(myInformation) }, { _, _ -> })
    }

    //获取我的版块信息
    fun getMyInformation(): MyInformation? {
        return myInformationBox.query().build().findFirst()
    }
}