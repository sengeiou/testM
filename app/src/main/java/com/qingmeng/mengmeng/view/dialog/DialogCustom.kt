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
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.*
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.JoinSupportAdapter
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.entity.BrandInformation
import com.qingmeng.mengmeng.entity.BrandInitialFee
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.GeetestUtil
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.utils.dp2px
import com.qingmeng.mengmeng.view.widget.MyItemView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

@Suppress("IMPLICIT_BOXING_IN_IDENTITY_EQUALS", "DEPRECATED_IDENTITY_EQUALS", "DEPRECATION")
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
        val imageView = view.findViewById<ImageView>(R.id.load_iv)
        imageView.setImageResource(R.drawable.loading_small)
        val drawable = imageView.drawable as AnimationDrawable
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
        val banCancel = view.findViewById<TextView>(R.id.dialog_image_cancel)
        val btnSure = view.findViewById<TextView>(R.id.dialog_image_confirm)
        val imageView = view.findViewById<ImageView>(R.id.dialog_image_image)
        val layout = view.findViewById<LinearLayout>(R.id.dialog_image_layout)
        val editText = view.findViewById<EditText>(R.id.dialog_image_edtext)
        mContext?.let { it ->
            view.setBackgroundColor(ContextCompat.getColor(it, R.color.white))
            Glide.with(it).load(IConstants.GET_IMAGE_CODE + phone)
                    .apply(RequestOptions().skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)).into(imageView)
            layout.setOnClickListener {
                Glide.with(it).load(IConstants.GET_IMAGE_CODE + phone)
                        .apply(RequestOptions().skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)).into(imageView)
            }
        }
        banCancel.setOnClickListener { remindDialog?.dismiss() }
        btnSure.setOnClickListener { _ ->
            if (editText.text.length !== 4) {
                mContext?.let {
                    ToastUtil.showShort(it.getString(R.string.scuuess_code))
                }
                return@setOnClickListener
            }
            ApiUtils.getApi().sendSms(phone, type, editText.text.toString())
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
        val height = getWindowHeight()
        bottomSheetDialog.windowHeight = height * 5 / 6
        bottomSheetDialog.delegate.findViewById<View>(android.support.design.R.id.design_bottom_sheet)
                ?.setBackgroundColor(mContext!!.resources.getColor(android.R.color.transparent))
        val content = view.findViewById<RecyclerView>(R.id.join_support_content)
        val compete = view.findViewById<TextView>(R.id.join_support_compete)
        compete.setOnClickListener { bottomSheetDialog.cancel() }
        content.layoutManager = LinearLayoutManager(mContext)
        content.adapter = JoinSupportAdapter(list)
        bottomSheetDialog.show()
    }

    /**
     * @param any 实体类
     * */
    fun showBrandDialog(any: Any) {
        bottomSheetDialog = MyBottomDialog(mContext!!)
        val view = LayoutInflater.from(mContext!!).inflate(R.layout.dialog_join_money, null)
        bottomSheetDialog.setContentView(view)
        val height = getWindowHeight()
        bottomSheetDialog.windowHeight = height * 5 / 6
        bottomSheetDialog.delegate.findViewById<View>(android.support.design.R.id.design_bottom_sheet)
                ?.setBackgroundColor(mContext!!.resources.getColor(android.R.color.transparent))
        val compete = view.findViewById<TextView>(R.id.join_money_compete)
        compete.setOnClickListener { bottomSheetDialog.cancel() }
        val moneyTitle = view.findViewById<TextView>(R.id.join_money_title)
        val moneyTotal = view.findViewById<MyItemView>(R.id.money_total)
        val moneyJoin = view.findViewById<MyItemView>(R.id.money_join)
        val moneyEnsure = view.findViewById<MyItemView>(R.id.money_ensure)
        val moneyEquipment = view.findViewById<MyItemView>(R.id.money_equipment)
        val moneyOther = view.findViewById<MyItemView>(R.id.money_other)
        if (any is BrandInitialFee) {
            any.apply {
                moneyTotal.setContent(affiliateFee)
                moneyJoin.setContent(joinGoldStr)
                moneyEnsure.setContent(marginStr)
                moneyEquipment.setContent(equipmentFeeStr)
                moneyOther.setContent(otherExpensesStr)
            }
        } else {
            moneyTitle.setText(R.string.brand_information)
            moneyTotal.setTitle(R.string.brand_ownership)
            moneyJoin.setTitle(R.string.franchise_mode)
            moneyEnsure.setTitle(R.string.attracting_investment_area)
            moneyEquipment.setTitle(R.string.regional_authorization)
            moneyOther.setTitle(R.string.suitable_for_crowd)
            val bean = any as BrandInformation
            moneyTotal.setContent(bean.belongName)
            var tempStr = ""
            bean.modeName.indices.forEach { tempStr += if (it == 0) bean.modeName[it] else ",${bean.modeName[it]}" }
            moneyJoin.setContent(tempStr)
            tempStr = ""
            bean.cityName.indices.forEach { tempStr += if (it == 0) bean.cityName[it] else ",${bean.cityName[it]}" }
            moneyEnsure.setContent(tempStr)
            moneyEquipment.setContent(bean.regionWarrantName)
            tempStr = ""
            bean.crowdName.indices.forEach { tempStr += if (it == 0) bean.crowdName[it] else ",${bean.crowdName[it]}" }
            moneyOther.setContent(tempStr)
        }

        bottomSheetDialog.show()
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
        val close = view.findViewById<ImageView>(R.id.join_data_close)
        val submit = view.findViewById<TextView>(R.id.join_data_submit)
        val title = view.findViewById<TextView>(R.id.join_data_title)
        val etName = view.findViewById<EditText>(R.id.join_data_name)
        val etPhone = view.findViewById<EditText>(R.id.join_data_phone)
        val etMessage = view.findViewById<EditText>(R.id.join_data_message)
        etMessage.setText(mContext!!.getString(R.string.join_data_message, shopName))
        submit.typeface = typeface
        title.typeface = typeface
        etName.typeface = typeface
        etPhone.typeface = typeface
        etMessage.typeface = typeface
        val userPhone = MainApplication.instance.user.userInfo.phone
        if (!TextUtils.isEmpty(userPhone)) {
            etPhone.setText(userPhone)
        }
        close.setOnClickListener { bottomSheetDialog.cancel() }
        etMessage.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                etMessage.setTextColor(R.color.color_333333)
            }
        }
        submit.setOnClickListener {
            val name = etName.text.toString()
            val phone = etPhone.text.toString()
            val message = etMessage.text.toString()
            when {
                TextUtils.isEmpty(name) -> ToastUtil.showShort(R.string.join_data_name)
                TextUtils.isEmpty(phone) -> ToastUtil.showShort(R.string.join_data_phone)
                TextUtils.isEmpty(message) -> ToastUtil.showShort(R.string.join_data_msg)
                else -> {
                    callback(name, phone, message)
                    bottomSheetDialog.cancel()
                }
            }
        }
        bottomSheetDialog.show()
    }

    private fun getWindowHeight(): Int {
        val wm = mContext!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        wm.defaultDisplay.getMetrics(dm)
        return dm.heightPixels
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
        val message = contentView.findViewById<TextView>(R.id.pop_more_message)
        val homepage = contentView.findViewById<TextView>(R.id.pop_more_homepage)
        val feedback = contentView.findViewById<TextView>(R.id.pop_more_feedback)
        val share = contentView.findViewById<TextView>(R.id.pop_more_share)
        message.setOnClickListener {
            msgClick()
            window.dismiss()
        }
        homepage.setOnClickListener {
            homepageClick()
            window.dismiss()
        }
        feedback.setOnClickListener {
            feedbackClick()
            window.dismiss()
        }
        share.setOnClickListener {
            shareClick()
            window.dismiss()
        }
    }
}