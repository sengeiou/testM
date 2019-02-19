package com.lemo.emojcenter.bean

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Created by wangru
 * Date: 2018/3/27  17:11
 * mail: 1902065822@qq.com
 * describe:
 */

class EmojInfoBean {

    /**
     * faceDetails : [{"id":1296,"keyword":"睡觉","name":"01","master":"http://test.rrzuzu.com/face/face_details/8895_339/20180206194704595_389856_01.gif","thumb":"http://test.rrzuzu.com/face/face_details/8895_339/20180206194655288_372562_01.png"},{"id":1297,"keyword":"有点道理","name":"02","master":"http://test.rrzuzu.com/face/face_details/8895_339/20180206194704617_155704_02.gif","thumb":"http://test.rrzuzu.com/face/face_details/8895_339/20180206194655301_143688_02.png"},{"id":1298,"keyword":"生气","name":"03","master":"http://test.rrzuzu.com/face/face_details/8895_339/20180206194704660_785871_03.gif","thumb":"http://test.rrzuzu.com/face/face_details/8895_339/20180206194655313_382211_03.png"},{"id":1299,"keyword":"捏脸","name":"04","master":"http://test.rrzuzu.com/face/face_details/8895_339/20180206194704656_722932_04.gif","thumb":"http://test.rrzuzu.com/face/face_details/8895_339/20180206194655343_091032_04.png"},{"id":1300,"keyword":"思考","name":"05","master":"http://test.rrzuzu.com/face/face_details/8895_339/20180206194704671_715066_05.gif","thumb":"http://test.rrzuzu.com/face/face_details/8895_339/20180206194655358_254394_05.png"},{"id":1301,"keyword":"泪奔","name":"06","master":"http://test.rrzuzu.com/face/face_details/8895_339/20180206194729817_791533_06.gif","thumb":"http://test.rrzuzu.com/face/face_details/8895_339/20180206194655229_659110_06.png"},{"id":1302,"keyword":"诵经中","name":"07","master":"http://test.rrzuzu.com/face/face_details/8895_339/20180206195218422_473727_07.gif","thumb":"http://test.rrzuzu.com/face/face_details/8895_339/20180206194655235_051576_07.png"},{"id":1303,"keyword":"吃饭","name":"08","master":"http://test.rrzuzu.com/face/face_details/8895_339/20180206194732267_460220_08.gif","thumb":"http://test.rrzuzu.com/face/face_details/8895_339/20180206194655250_722746_08.png"}]
     * icon : http://test.rrzuzu.com/face/face_details/20180206195500412_957406_icon.png
     */
    @SerializedName(value = "faceId", alternate = arrayOf("id"))
    var faceId: Int = 0
    @SerializedName("icon")
    var icon: String? = null
    @SerializedName("name")
    var name: String? = null
    @SerializedName("cover")
    var cover: String? = null
    @SerializedName("resource")
    var resource: String? = null
    @SerializedName(value = "faceDetails", alternate = arrayOf("faceDetailDTOs"))
    var itemList: List<ItemBean>? = ArrayList()

    /**
     * 表情详情是否不为空
     *
     * @return
     */
    val isNotNull: Boolean?
        get() = this != null && this.faceId > 0 && this.itemList != null && this.itemList!!.size > 0

    class ItemBean {
        /**
         * id : 1296
         * keyword : 睡觉
         * name : 01
         * master : http://test.rrzuzu.com/face/face_details/8895_339/20180206194704595_389856_01.gif
         * thumb : http://test.rrzuzu.com/face/face_details/8895_339/20180206194655288_372562_01.png
         */

        @SerializedName("id")
        var id: Int = 0
        @SerializedName("keyword")
        var keyword: String? = null
        @SerializedName("name")
        var name: String? = null
        @SerializedName("master")
        var master: String? = null
        @SerializedName("thumb")
        var thumb: String? = null
    }
}
