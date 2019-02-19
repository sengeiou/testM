package com.lemo.emojcenter.bean

import com.google.gson.annotations.Expose

/**
 * Description:
 * Author:wxw
 * Date:2018/1/24.
 */
class DownloadBean {


    /**
     * resource : face/zip/3181_307.zip
     * faceId : 307
     * name : 敬业福
     * cover : http://test.rrzuzu.com/face/face_details/20180206102837449_939578_cover.png
     * downloadTime : 1517895351000
     * isDelete : 1
     */


    var resource: String? = null
    var faceId: Int = 0
    var name: String? = null
    var cover: String? = null
    var downloadTime: Long = 0
    var isDelete: Int = 0

    @Expose
    var progress: Int = 0

    @Expose
    var downloadStatus: Int = 0
}
