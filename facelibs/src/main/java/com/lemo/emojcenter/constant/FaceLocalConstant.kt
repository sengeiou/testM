package com.lemo.emojcenter.constant

/**
 * Created by wangru
 * Date: 2018/3/27  11:28
 * mail: 1902065822@qq.com
 * describe:
 */

interface FaceLocalConstant {

    interface Key {
        companion object {
            val USER_ID = "userId"

            val EMOJDETAIL_ID = "emoj_id"
            val EMOJDETAIL_TYPE = "type"
        }

    }

    interface Value {
        companion object {
            val EMOJDETAIL_TYPE_FACE = 1
            //从聊天进入
            val EMOJDETAIL_TYPE_CHAT = 2
        }
    }

    interface CollectEditStatu {
        companion object {
            val INIT = 0
            val DOING = 1
        }
    }

    interface ImageLoadStatu {
        companion object {
            val Success = 1
            val Fail = 2
        }
    }

    companion object {
        const val IMGTYPE_COLLECT = 2
        const val IMGTYPE_FACE = 1
        const val IMGTYPE_EMOJ = 3
        const val IMGTYPE_EMOJ_DELETE = 4
        val FACE_SELF = "faceSelf"
        //emoj 表情id
        val FACE_ID_EMOJ = "-2"
        //收藏 表情id
        val FACE_ID_COLLECT = "-3"

        val DELETE_KEY = "delete_emojiion_key"

        val VERSION_EMOJ = "version_emoj"

        val COLLECT_TYPE_DESC = "[表情图片]"
        val IS_SHOW_COLLECT_ADD_TIIP = "showAddImageTip"

        val USER_ID_FACE = "faceCurrentUserId"
    }

}
