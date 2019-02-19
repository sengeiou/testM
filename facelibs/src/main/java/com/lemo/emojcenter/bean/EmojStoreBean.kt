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
 * Date: 2018/1/25
 */

class EmojStoreBean {

    /**
     * id : 4
     * name : http://bpic.588ku.com//element_pic/16/12/11/08a7ed5263cde1df5390328d9f43349b.jpg!/fh/208/quality/90/unsharp/true/compress/true
     * cover : 人心1
     * shortIntro : 得人心
     */

    var id: Int = 0
    var name: String? = null
    var cover: String? = null
    var shortIntro: String? = null
    var resource: String? = null
    var isDelete = -1
    @Expose
    var progress: Int = 0
    @Expose
    var downloadStatus: Int = 0
}
