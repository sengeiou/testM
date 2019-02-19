package com.lemo.emojcenter.adapter

import android.graphics.drawable.Drawable
import android.support.annotation.LayoutRes
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
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
import com.lemo.emojcenter.bean.DownloadBean
import com.lemo.emojcenter.bean.DownloadStatus
import com.lemo.emojcenter.constant.FaceLocalConstant
import com.lemo.emojcenter.utils.DateUtil
import com.lemo.emojcenter.utils.ImageUtils

/**
 * Description:
 * Author:wxw
 * Date:2018/1/24.
 */
class FaceDownloadAdapter(@LayoutRes layoutResId: Int, data: MutableList<DownloadBean>) : BaseQuickAdapter<DownloadBean, BaseViewHolder>(layoutResId, data) {


    override fun convert(helper: BaseViewHolder, item: DownloadBean) {
        val mTvDownload: TextView
        val mTvDownComplete: TextView
        val mProgress: ProgressBar
        //初始化控件
        helper.setText(R.id.emoj_name, item.name)
        helper.setText(R.id.download_time, DateUtil.getDateUtil(item.downloadTime))

        mTvDownload = helper.getView(R.id.download)
        mTvDownComplete = helper.getView(R.id.downloaded)
        mProgress = helper.getView(R.id.progress)

        //头像
        val iv = helper.getView<View>(R.id.download_image) as ImageView
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
        //子控件绑定点击事件
        helper.addOnClickListener(R.id.download)


        //是否下载
        if (item.isDelete == 1) {
            mTvDownload.visibility = View.VISIBLE
            mTvDownComplete.visibility = View.GONE
        } else {
            mTvDownload.visibility = View.GONE
            mTvDownComplete.visibility = View.VISIBLE
            item.downloadStatus = DownloadStatus.download_finish
        }
        //根据下载状态判断显示隐藏
        when(item.downloadStatus) {
             DownloadStatus.download_init -> {
                mTvDownload.visibility = View.VISIBLE
                mProgress.visibility = View.GONE
                mTvDownComplete.visibility = View.GONE
            }
            DownloadStatus.download_start -> {
                mTvDownload.visibility = View.GONE
                mProgress.visibility = View.VISIBLE
                mTvDownComplete.visibility = View.GONE
            }
            DownloadStatus.download_run -> {//正在下载
                mTvDownload.visibility = View.GONE
                mProgress.visibility = View.VISIBLE
                mTvDownComplete.visibility = View.GONE
                mProgress.progress = item.progress
            }
            DownloadStatus.download_finish -> {
                mTvDownload.visibility = View.GONE
                mProgress.visibility = View.GONE
                mTvDownComplete.visibility = View.VISIBLE
            }
            DownloadStatus.download_error -> {
                mTvDownload.text = "重新下载"
                mTvDownload.visibility = View.VISIBLE
                mProgress.visibility = View.GONE
                mTvDownComplete.visibility = View.GONE
            }
        }
    }

    private fun loadImage(iv: ImageView, url: String, urlNet: String, isFail: Boolean) {
        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.drawable.face_chat_img_bg)
        requestOptions.error(R.drawable.face_chat_img_bg)
        Glide.with(mContext)
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
