package com.lemo.emojcenter.bean

import com.lemo.emojcenter.constant.FaceEmojOpeType

/**
 * Description:
 * Author:wxw
 * Date:2018/2/8.
 */
class EmojOpeBean(var faceId: String?) {
    var emojOpeType: FaceEmojOpeType? = null
    var emojInfoBean: EmojInfoBean? = null
    var downProgress: Int = 0

    var downloadStatus: Int = 0
}
