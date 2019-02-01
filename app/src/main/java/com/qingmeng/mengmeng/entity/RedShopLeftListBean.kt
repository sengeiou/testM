package com.qingmeng.mengmeng.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

/**
 * Created by fyf on 2019/1/21
 * 红铺菜单列表数据Bean
 */
data class RedShopLeftListBean(
//        val typeList: RedShopLeftBean,
        var popularBrands: PopularBrands,
        var type: Type,
        var version: String
) {
    fun setVersion() {
        popularBrands.hotBrands?.forEach {
            it.version = version
            it.type = 1
        }
        type.typeList.forEach {
            it.version = version
            it.type = if (it.fahterId == 0L) 0 else 2
        }
    }
}

//右边热门
data class PopularBrands(var hotBrands: List<RedShopLeftBean>? = null)

data class Type(var typeList: List<RedShopLeftBean>)

@Entity
data class RedShopLeftBean(
        @Id
        var cacheId: Long = 0,
        var id: Int,
        var name: String,
        var type: Long,
        var fahterId: Long,
        var logo: String,
        var checkState: Boolean,
        var version: String
)


//package com.qingmeng.mengmeng.entity
//
//import io.objectbox.annotation.Entity
//import io.objectbox.annotation.Id
//
///**
// * Created by fyf on 2019/1/21
// * 红铺菜单列表数据Bean
// */
//class RedShopLeftListBean {
//    //        val typeList: RedShopLeftBean,
//    var popularBrands = PopularBrands()
//    var type: Type = Type()
//    var version = ""
//    var dataStr = ""
//    var dataStrHost = ""
//}
//
////右边热门
//class PopularBrands {
//    var hotBrands = ArrayList<RedShopLeftBean>()
//}
//
//class Type{
//    var typeList = ArrayList<RedShopLeftBean>()
//}
//
//@Entity
//data class RedShopLeftBean (
//        @Id
//        var cacheId: Long,
//        var id: Int,
//        var name: String ,
//        var fahterId: Long ,
//        var logo: String ,
//        var checkState: Boolean
//)











