package com.qingmeng.mengmeng.entity

class BannersBean(var banners: ArrayList<BannerBean>, var version: String) {
    class BannerBean(var id: Int, var sort: Int, var type: Int, var interiorDetailsId: Int, var skipType: Int,
                     var isDel: Boolean, var title: String, var imgUrl: String, var url: String)
}