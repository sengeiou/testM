package com.lemo.emojcenter.bean

import com.lemo.emojcenter.utils.DESUtil

/**
 * Description :
 *
 *
 * Author:fangxu
 *
 *
 * Email:634804858@qq.com
 *
 *
 * Date: 2018/1/31
 */

class OssDataBean {

    /**
     * endpoint : http://oss-cn-hangzhou.aliyuncs.com
     * buckName : erka
     * folder : face/app/2018-01-31/
     * accessUrl : http://test.rrzuzu.com/
     * oss : {"securityToken":"JMXPJOiGpILgQO8I3RxYyArjWL2ahfE8MlbKKhOyTsk8FK22gpeLksgg3Gz/l45tv1AdC8JGPleeMUssrXB9/Th18n4GXFH9VY07ZuP/Er7SVM9QOJTvk19rNq4HBO5T4IYFzyO7QrxIuFRF1hlh9SEpAijkYrUCZJ5WwyQbmBO7xX1ccfWnHSWIgTesrL8je9orDxORMx/62s4sz5A5LWtmmDXC5Jks7QyrnuCQ2w6Vc6GJ8YqAXb8R0g7dOqhhRwBNyJngSdHUhmX32bBsa/ib03IfVU3QS64raUCC3LLKOx2nIUUtc7gp98LJow414GEsaFRsK06cQ49HCgOeW09DKzvP6i2uzdlM2cqKykm+Q9nH7hdC8/TJKCyqkGBuDQCt2mlWxcMjDPYsXlKopcQfHyDZfJa9Msv8qlY8GcXF5mvt10XTQaEkk2qxSqHU8BeBePIKlsqRWPQSHOpBNARlJOiVOdTAo7LkLfxvaeUduz2BhwfFb16sjl8VgFhsuFyAdZkTqeHOV8kwQ+Ymm6CI9lV2Widm4igyyWLDG5TbauCKGqKdOqrzvxsHWcQvYkUmWl22k0pcjLWAF2KrJraqUOSd8bSQvYh72rIRmDLnZ1Lz0L84fnZR19jMVdCFcoMffufrXRq7lPt03Yyo2CprISMv6UFDwV0fiB3RHHK86mQUCHzuLg9bXJJkKwsWhRRpqIi0JGptis9TWB46ZOUziQLeuNQpKOenhoifkRUbwvLfpZxW4H7BZyeJewswYQ9Uo7S4FN0=","accessKeySecret":"+bmiEegtRqrHY02MXDf++6JAfjmdr0WA/EYLsKOMt90DSecjzztsul4krzGgCRoN","accessKeyId":"KqdkGrtUUABt4untHdB4+BdJ9BNtFjOZpbDyc87nvZo=","expiration":"PdrcNBapKMbusEcFTBWp8XXV0BjbUsL0"}
     */

    var endpoint: String? = null
    var buckName: String? = null
    var folder: String? = null
    var accessUrl: String? = null
    var oss: OssBean? = null

    class OssBean {
        /**
         * securityToken : JMXPJOiGpILgQO8I3RxYyArjWL2ahfE8MlbKKhOyTsk8FK22gpeLksgg3Gz/l45tv1AdC8JGPleeMUssrXB9/Th18n4GXFH9VY07ZuP/Er7SVM9QOJTvk19rNq4HBO5T4IYFzyO7QrxIuFRF1hlh9SEpAijkYrUCZJ5WwyQbmBO7xX1ccfWnHSWIgTesrL8je9orDxORMx/62s4sz5A5LWtmmDXC5Jks7QyrnuCQ2w6Vc6GJ8YqAXb8R0g7dOqhhRwBNyJngSdHUhmX32bBsa/ib03IfVU3QS64raUCC3LLKOx2nIUUtc7gp98LJow414GEsaFRsK06cQ49HCgOeW09DKzvP6i2uzdlM2cqKykm+Q9nH7hdC8/TJKCyqkGBuDQCt2mlWxcMjDPYsXlKopcQfHyDZfJa9Msv8qlY8GcXF5mvt10XTQaEkk2qxSqHU8BeBePIKlsqRWPQSHOpBNARlJOiVOdTAo7LkLfxvaeUduz2BhwfFb16sjl8VgFhsuFyAdZkTqeHOV8kwQ+Ymm6CI9lV2Widm4igyyWLDG5TbauCKGqKdOqrzvxsHWcQvYkUmWl22k0pcjLWAF2KrJraqUOSd8bSQvYh72rIRmDLnZ1Lz0L84fnZR19jMVdCFcoMffufrXRq7lPt03Yyo2CprISMv6UFDwV0fiB3RHHK86mQUCHzuLg9bXJJkKwsWhRRpqIi0JGptis9TWB46ZOUziQLeuNQpKOenhoifkRUbwvLfpZxW4H7BZyeJewswYQ9Uo7S4FN0=
         * accessKeySecret : +bmiEegtRqrHY02MXDf++6JAfjmdr0WA/EYLsKOMt90DSecjzztsul4krzGgCRoN
         * accessKeyId : KqdkGrtUUABt4untHdB4+BdJ9BNtFjOZpbDyc87nvZo=
         * expiration : PdrcNBapKMbusEcFTBWp8XXV0BjbUsL0
         */

        var securityToken: String? = null
            get() = DESUtil.decryptECB(field, key)
        var accessKeySecret: String? = null
            get() = DESUtil.decryptECB(field, key)
        var accessKeyId: String? = null
            get() = DESUtil.decryptECB(field, key)
        var expiration: String? = null
            get() = DESUtil.decryptECB(field, key)
        var key = "biaoqing"
    }
}
