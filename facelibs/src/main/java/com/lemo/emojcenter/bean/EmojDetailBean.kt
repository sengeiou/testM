package com.lemo.emojcenter.bean

import com.google.gson.annotations.Expose

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
 * Date: 2018/1/26
 */

class EmojDetailBean {

    /**
     * id : 3
     * name : 人心
     * intro : 老阔疼
     * cover : http://bpic.588ku.com//element_pic/16/12/11/08a7ed5263cde1df5390328d9f43349b.jpg!/fh/208/quality/90/unsharp/true/compress/true
     * emotionType : 0
     * sortBy : 0
     * details : [{"id":0,"master":"master2"},{"id":0,"master":"http://test.rrzuzu.com/biaoqing/banner/20180123201000940_13742718.png"}]
     */

    var id: Int = 0
    var name: String? = null
    var intro: String? = null
    var cover: String? = null

    var banner: String? = null
    var resource: String? = null
    var emotionType: Int = 0
    var sortBy: Int = 0

    var isDelete: Int = 0
    var details: List<DetailsBean>? = null
    @Expose
    var downloadStatus: Int = 0

    class DetailsBean {
        /**
         * id : 0
         * master : master2
         */

        var id: Int = 0
        var master: String? = null
        var thumb: String? = null
    }
}
