package com.qingmeng.mengmeng.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.entity.*


object BoxUtils {
    private val boxStore = MainApplication.boxStore
    private val bannerBox = boxStore.boxFor(Banner::class.java)
    private val staticBox = boxStore.boxFor(StaticBean::class.java)
    private val hotsearchBox = boxStore.boxFor(HotSearchesList::class.java)
    private val searchBox = boxStore.boxFor(SearchHistoryList::class.java)
    private val mCacheBox = boxStore.boxFor(CacheBean::class.java)
    private val myInformationBox = boxStore.boxFor(MyInformation::class.java)   //我的板块信息
    private val allCityBox = boxStore.boxFor(AllCityBean::class.java)           //所有城市
    private val redShopBox = boxStore.boxFor(RedShopLeftBean::class.java)       //红铺数据
    private val newsPagerBox = boxStore.boxFor(NewsPagerList::class.java)       //头报数据
    private val joinTypeBox = boxStore.boxFor(JoinModes::class.java)        //加盟模式
    private val joinMoneyBox = boxStore.boxFor(CapitalList::class.java)        //投资金额

    //保存banner到数据库
    fun saveBanners(banners: MutableList<Banner>) {
        boxStore.runInTxAsync({ bannerBox.put(banners) }, { _, _ -> })
    }

    /**
     * 根据类型获取banner信息
     *
     * @param type 7、首页banner；8、头报banner；9、广告页 10、登录
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
    fun getHotSearch(version: String): MutableList<HotSearchesList> {
        return hotsearchBox.query().equal(HotSearchesList_.version, version).build().find()
    }

    //根据cacheId获取热门数据
    fun getIdHotSearch(cacheId: Long): MutableList<HotSearchesList> {
        return hotsearchBox.query().equal(HotSearchesList_.cacheId, cacheId).build().find()
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
    fun getnameSearch(name: String): MutableList<SearchHistoryList> {
        return searchBox.query().equal(SearchHistoryList_.name, name).build().find()
    }

    //根据Id获取搜索记录
    fun getIdSearch(id: Long): MutableList<SearchHistoryList> {
        return searchBox.query().equal(SearchHistoryList_.id, id).build().find()
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

    //保存所有城市信息
    fun saveAllCity(allCityBean: AllCityBean) {
        //把数组转成字符串存入 数组存不进去
        val cityListToJson = Gson().toJson(allCityBean.city)
        allCityBean.cityString = cityListToJson
        boxStore.runInTxAsync({ allCityBox.put(allCityBean) }, { _, _ -> })
    }

    //删除已过期的所有城市
    fun removeAllCity(allCityBean: AllCityBean) {
        boxStore.runInTxAsync({ allCityBox.remove(allCityBean) }, { _, _ -> })
    }

    //获取所有城市
    fun getAllCity(): AllCityBean? {
        val allCityBean = allCityBox.query().build().findFirst()
        //取得时候再解析成数组
        val list = Gson().fromJson<List<AllCity>>(allCityBean?.cityString, object : TypeToken<List<AllCity>>() {}.type)
        allCityBean?.city = list
        return allCityBean
    }

    fun <T> saveCache(bean: T, key: String) {
        boxStore.runInTxAsync({
            val value = Gson().toJson(bean)
            val cacheBean = CacheBean(key, value)
            mCacheBox.put(cacheBean)
        }, { _, _ -> })
    }

    inline fun <reified T> getCache(key: String): T {
        val boxStore = MainApplication.boxStore
        val cacheBox = boxStore.boxFor(CacheBean::class.java)
        val cacheBean = cacheBox.query().equal(CacheBean_.key, key).build().findFirst()
        return Gson().fromJson(cacheBean!!.value, T::class.java)
    }

    fun removeCache(key: String) {
        boxStore.runInTxAsync({
            val cacheBean = mCacheBox.query().equal(CacheBean_.key, key).build().findFirst()
            cacheBean?.let { mCacheBox.remove(it) }
        }, { _, _ -> })
    }

    //保存所有红铺数据
    fun saveAllRedShop(redShopList: MutableList<RedShopLeftBean>) {
        boxStore.runInTxAsync({ redShopBox.put(redShopList) }, { _, _ -> })
    }

    //删除所有红铺数据
    fun removeAllRedShop(redShopList: MutableList<RedShopLeftBean>) {
        boxStore.runInTxAsync({ redShopBox.remove(redShopList) }, { _, _ -> })
    }

    //   获取所有红铺数据     type:0 1级列表   1 2 3  根据1级列表id 找2 级列表
    fun getAllRedShop(fahterId: Long, type: Long): MutableList<RedShopLeftBean> {
        return redShopBox.query().equal(RedShopLeftBean_.fahterId, fahterId)
                .equal(RedShopLeftBean_.type, type).build().find()
    }

    fun saveNewsPager(newsPagerList: MutableList<NewsPagerList>) {
        boxStore.runInTxAsync({ newsPagerBox.put(newsPagerList) }, { _, _ -> })
    }

    //删除所有头报列表数据
    fun removeNewsPager(newsPagerList: MutableList<NewsPagerList>) {
        boxStore.runInTxAsync({ newsPagerBox.remove(newsPagerList) }, { _, _ -> })
    }

    //   获取所有头报列表数据
    fun getNewsPager(): MutableList<NewsPagerList> {
        return newsPagerBox.query().build().find()
    }

    //保存所有加盟数据
    fun saveJoinType(joinTypeList: MutableList<JoinModes>) {
        boxStore.runInTxAsync({ joinTypeBox.put(joinTypeList) }, { _, _ -> })
    }

    //删除所有加盟数据
    fun removeJoinType(joinTypeList: MutableList<JoinModes>) {
        boxStore.runInTxAsync({ joinTypeBox.remove(joinTypeList) }, { _, _ -> })
    }

    //   获取所有加盟方式数据
    fun getJoinType(): MutableList<JoinModes> {
        return joinTypeBox.query().build().find()
    }

    //保存所有投资金额
    fun saveMoneyType(joinTypeList: MutableList<CapitalList>) {
        boxStore.runInTxAsync({ joinMoneyBox.put(joinTypeList) }, { _, _ -> })
    }

    //删除所有投资金额
    fun removeMoneyType(joinTypeList: MutableList<CapitalList>) {
        boxStore.runInTxAsync({ joinMoneyBox.remove(joinTypeList) }, { _, _ -> })
    }

    //   获取所有投资金额数据
    fun getMoneyType(): MutableList<CapitalList> {
        return joinMoneyBox.query().build().find()
    }
    //保存所有搜索页面城市列表数据
//    fun saveSeachCity(seachCityList: MutableList<FatherDto>) {
//        boxStore.runInTxAsync({ seachCityBox.put(seachCityList) }, { _, _ -> })
//    }
//
//    //删除所有搜索页面城市列表数据
//    fun removeSeachCity(seachCityList: MutableList<FatherDto>) {
//        boxStore.runInTxAsync({ seachCityBox.remove(seachCityList) }, { _, _ -> })
//    }
//
//    //   获取所有搜索页面城市列表数据
//    fun getSeachCity(fahterId: Long): MutableList<FatherDto> {
//        return seachCityBox.query().equal(FatherDto_.fatherId, fahterId).build().find()
//    }

    // 保存所有搜索页面餐饮类型列表数据
//    fun saveSeachFoodType(seachFoodTypeList: MutableList<FoodType>) {
//        boxStore.runInTxAsync({ seachFoodTypeBox.put(seachFoodTypeList) }, { _, _ -> })
//    }
//
//    //删除所有搜索页面餐饮类型列表数据
//    fun removeSeachFoodType(seachFoodTypeList: MutableList<FoodType>) {
//        boxStore.runInTxAsync({ seachFoodTypeBox.remove(seachFoodTypeList) }, { _, _ -> })
//    }
//
//    //   获取所有搜索页面餐饮类型列表数据
//    fun getSeachFoodType(fahterId: Long): MutableList<FoodType> {
//        return seachFoodTypeBox.query().equal(FoodType_.fahterId, fahterId).build().find()
//    }
}