package com.mogujie.tt.ui.adapter.album

import java.io.Serializable

/**
 *
 * @Description 图片对象
 * @author Nana
 * @date 2014-5-9
 */
class ImageItem : Serializable {
    var imageId: String? = null
    var thumbnailPath: String=""
    var imagePath: String=""
    var isSelected = false
}
