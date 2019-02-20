package com.lemo.emojcenter.adapter

import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.lemo.emojcenter.FaceConfigInfo
import com.lemo.emojcenter.R
import com.lemo.emojcenter.bean.EmojInfoBean
import com.lemo.emojcenter.constant.FaceLocalConstant
import com.lemo.emojcenter.utils.ImageUtils

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

class FaceMyEmojAdapter(layoutResId: Int, data: List<EmojInfoBean>?) : BaseQuickAdapter<EmojInfoBean, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: EmojInfoBean) {

        helper.setText(R.id.item_tv_myemoj_name, item.name)
                .addOnClickListener(R.id.item_myemoj_remove)


        val iv = helper.getView<View>(R.id.item_myemoj_avater) as ImageView
        //没有tag 或 tag不是当前地址  或没加载成功，才重新加载
        val isLoad = iv.tag == null || !TextUtils.equals(iv.getTag(R.id.face_image_uri) as String, item.cover) || iv.getTag(R.id.face_image_statu) as Int != FaceLocalConstant.ImageLoadStatu.Success
        if (isLoad) {
            iv.setTag(R.id.face_image_uri, item.cover)
            val urlNet = item.cover
            val pathLocal = FaceConfigInfo.getPathFaceCover(item.faceId.toString())
            var url = urlNet
            if (ImageUtils.isImageComplete(pathLocal)) {
                url = pathLocal
            }
            url?.let { urlNet?.let { it1 -> loadImage(iv, it, it1, false) } }
        } else {
            Log.d(BaseQuickAdapter.TAG, "convert: 复用" + item.cover)
        }
    }

    private fun loadImage(iv: ImageView, url: String, urlNet: String, isFail: Boolean) {
        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.mipmap.face_icon_error)
        requestOptions.error(R.mipmap.face_icon_error)
        Glide.with(mContext.applicationContext)
                .load(url)
                .apply(requestOptions)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                        if (!isFail && !TextUtils.isEmpty(url) && !url.startsWith("http")) {
                            loadImage(iv, urlNet, urlNet, true)
                        } else {
                            iv.setTag(R.id.face_image_statu, FaceLocalConstant.ImageLoadStatu.Fail)
                        }
                        return false
                    }

                    override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        iv.setTag(R.id.face_image_statu, FaceLocalConstant.ImageLoadStatu.Success)
                        return false
                    }
                })
                .into(iv)
    }
}
