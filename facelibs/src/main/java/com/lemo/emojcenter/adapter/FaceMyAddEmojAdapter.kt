package com.lemo.emojcenter.adapter

import android.text.TextUtils
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.lemo.emojcenter.R
import com.lemo.emojcenter.bean.MyAddEmojBean
import com.lemo.emojcenter.bean.OssIconstants

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
 * Date: 2018/1/27
 */

class FaceMyAddEmojAdapter(layoutResId: Int, private val mDatas: List<MyAddEmojBean>?) : BaseQuickAdapter<MyAddEmojBean, BaseViewHolder>(layoutResId, mDatas) {
    internal var isEdit: Boolean = false
    internal lateinit var mCheckBoxSelectListener: CheckBoxSelectListener
    private var mOnClickListener: ImageClickListener? = null
    private var state: Boolean = false

    override fun convert(helper: BaseViewHolder, item: MyAddEmojBean) {

        val mCheckBox = helper.getView<CheckBox>(R.id.item_add_emoj_cb)
        val imageView = helper.getView<ImageView>(R.id.img)
        var mPath = item.cover
        if (!TextUtils.isEmpty(mPath)) {
            if ("http" == mPath!!.subSequence(0, 4)) {
                mPath += OssIconstants.OSS_SIZE_FOODS_ITEM
            }
        }
        //是编辑状态,让checkbox显示
        if (isEdit) {
            helper.getView<View>(R.id.item_add_emoj_cb).visibility = View.VISIBLE
        } else {
            helper.getView<View>(R.id.item_add_emoj_cb).visibility = View.GONE
        }
        //设置checkbox是否选中状态
        mCheckBox.isChecked = state
        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.mipmap.face_icon_error)
        requestOptions.error(R.mipmap.face_icon_error)
        //最后一张上传图片按钮
        val isUpImage = !isEdit && helper.layoutPosition == itemCount - 1
        if (isUpImage) {
            helper.getView<View>(R.id.item_add_emoj_cb).visibility = View.GONE
            Glide.with(mContext).load(R.mipmap.face_tianjia).into(imageView)
        } else {
            Glide.with(mContext)
                    .load(mPath)
                    .apply(requestOptions)
                    .into(imageView)//不是最后一张图片,正常显示表情
        }


        mCheckBox.setOnCheckedChangeListener { buttonView, isChecked -> mCheckBoxSelectListener.selecet(helper.layoutPosition, isChecked) }
        imageView.setOnClickListener { v ->
            if (itemCount > 0 && !isUpImage) {
                val isChecked = !mCheckBox.isChecked
                mCheckBox.isChecked = isChecked
            }
            if (isUpImage) {//条目点击事件,如果是最后一条条目
                if (mOnClickListener != null) {
                    mOnClickListener!!.click(v, helper.layoutPosition)
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return if (isEdit) {
            if (super.getItemCount() == 0) {
                0
            } else {
                super.getItemCount() - 1
            }
        } else {
            super.getItemCount()
        }
    }

    fun setStatusCode(isEdit: Boolean) {
        this.isEdit = isEdit
        this.notifyDataSetChanged()
    }

    interface CheckBoxSelectListener {
        fun selecet(positon: Int, isCheck: Boolean)
    }

    interface ImageClickListener {
        fun click(v: View, position: Int)
    }

    fun setCheckBoxSelectListener(checkBoxSelectListener: CheckBoxSelectListener) {
        mCheckBoxSelectListener = checkBoxSelectListener
    }

    fun setImageClickListener(onClickListener: ImageClickListener) {
        this.mOnClickListener = onClickListener
    }

    fun setCheckBoxState(state: Boolean) {
        this.state = state
        notifyDataSetChanged()
    }
}
