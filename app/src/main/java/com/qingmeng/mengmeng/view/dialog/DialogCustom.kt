package com.qingmeng.mengmeng.view.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.JoinSupportAdapter
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.entity.BrandInformation
import com.qingmeng.mengmeng.entity.BrandInitialFee
import com.qingmeng.mengmeng.entity.UpdateBean
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.GeetestUtil
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.utils.dp2px
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_join_money.*
import kotlinx.android.synthetic.main.dialog_join_support.view.*
import kotlinx.android.synthetic.main.dialog_want_to_join.view.*
import kotlinx.android.synthetic.main.layout_dialog_imageedtext.view.*
import kotlinx.android.synthetic.main.layout_dialog_loading.view.*
import kotlinx.android.synthetic.main.layout_dialog_update.*
import kotlinx.android.synthetic.main.layout_pop_more.view.*

@Suppress("IMPLICIT_BOXING_IN_IDENTITY_EQUALS", "DEPRECATED_IDENTITY_EQUALS", "DEPRECATION",
        "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@SuppressLint("ObsoleteSdkInt", "ClickableViewAccessibility", "ResourceAsColor", "InflateParams")
class DialogCustom(private var mContext: Context?) {
    private var remindDialog: Dialog? = null
    private var loadingDialog: Dialog? = null
    private lateinit var bottomSheetDialog: MyBottomDialog
    private var screenWidth: Int = mContext?.resources?.displayMetrics?.widthPixels ?: 0

    fun unBindContext() {
        screenWidth = 0
        mContext = null
        remindDialog?.let { remindDialog = null }
        loadingDialog?.let { loadingDialog = null }
    }

    fun dismissLoadingDialog() {
        if (mContext != null && !(mContext as Activity).isFinishing) {
            loadingDialog?.let {
                if (it.isShowing) {
                    it.dismiss()
                }
            }
        }
    }

    fun showLoadingDialog() {
        loadingDialog?.let {
            if (it.isShowing) {
                return@let
            }
        }
        val view = View.inflate(mContext, R.layout.layout_dialog_loading, null)
        view.load_iv.setImageResource(R.drawable.loading_small)
        val drawable = view.load_iv.drawable as AnimationDrawable
        drawable.start()
        loadingDialog = Dialog(mContext!!, R.style.dialog_loading)
        loadingDialog?.apply {
            setContentView(view)
            window?.setGravity(Gravity.CENTER)
            setCanceledOnTouchOutside(false)
            setCancelable(true)
            val activity = mContext as Activity
            if (activity.isFinishing || (Build.VERSION.SDK_INT >= 17 && activity.isDestroyed)) {
                return
            }
            show()
        }
    }

    fun showImageCodeDialog(phone: String, type: Int, sureClick: (disposable: Disposable) -> Unit, startTiming: () -> Unit) {
        remindDialog?.let {
            if (it.isShowing) {
                return@let
            }
        }
        remindDialog = Dialog(mContext!!, R.style.commondialogstyle)
        val view = View.inflate(mContext, R.layout.layout_dialog_imageedtext, null)
        mContext?.let { it ->
            view.setBackgroundColor(ContextCompat.getColor(it, R.color.white))
            Glide.with(it).load(IConstants.GET_IMAGE_CODE + phone)
                    .apply(RequestOptions().skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)).into(view.dialog_image_image)
            view.dialog_image_layout.setOnClickListener {
                Glide.with(it).load(IConstants.GET_IMAGE_CODE + phone)
                        .apply(RequestOptions().skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)).into(view.dialog_image_image)
            }
        }
        view.dialog_image_cancel.setOnClickListener { remindDialog?.dismiss() }
        view.dialog_image_confirm.setOnClickListener { _ ->
            if (view.dialog_image_edtext.text.length !== 4) {
                mContext?.let {
                    ToastUtil.showShort(it.getString(R.string.scuuess_code))
                }
                return@setOnClickListener
            }
            ApiUtils.getApi().sendSms(phone, type, view.dialog_image_edtext.text.toString())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        if (it.code == 12000) {
                            startTiming()
                            remindDialog?.dismiss()
                        } else {
                            ToastUtil.showShort(it.msg)
                            remindDialog?.dismiss()
                        }
                    }, {
                        GeetestUtil.showFailedDialog()
                        ToastUtil.showNetError()
                    }, {}, { sureClick(it) })
        }
        remindDialog?.setContentView(view)
        val lp = remindDialog?.window?.attributes
        if (screenWidth == 0) {
            mContext?.let {
                screenWidth = it.resources.displayMetrics.widthPixels
            }
        }
        lp?.let {
            it.width = screenWidth * 4 / 5 // 设置宽度
            remindDialog?.window?.attributes = lp
            remindDialog?.setCanceledOnTouchOutside(false)
            remindDialog?.setCancelable(false)
            remindDialog?.show()
        }
    }

    fun showBrandDialog(list: ArrayList<String>) {
        bottomSheetDialog = MyBottomDialog(mContext!!)
        val view = LayoutInflater.from(mContext!!).inflate(R.layout.dialog_join_support, null)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.windowHeight = mContext!!.dp2px(500)
        bottomSheetDialog.delegate.findViewById<View>(android.support.design.R.id.design_bottom_sheet)
                ?.setBackgroundColor(mContext!!.resources.getColor(android.R.color.transparent))
        view.join_support_compete.setOnClickListener { bottomSheetDialog.cancel() }
        view.join_support_content.layoutManager = LinearLayoutManager(mContext)
        view.join_support_content.adapter = JoinSupportAdapter(list)
        bottomSheetDialog.show()
    }

    /**
     * @param any 实体类
     * */
    fun showBrandDialog(any: Any) {
        BrandDialog(mContext!!, any).show()
    }

    private inner class BrandDialog(context: Context, any: Any) : Dialog(context, R.style.dialog_share) {

        init {
            setContentView(R.layout.dialog_join_money)
            val lp = window.attributes
            lp.width = context.resources.displayMetrics.widthPixels // 设置宽度
            window.setGravity(Gravity.BOTTOM)
            window.attributes = lp
            join_money_compete.setOnClickListener { cancel() }
            if (any is BrandInitialFee) {
                any.apply {
                    money_total.setContent(affiliateFee)
                    money_join.setContent(joinGoldStr)
                    money_ensure.setContent(marginStr)
                    money_equipment.setContent(equipmentFeeStr)
                    money_other.setContent(otherExpensesStr)
                }
                money_tip.visibility = View.VISIBLE
            } else {
                join_money_title.setText(R.string.brand_information)
                money_total.setTitle(R.string.brand_ownership)
                money_join.setTitle(R.string.franchise_mode)
                money_ensure.setTitle(R.string.attracting_investment_area)
                money_equipment.setTitle(R.string.regional_authorization)
                money_other.setTitle(R.string.suitable_for_crowd)
                val bean = any as BrandInformation
                money_total.setContent(bean.belongName)
                var tempStr = ""
                bean.modeName.indices.forEach { tempStr += if (it == 0) bean.modeName[it] else ",${bean.modeName[it]}" }
                money_join.setContent(tempStr)
                tempStr = ""
                bean.cityName.indices.forEach { tempStr += if (it == 0) bean.cityName[it] else ",${bean.cityName[it]}" }
                money_ensure.setContent(tempStr)
                money_equipment.setContent(bean.regionWarrantName)
                tempStr = ""
                bean.crowdName.indices.forEach { tempStr += if (it == 0) bean.crowdName[it] else ",${bean.crowdName[it]}" }
                money_other.setContent(tempStr)
                money_tip.visibility = View.GONE
            }
        }
    }

    /**
     * 版本更新提示框
     */
    fun showVersionUpdateDialog(updateBean: UpdateBean, update: (UpdateDialog, View) -> Unit) {
        mContext?.let { UpdateDialog(it, updateBean, update).show() }
    }

    class UpdateDialog(context: Context, updateBean: UpdateBean, var update: (UpdateDialog, View) -> Unit) : Dialog(context, R.style.commondialogstyle) {
        init {
            setContentView(R.layout.layout_dialog_update)
            updateClose.visibility = if (updateBean.forceUpdate == 0) View.VISIBLE else View.GONE
            updateContent.text = updateBean.versionDoc
            setCanceledOnTouchOutside(updateBean.forceUpdate == 0)
            setCancelable(updateBean.forceUpdate == 0)
            updateClose.setOnClickListener { cancel() }
            updateImmediately.setOnClickListener {
                update(this, it)
            }
        }

        fun showProgress() {
            waveProgress.visibility = View.VISIBLE
        }

        fun setProgress(progress: Int) {
            if (progress == -1) {
                waveProgress.setProgress(0)
                waveProgress.visibility = View.GONE
                updateImmediately.isClickable = true
            } else {
                waveProgress.setProgress(progress)
                if (progress == 100) {
                    waveProgress.visibility = View.GONE
                    updateImmediately.isClickable = true
                }
            }
        }
    }

    /**
     * @param callback 姓名 手机号 留言
     */
    fun showJoinDataDialog(shopName: String, callback: (String, String, String) -> Unit) {
        bottomSheetDialog = MyBottomDialog(mContext!!)
        val view = LayoutInflater.from(mContext!!).inflate(R.layout.dialog_want_to_join, null)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.windowHeight = ViewGroup.LayoutParams.MATCH_PARENT
        bottomSheetDialog.delegate.findViewById<View>(android.support.design.R.id.design_bottom_sheet)
                ?.setBackgroundColor(mContext!!.resources.getColor(android.R.color.transparent))
        val typeface = getTypeface()
        view.joinDataMessage.setText(mContext!!.getString(R.string.join_data_message, shopName))
        view.joinDataSubmit.typeface = typeface
        view.joinDataTitle.typeface = typeface
        view.joinDataName.typeface = typeface
        view.joinDataPhone.typeface = typeface
        view.joinDataMessage.typeface = typeface
        val userPhone = MainApplication.instance.user.userInfo.phone
        if (!TextUtils.isEmpty(userPhone)) {
            view.joinDataPhone.setText(userPhone)
        }
        view.joinDataClose.setOnClickListener { bottomSheetDialog.cancel() }
        view.joinDataMessage.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                view.joinDataMessage.setTextColor(R.color.color_333333)
            }
        }
        view.joinDataSubmit.setOnClickListener {
            val name = view.joinDataName.text.toString()
            val phone = view.joinDataPhone.text.toString()
            val message = view.joinDataMessage.text.toString()
            when {
                TextUtils.isEmpty(name) -> ToastUtil.showShort(R.string.join_data_name)
                TextUtils.isEmpty(phone) -> ToastUtil.showShort(R.string.join_data_phone)
                phone.length < 11 -> ToastUtil.showShort(R.string.correct_phone)
                TextUtils.isEmpty(message) -> ToastUtil.showShort(R.string.join_data_msg)
                else -> {
                    callback(name, phone, message)
                    bottomSheetDialog.cancel()
                }
            }
        }
        bottomSheetDialog.show()
    }

    private fun getTypeface(): Typeface {
        return Typeface.createFromAsset(mContext!!.assets, "fonts/join_data.ttf")
    }

    fun showMorePop(view: View, msgClick: () -> Unit, homepageClick: () -> Unit, feedbackClick: () -> Unit, shareClick: () -> Unit) {
        // 用于PopupWindow的View
        val contentView = LayoutInflater.from(mContext!!).inflate(R.layout.layout_pop_more, null, false)
        // 创建PopupWindow对象，其中：
        // 第一个参数是用于PopupWindow中的View，第二个参数是PopupWindow的宽度，
        // 第三个参数是PopupWindow的高度，第四个参数指定PopupWindow能否获得焦点
        val window = PopupWindow(contentView, mContext!!.dp2px(105), mContext!!.dp2px(122), true)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.showAsDropDown(view, mContext!!.dp2px(-80), mContext!!.dp2px(5))
        contentView.popMoreMessage.setOnClickListener {
            msgClick()
            window.dismiss()
        }
        contentView.popMoreHomepage.setOnClickListener {
            homepageClick()
            window.dismiss()
        }
        contentView.popMoreFeedback.setOnClickListener {
            feedbackClick()
            window.dismiss()
        }
        contentView.popMoreShare.setOnClickListener {
            shareClick()
            window.dismiss()
        }
    }
}