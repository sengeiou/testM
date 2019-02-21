package com.qingmeng.mengmeng.activity

import android.app.Activity
import android.content.Intent
import android.view.Gravity
import android.view.View
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.entity.AllCity
import com.qingmeng.mengmeng.entity.MySettingsUserBean
import com.qingmeng.mengmeng.entity.SelectBean
import com.qingmeng.mengmeng.utils.*
import com.qingmeng.mengmeng.utils.imageLoader.CacheType
import com.qingmeng.mengmeng.utils.imageLoader.GlideLoader
import com.qingmeng.mengmeng.utils.photo.PhotoConfig
import com.qingmeng.mengmeng.utils.photo.SimplePhotoUtil
import com.qingmeng.mengmeng.view.dialog.PopCitySelect
import com.qingmeng.mengmeng.view.dialog.SelectDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_my_settings_user.*
import kotlinx.android.synthetic.main.layout_head.*


/**
 *  Description :设置 - 修改用户信息

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/3
 */
class MySettingsUserActivity : BaseActivity() {
    private lateinit var mBottomDialog: SelectDialog           //自定义dialog
    private lateinit var mPopCity: PopCitySelect               //城市选择pop
    private var mMySettingsUserBean = MySettingsUserBean()     //个人信息bean
    private var mMoneyList = ArrayList<SelectBean>()           //创业资本数据
    private var mInterestList = ArrayList<SelectBean>()        //感兴趣行业数据
    private var mLocalPath = ""                                //本地选好的图片路径
    private var mCanHttpLoad = true                            //是否可以再次请求
    //一些必填的内容
    private var mName: String = ""                   //真实姓名
    private var mPhone: String = ""                  //手机号
    private var mDistrictId: Int = 0                 //县id
    private var mCapitalId: Int = 0                  //创业资本
    private var mIndustryOfInterest = ""             //感兴趣行业
    //一些选填的内容
    private var mAvatar: String = ""                 //头像地址
    private var mSex: Int = 0                        //年龄
    private var mTelephone: String = ""              //固定电话
    private var mWx: String = ""                     //微信
    private var mQQ: String = ""                     //qq
    private var mEmail: String = ""                  //邮箱

    override fun getLayoutId(): Int {
        return R.layout.activity_my_settings_user
    }

    override fun initObject() {
        super.initObject()

        setHeadName(R.string.my_settings_user_title)
        slMySettingsUser.visibility = View.GONE

        httpLoad()
    }

    override fun initListener() {
        super.initListener()

        //返回
        mBack.setOnClickListener {
            onBackPressed()
        }

        //保存
        mMenu.setOnClickListener {
            //文本赋值
            mName = etMySettingsName.text.toString()
            mTelephone = etMySettingsUserTelephone.text.toString()
            mWx = etMySettingsUserWechat.text.toString()
            mQQ = etMySettingsUserQQ.text.toString()
            mEmail = etMySettingsUserEmail.text.toString()
            //验证内容是否合法和接口请求
            contentVerification()
        }

        //空白处点击刷新
        llMySettingsUserTips.setOnClickListener {
            httpLoad()
        }

        //头像
        llMySettingsUserHead.setOnClickListener { _ ->
            //菜单内容
            val menuList = arrayListOf(SelectBean(name = getString(R.string.photograph)), SelectBean(name = getString(R.string.albumSelect)))
            mBottomDialog = SelectDialog(this, menuList, onItemClick = {
                //拍照
                if (menuList[it].name == getString(R.string.photograph)) {
                    PermissionUtils.camera(this) {
                        //打开相机方法
                        openCameraAndUploadServer()
                    }
                } else {  //相册选择
                    PermissionUtils.readAndWrite(context = this, readAndWriteCallback = {
                        //打开相册方法
                        openAlbumAndUploadServer()
                    })
                }
            })
            mBottomDialog.show()
        }

        //性别
        llMySettingsUserGender.setOnClickListener { _ ->
            //菜单内容
            val menuList = ArrayList<SelectBean>()
            menuList.add(SelectBean(name = getString(R.string.boy)))
            menuList.add(SelectBean(name = getString(R.string.girl)))
            when (mMySettingsUserBean.sex) {
                1 -> {
                    menuList[1].checkState = true
                    menuList[0].checkState = false
                }
                2 -> {
                    menuList[0].checkState = false
                    menuList[1].checkState = true
                }
            }
            mBottomDialog = SelectDialog(this, menuList, onItemClick = {
                //设置内容
                tvMySettingsUserGender.text = menuList[it].name
                tvMySettingsUserGender.setTextColor(resources.getColor(R.color.color_333333))
                //1女2男
                when (it) {
                    0 -> {
                        //年龄赋值
                        mSex = 2
                        mMySettingsUserBean.sex = 2
                    }
                    1 -> {
                        mSex = 1
                        mMySettingsUserBean.sex = 1
                    }
                }
            })
            mBottomDialog.show()
        }

        //所在城市
        llMySettingsUserCity.setOnClickListener {
            mPopCity = PopCitySelect(this)
            mPopCity.showAtLocation(llMySettingsUser, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0)
            mPopCity.setOnCitySelectListener(object : PopCitySelect.CitySelectCallBack {
                override fun onCitySelectCallBack(city: AllCity) {
                    //选择完后给城市id赋值 文本赋值
                    mDistrictId = city.id.toInt()
                    tvMySettingsUserCity.text = city.name
                    tvMySettingsUserCity.setTextColor(resources.getColor(R.color.color_333333))
                }
            })
        }

        //创业资本
        llMySettingsUserMoney.setOnClickListener {
            //数据不为空就直接调打开弹框方法
            if (mMoneyList.isNotEmpty()) {
                openMoneyDialog(mMoneyList)
            } else {    //请求接口 加载静态数据
                if (mCanHttpLoad) {
                    getMoneyStaticHttp()
                }
            }
        }
        //感兴趣行业
        llMySettingsUserInterestIndustry.setOnClickListener {
            //数据不为空就直接调打开弹框方法
            if (mInterestList.isNotEmpty()) {
                openInterestDialog(mInterestList)
            } else {    //请求接口 加载静态数据
                if (mCanHttpLoad) {
                    getInterestStaticHttp()
                }
            }
        }
    }

    //个人信息接口请求
    private fun httpLoad() {
        myDialog.showLoadingDialog()
        ApiUtils.getApi()
                .mySettingsUser(MainApplication.instance.TOKEN)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    myDialog.dismissLoadingDialog()
                    it.apply {
                        if (code == 12000) {
                            llMySettingsUserTips.visibility = View.GONE
                            slMySettingsUser.visibility = View.VISIBLE
                            mMenu.text = getString(R.string.save)
                            mMySettingsUserBean = data as MySettingsUserBean
                            setData(data as MySettingsUserBean)
                        } else {
                            llMySettingsUserTips.visibility = View.VISIBLE
                        }
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                    llMySettingsUserTips.visibility = View.VISIBLE
                })
    }

    //修改个人信息接口
    private fun updateUserHttp() {
        myDialog.showLoadingDialog()
        ApiUtils.getApi()
                .updateMySettingsUser(mAvatar, mName, mSex, mPhone, mTelephone, mWx, mQQ, mEmail, mDistrictId, mCapitalId, mIndustryOfInterest, MainApplication.instance.user.wxUid, token = MainApplication.instance.TOKEN)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    myDialog.dismissLoadingDialog()
                    it.apply {
                        if (code == 12000) {
                            ToastUtil.showShort("修改成功")
                            //把本地图片地址返回给上一页
                            setResult(Activity.RESULT_OK, Intent().apply {
                                putExtra("mLocalPath", mLocalPath)
                            })
                            onBackPressed()
                        } else {
                            ToastUtil.showShort(msg)
                        }
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                })
    }

    //静态数据 创业资本接口请求
    private fun getMoneyStaticHttp() {
        mCanHttpLoad = false
        myDialog.showLoadingDialog()
        ApiUtils.getApi()
                .getMoneyStatic()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    mCanHttpLoad = true
                    myDialog.dismissLoadingDialog()
                    it.apply {
                        if (code == 12000) {
                            mMoneyList = data?.capitalList as ArrayList<SelectBean>
                            //打开弹框
                            openMoneyDialog(mMoneyList)
                        }
                    }
                }, {
                    mCanHttpLoad = true
                    myDialog.dismissLoadingDialog()
                })
    }

    //静态数据 感兴趣行业接口请求
    private fun getInterestStaticHttp() {
        mCanHttpLoad = false
        myDialog.showLoadingDialog()
        ApiUtils.getApi()
                .getInterestStatic()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    mCanHttpLoad = true
                    myDialog.dismissLoadingDialog()
                    it.apply {
                        if (code == 12000) {
                            mInterestList = data?.industry as ArrayList<SelectBean>
                            //打开弹框
                            openInterestDialog(mInterestList)
                        }
                    }
                }, {
                    mCanHttpLoad = true
                    myDialog.dismissLoadingDialog()
                })
    }

    //打开创业资本弹框
    private fun openMoneyDialog(moneyList: ArrayList<SelectBean>) {
        //先填充内容 加入已选择的资本
        moneyList.forEach {
            //如果id相等 就让它选中
            it.checkState = mMySettingsUserBean.capitalId == it.id
        }
        //全局变量赋值
        mMoneyList = moneyList
        //打开弹框
        mBottomDialog = SelectDialog(this, mMoneyList, onItemClick = {
            //创业资本id赋值
            mCapitalId = mMoneyList[it].id
            mMySettingsUserBean.capitalId = mMoneyList[it].id
            tvMySettingsUserMoney.text = mMoneyList[it].name
            tvMySettingsUserMoney.setTextColor(resources.getColor(R.color.color_333333))
        })
        mBottomDialog.show()
    }

    //打开感兴趣行业弹框
    private fun openInterestDialog(interestList: ArrayList<SelectBean>) {
        //先填充内容 加入已选择的行业
        val list: List<String> = mMySettingsUserBean.industryOfInterest.split(",")
        list.forEach {
            interestList.forEachIndexed { _, selectBean ->
                //如果相等就把interestList里的选中状态改变下
                if (it == "${selectBean.id}") {
                    selectBean.checkState = true
                }
            }
        }
        //全局变量赋值
        mInterestList = interestList
        //打开弹框
        mBottomDialog = SelectDialog(this, mInterestList, isDefaultLayout = false, onCancelClick = { _, resultList ->
            var str = ""
            resultList.forEach {
                if (it.checkState) {
                    str = str + it.id + ","
                }
            }
            if (str.isNotEmpty()) {
                str = str.substring(0, str.length - 1)
                tvMySettingsUserInterestIndustry.text = getString(R.string.my_settings_user_select_yes)
                tvMySettingsUserInterestIndustry.setTextColor(resources.getColor(R.color.color_333333))
            } else {
                mMySettingsUserBean.industryOfInterest = ""
                tvMySettingsUserInterestIndustry.text = getString(R.string.my_settings_user_select_no)
                tvMySettingsUserInterestIndustry.setTextColor(resources.getColor(R.color.color_999999))
            }
            //选择好后重新赋值
            mInterestList = resultList
            //改变感兴趣行业
            mIndustryOfInterest = str
        })
        mBottomDialog.show()
    }

    //接口请求前内容验证
    private fun contentVerification() {
        when {
            mName.isBlank() -> ToastUtil.showShort(getString(R.string.my_settings_user_name_tips))   //真实姓名
            mPhone.isBlank() -> ToastUtil.showShort(getString(R.string.phoneTips))  //手机号
            !InputCheckUtils.checkPhone(mPhone) -> ToastUtil.showShort(getString(R.string.phoneFormat_tips))   //手机号格式错误
            mDistrictId == 0 -> ToastUtil.showShort(getString(R.string.popCitySelect_twoTips))   //城市id
            mCapitalId == 0 -> ToastUtil.showShort(getString(R.string.my_settings_user_money_tips))   //创业资本
            mIndustryOfInterest.isBlank() -> ToastUtil.showShort(getString(R.string.my_settings_user_interestIndustry_tips)) //感兴趣行业
            !mTelephone.isBlank() && !InputCheckUtils.checkTel(mTelephone) -> ToastUtil.showShort(getString(R.string.my_settings_user_telephone_tips))
            !mWx.isBlank() && !InputCheckUtils.checkWechat(mWx) -> ToastUtil.showShort(getString(R.string.my_settings_user_wechat_tips))
            !mQQ.isBlank() && !InputCheckUtils.checkQQ(mQQ) -> ToastUtil.showShort(getString(R.string.my_settings_user_qq_tips))
            !mEmail.isBlank() && !InputCheckUtils.checkEmail(mEmail) -> ToastUtil.showShort(getString(R.string.my_settings_user_email_tips))
            mLocalPath != "" -> {
                myDialog.showLoadingDialog()
                //上传oss 返回http地址
                ApiUtils.updateImg(this, mLocalPath, callback = { newUrl, _ ->
                    mAvatar = if (newUrl != "") newUrl else mAvatar
                    //请求修改个人信息接口
                    updateUserHttp()
                })
            }
            else -> updateUserHttp()
        }
    }

    //设置数据
    private fun setData(mySettingsUserBean: MySettingsUserBean) {
        //头像
        GlideLoader.load(this, mySettingsUserBean.avatar, ivMySettingsUserHead, cacheType = CacheType.All, placeholder = R.drawable.default_img_icon)
        tvMySettingsUserUserName.text = mySettingsUserBean.userName
        etMySettingsName.setText(mySettingsUserBean.name)
        if (mySettingsUserBean.sex == 1) {
            tvMySettingsUserGender.text = getString(R.string.girl)
            tvMySettingsUserGender.setTextColor(resources.getColor(R.color.color_333333))
        } else if (mySettingsUserBean.sex == 2) {
            tvMySettingsUserGender.text = getString(R.string.boy)
            tvMySettingsUserGender.setTextColor(resources.getColor(R.color.color_333333))
        } else {
            tvMySettingsUserGender.text = getString(R.string.my_settings_user_select_no)
            tvMySettingsUserGender.setTextColor(resources.getColor(R.color.color_999999))
        }
        tvMySettingsPhone.text = mySettingsUserBean.phone
        etMySettingsUserTelephone.setText(mySettingsUserBean.telephone)
        etMySettingsUserWechat.setText(mySettingsUserBean.wx)
        etMySettingsUserQQ.setText(mySettingsUserBean.qq)
        etMySettingsUserEmail.setText(mySettingsUserBean.email)
        if (!mySettingsUserBean.cityName.isNullOrBlank()) {
            tvMySettingsUserCity.text = mySettingsUserBean.cityName
            tvMySettingsUserCity.setTextColor(resources.getColor(R.color.color_333333))
        } else {
            tvMySettingsUserCity.text = getString(R.string.my_settings_user_select_no)
            tvMySettingsUserCity.setTextColor(resources.getColor(R.color.color_999999))
        }
        if (!mySettingsUserBean.capital.isNullOrBlank()) {
            tvMySettingsUserMoney.text = mySettingsUserBean.capital
            tvMySettingsUserMoney.setTextColor(resources.getColor(R.color.color_333333))
        } else {
            tvMySettingsUserMoney.text = getString(R.string.my_settings_user_select_no)
            tvMySettingsUserMoney.setTextColor(resources.getColor(R.color.color_999999))
        }
        if (mySettingsUserBean.industryOfInterest.isNotEmpty()) {
            tvMySettingsUserInterestIndustry.text = getString(R.string.my_settings_user_select_yes)
            tvMySettingsUserInterestIndustry.setTextColor(resources.getColor(R.color.color_333333))
        } else {
            tvMySettingsUserInterestIndustry.text = getString(R.string.my_settings_user_select_no)
            tvMySettingsUserInterestIndustry.setTextColor(resources.getColor(R.color.color_999999))
        }
        //修改信息内容赋值
        mAvatar = mySettingsUserBean.avatar
        mName = mySettingsUserBean.name
        mSex = mySettingsUserBean.sex
        mPhone = mySettingsUserBean.phone
        mTelephone = mySettingsUserBean.telephone
        mWx = mySettingsUserBean.wx
        mQQ = mySettingsUserBean.qq
        mEmail = mySettingsUserBean.email
        mDistrictId = mySettingsUserBean.cityIds
        mCapitalId = mySettingsUserBean.capitalId
        mIndustryOfInterest = mySettingsUserBean.industryOfInterest
    }

    //权限申请结果
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //打开相机拍照上传服务器
    private fun openCameraAndUploadServer() {
        SimplePhotoUtil.instance.setConfig(PhotoConfig(this, true, onPathCallback = { path ->
            //用ImageView显示出来
            val bitmap = getLoacalBitmap(path)
            ivMySettingsUserHead.setImageBitmap(bitmap)
            //先把选好的本地路径保存下 当用户点击保存时再上传oss
            mLocalPath = path
        }).apply {
            isCuted = true  //是否剪裁
            cutHeight = 300 //剪裁宽高
            cutWidth = 300
        })
    }

    //打开相册读取文件上传服务器
    private fun openAlbumAndUploadServer() {
        SimplePhotoUtil.instance.setConfig(PhotoConfig(this, false, onPathCallback = { path ->
            val bitmap = getLoacalBitmap(path)
            ivMySettingsUserHead.setImageBitmap(bitmap)
            mLocalPath = path
        }).apply {
            isCuted = true
            cutHeight = 300
            cutWidth = 300
        })
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //选择照片（图库，拍照）
        SimplePhotoUtil.instance.onPhotoResult(requestCode, resultCode, data)
    }
}