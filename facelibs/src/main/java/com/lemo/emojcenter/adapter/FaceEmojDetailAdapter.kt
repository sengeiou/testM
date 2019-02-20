package com.lemo.emojcenter.adapter

import android.view.View
import android.widget.ImageView

import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.lemo.emojcenter.R
import com.lemo.emojcenter.bean.EmojDetailBean

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

class FaceEmojDetailAdapter(layoutResId: Int, datas: List<EmojDetailBean.DetailsBean>) : BaseQuickAdapter<EmojDetailBean.DetailsBean, BaseViewHolder>(layoutResId, datas) {

    override fun convert(helper: BaseViewHolder, item: EmojDetailBean.DetailsBean) {

        Glide.with(mContext).load(item.master)
                .into(helper.getView<View>(R.id.item_iv_emoj_detail) as ImageView)

    }
}
