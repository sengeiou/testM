package com.qingmeng.mengmeng.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Created by wr Date: 2018/9/5  15:43 describe:
 * //---京东和淘宝的商铺及商品ID
 * String TaoBaoShopId ="131259851";   //--耐凡眼镜店
 * String JDShopId = "1000004123";     //--京东小米官方旗舰店
 * String TaoBaoGoodsId ="525249416835";  //--时尚潮流复古学生...眼镜框
 * String JDGoodsId = "4099139";       //--小米6详情页
 *
 *
 * //--3.京东和淘宝的包名
 * String mJDMall = "com.jingdong.app.mall"; String mTaoBao = "com.taobao.taobao";
 *
 *
 * 京东商店  shopId（商店id）
 * openApp.jdMobile://virtual?params={"category":"jump","des":"jshopMain","shopId":"1000004123","sourceType":"M_sourceFrom","sourceValue":"dp"}
 * http://shop.m.jd.com/?shopId=1000004123
 *
 *
 * 京东详情
 * openApp.jdMobile://virtual?params={"category":"jump","des":"productDetail","skuId":"4099139","sourceType":"JSHOP_SOURCE_TYPE","sourceValue":"JSHOP_SOURCE_VALUE"}
 * https://item.m.jd.com/product/4099139.web
 *
 *
 *
 *
 * 天猫商店
 * tmall://page.tm/shop?shopId=488035550
 * https://page.tm/shop?shopId=488035550
 *
 *
 * 天猫详情  id（商品id）
 * tmall://tmallclient/?{"action":"item:id=525249416835"}
 * https://detail.m.tmall.com/item.htm?id=525249416835
 *
 *
 * 淘宝商店
 * taobao://shop.m.taobao.com/shop/shop_index.htm?shop_id=488035550
 * https://shop.m.taobao.com/shop/shop_index.htm?shop_id=488035550
 * 淘宝详情
 * taobao://item.taobao.com/item.htm?id=525249416835
 * https://item.taobao.com/item.htm?id=525249416835
 */

object OpenMallApp {

    /**
     * 跳转App 跳转失败抛异常NotInstalledException
     *
     * @param context Context
     * @param url     商品,商铺地址
     */
    @Throws(NotInstalledException::class)
    fun open(context: Context, url: String) {
        try {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse(url)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            throw NotInstalledException()
        }
    }

    class NotInstalledException internal constructor() : Exception()
}
