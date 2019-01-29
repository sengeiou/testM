package com.qingmeng.mengmeng.entity

data class BrandBean(var name: String,
                     var directStoreNum: Int,
                     var joinStoreNum: Int,
                     var status: Int,
                     var foodName: String,
                     var capitalName: String,
                     var planImage: Array<String>,
                     var video: String,
                     var brandHtmlUrl: String,
                     var isAttention: Int,
                     var affiliateSupport: AffiliateSupport,
                     var brandInformation: BrandInformation,
                     var brandInitialFee: BrandInitialFee)

data class AffiliateSupport(var trainContent: Array<String>? = null,
                            var operateSupport: Array<String>? = null,
                            var operationalSupervision: Array<String>? = null,
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