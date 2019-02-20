package com.lemo.emojcenter.adapter

import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.lemo.emojcenter.R
import com.lemo.emojcenter.bean.DownloadStatus
import com.lemo.emojcenter.bean.EmojStoreBean
import com.lemo.emojcenter.bean.OssIconstants
import com.lemo.emojcenter.constant.FaceNetConstant

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
 * Date: 2018/1/25
 */

class FaceEmojStoreAdapter(layoutResId: Int, data: List<EmojStoreBean>?) : BaseQuickAdapter<EmojStoreBean, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: EmojStoreBean) {
        val mTvDownlod: TextView
        val mPbDownloding: ProgressBar
        val mTvDownComplete: TextView
        helper.setText(R.id.item_tv_emojstore_name, item.name)
                .setText(R.id.item_tv_emojstore_content, item.shortIntro)

        //下载按钮绑定点击事件
        helper.addOnClickListener(R.id.item_tv_emojstore_download)
        helper.addOnClickListener(R.id.item_ll_emoj_store)
        mTvDownlod = helper.getView(R.id.item_tv_emojstore_download)
        mPbDownloding = helper.getView(R.id.item_pb_emojstore_downloading)
        mTvDownComplete = helper.getView(R.id.item_emojstore_downcomplete)

        mTvDownlod.visibility = View.VISIBLE
        mPbDownloding.visibility = View.GONE
        mTvDownComplete.visibility = View.GONE

        if (item.isDelete == FaceNetConstant.IsDown.DOWN) {
            mTvDownlod.visibility = View.GONE
            mPbDownloding.visibility = View.GONE
            mTvDownComplete.visibility = View.VISIBLE
            item.downloadStatus = DownloadStatus.download_finish
        }
        if (item.isDelete == FaceNetConstant.IsDown.DOWN_NOT) {
            mTvDownlod.visibility = View.VISIBLE
            mPbDownloding.visibility = View.GONE
            mTvDownComplete.visibility = View.GONE
        }

        when (item.downloadStatus) {
            DownloadStatus.download_init -> {
                //没下载
                mTvDownlod.visibility = View.VISIBLE
                mPbDownloding.visibility = View.GONE
                mTvDownComplete.visibility = View.GONE
            }
            DownloadStatus.download_error -> {
                //下载失败
                mTvDownlod.text = "重新下载"
                mTvDownlod.setTextColor(ContextCompat.getColor(mContext, R.color.down_error))
                mTvDownlod.setBackgroundResource(R.drawable.face_emoj_down_error_bg_shape)
                mTvDownlod.visibility = View.VISIBLE
                mPbDownloding.visibility = View.GONE
                mTvDownComplete.visibility = View.GONE
            }
            DownloadStatus.download_finish -> {
                //下载完成
                mTvDownlod.visibility = View.GONE
                mPbDownloding.visibility = View.GONE
                mTvDownComplete.visibility = View.VISIBLE
            }
            else -> {
                //正在下载
                mPbDownloding.progress = item.progress
                mTvDownlod.visibility = View.GONE
                mPbDownloding.visibility = View.VISIBLE
                mTvDownComplete.visibility = View.GONE
            }
        }

        val requestOptions = RequestOptions()
        requestOptions.error(R.mipmap.face_img_error)
        requestOptions.diskCacheStrategy(DiskCacheStrategy.RESOURCE)

        //http://bpic.588ku.com//element_pic/16/12/11/08a7ed5263cde1df5390328d9f43349b.jpg!/fh/208/quality/90/unsharp/true/compress/true
        //http://p0.so.qhimgs1.com/bdr/_240_/t01af13e6ab66fafb0c.jpg
        Glide.with(mContext)
                .load(item.cover + OssIconstants.OSS_SIZE_EMOJ_ITEM)
                .apply(requestOptions)
                .into(helper.getView<View>(R.id.item_emojstore_avater) as ImageView)
    }

}
