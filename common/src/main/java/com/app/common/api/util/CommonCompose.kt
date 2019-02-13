package com.app.common.api.util

import com.app.common.api.ApiException
import com.app.common.json.GsonUtil
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by wr
 * Date: 2019/1/14  20:36
 * mail: 1902065822@qq.com
 * describe:
 */
fun <T> composeCommon(): ObservableTransformer<T, T> {
    return ObservableTransformer { observable ->
        observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
}