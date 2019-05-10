package com.qingmeng.mengmeng.entity

data class BrandBean(var name: String,
                     var directStoreNum: Int,
                     var joinStoreNum: Int,
                     var status: Int,
                     var isStand: Boolean,
                     var foodName: String,
                     var capitalName: String,
                     var planImage: Array<String>,
                     var logo: String,
                     var video: String,
                     var brandHtmlUrl: String,
                     var wxServiceId: Int,
                     var directStoreNumStr: String,
                     var joinStoreNumStr: String,
                     var avatar: String,
                     var nickname: String,
                     var isAttention: Int,
                     var affiliateSupport: AffiliateSupport,
                     var brandInformation: BrandInformation,
                     var brandInitialFee: BrandInitialFee,
                     var brandIsShow: Int = 1
)

data class AffiliateSupport(var trainContent: Array<String>? = null,
                            var operateSupport: Array<String>? = null,
                            var operationalSupervision: Array<String>? = null,
                            var locationName: String,
                            var decorationName: String,
                            var trainingMethodName: String)

data class BrandInformation(var belongName: String,
                            var modeName: Array<String>,
                            var cityName: Array<String>,
                            var regionWarrantName: String,
                            var crowdName: Array<String>)

data class BrandInitialFee(var joinGoldStr: String,
                           var marginStr: String,
                           var equipmentFeeStr: String,
                           var otherExpensesStr: String,
                           var affiliateFee: String)