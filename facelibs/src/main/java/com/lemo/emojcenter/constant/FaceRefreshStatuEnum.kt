package com.lemo.emojcenter.constant

/**
 * Created by wangru
 * Date: 2018/3/29  13:18
 * mail: 1902065822@qq.com
 * describe:
 */

enum class FaceRefreshStatuEnum {
    DEFAULT,
    /**
     * 首次进入
     */
    FIRST,
    /**
     * 刷新
     */
    REFRESH,
    /**
     * 加载
     */
    LOAD,
    /**
     * 首次完成
     */
    FIRST_SUC,
    /**
     * 首次失败
     */
    FIRST_FAIL,
    /**
     * 刷新完成
     */
    REFRESH_SUC,
    /**
     * 刷新完成
     */
    REFRESH_FAIL,
    /**
     * 加载完成
     */
    LOAD_SUC,
    /**
     * 加载失败
     */
    LOAD_FAIL,
    /**
     * 全部加在完成
     */
    LOAD_OVER_ALL,
    /**
     * 没有数据
     */
    NULL;

    /**
     * 正在请求数据（刷新或加载）
     */
    val isDoing: Boolean
        get() = this == REFRESH || this == LOAD || this == FIRST

    val isRefresh: Boolean
        get() = this == REFRESH || this == REFRESH_SUC || this == REFRESH_FAIL

    val isLoad: Boolean
        get() = this == LOAD || this == LOAD_SUC || this == REFRESH_FAIL

    val isFirst: Boolean
        get() = this == FIRST || this == FIRST_SUC || this == FIRST_FAIL


    /**
     * 得到完成状态
     */
    fun setStatuSuc(): FaceRefreshStatuEnum {
        var mRefreshStatu = DEFAULT
        if (this == FaceRefreshStatuEnum.REFRESH) {
            mRefreshStatu = REFRESH_SUC
        }
        if (this == FaceRefreshStatuEnum.LOAD) {
            mRefreshStatu = LOAD_SUC
        }
        if (this == FaceRefreshStatuEnum.FIRST) {
            mRefreshStatu = FIRST_SUC
        }
        return mRefreshStatu
    }

    fun setStatuFail(): FaceRefreshStatuEnum {
        var mRefreshStatu = DEFAULT
        if (this == REFRESH) {
            mRefreshStatu = REFRESH_FAIL
        }
        if (this == LOAD) {
            mRefreshStatu = LOAD_FAIL
        }
        if (this == FIRST) {
            mRefreshStatu = FIRST_FAIL
        }
        return mRefreshStatu
    }

}
