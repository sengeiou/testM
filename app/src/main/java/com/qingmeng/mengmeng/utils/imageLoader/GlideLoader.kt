package com.qingmeng.mengmeng.utils.imageLoader

import android.app.Activity
import android.content.Context
import android.support.v4.app.Fragment
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers

object GlideLoader : ILoader {
    override fun clearMemory(context: Any) {
        Observable.empty<Any>()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    when (context) {
                        is Activity -> Glide.get(context.application).clearMemory()
                        is Context -> Glide.get(context).clearMemory()
                        else -> throw TypeCastException("only support Context or Activity")
                    }
                }
                .subscribe()
    }

    /**
     * model 图片地址，target 加载图片控件，placeholder 默认图片，centerCrop 是否居中显示，cacheType 加载图片类型，isCircleCrop 是否圆形
     */
    override fun load(context: Any, model: Any?, target: ImageView, placeholder: Int?, error: Int?, centerCrop: Boolean, thumbSize: Float, cacheType: Int, isCircleCrop: Boolean, roundRadius: Int?) {
        val options = RequestOptions()
        options.dontAnimate()
        placeholder?.let { options.placeholder(placeholder) }
        error?.let { options.error(error) }
        when (cacheType) {
            CacheType.NONE -> {
                options.skipMemoryCache(true)
                options.diskCacheStrategy(DiskCacheStrategy.NONE)
            }
            CacheType.All -> options.diskCacheStrategy(DiskCacheStrategy.ALL)
            CacheType.RESOURCE -> options.diskCacheStrategy(DiskCacheStrategy.RESOURCE)//对应Glide 3中的DiskCacheStrategy.RESULT
            CacheType.DATA -> options.diskCacheStrategy(DiskCacheStrategy.DATA)//对应Glide 3中的DiskCacheStrategy.SOURCE
        }


        val requestManage: RequestManager? = when (context) {
            is Activity -> {
                if (context.isDestroyed.not()) {
                    Glide.with(context)
                } else {
                    Log.e("", "You cannot start a load for a destroyed activity")
                    return
                }
            }
            is Context -> Glide.with(context)
            is Fragment -> Glide.with(context)
            is android.app.Fragment -> Glide.with(context)
            else -> throw TypeCastException("only support ")
        }

        options.apply() {
            if (centerCrop) {
                options.optionalCenterCrop()
            }
            if (isCircleCrop) {
                bitmapTransform(CircleCrop())
            }
            if (roundRadius ?: 0 > 0) {
                bitmapTransform(RoundedCorners(roundRadius ?: 0))
            }
        }

        requestManage?.load(model)?.apply {
            //            thumbnail(thumbSize)
            apply(options)
        }?.into(target)
    }
}