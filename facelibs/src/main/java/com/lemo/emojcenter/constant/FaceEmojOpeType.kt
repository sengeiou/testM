package com.lemo.emojcenter.constant

/**
 * 表情包操作
 * Created by wangru
 * Date: 2018/3/31  12:42
 * mail: 1902065822@qq.com
 * describe:
 */

enum class FaceEmojOpeType {
    //收藏添加完成
    CollectsAdd,
    //收藏删除完成
    CollectsDelete,
    //收藏排序完成
    CollectsSort,
    //表情包添加完成
    EmojAdd,
    //表情包下载
    EmojDowning,
    //表情包删除完成
    EmojDelete,
    //表情包现在完成
    EmojOver;

    /**
     * 收藏表情改变
     *
     * @return
     */
    val isCollectsChange: Boolean
        get() = this == CollectsAdd || this == CollectsDelete || this == CollectsSort

    /**
     * 表情包变化
     *
     * @return
     */
    val isEmojChange: Boolean
        get() = this == EmojAdd || this == EmojDelete || this == EmojOver || this == EmojDowning

    /**
     * 所有表情变化
     *
     * @return
     */
    val isFaceChange: Boolean
        get() = isCollectsChange || this == EmojDelete || this == EmojOver
}
