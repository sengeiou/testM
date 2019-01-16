package com.qingmeng.mengmeng.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.Gravity
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.constant.IConstants.RESULT_CODE_OPEN_ALBUM
import com.qingmeng.mengmeng.constant.IConstants.RESULT_CODE_TAKE_CAMERA
import com.qingmeng.mengmeng.constant.IConstants.TEST_ACCESS_TOKEN
import com.qingmeng.mengmeng.entity.AllCity
import com.qingmeng.mengmeng.entity.MySettingsUserBean
import com.qingmeng.mengmeng.entity.SelectBean
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.ToastUtil
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
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.regex.Pattern


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
    private var mCanHttpLoad = true                            //是否可以再次请求
    //一些必填的内容
    private var mName: String? = null                //真实姓名
    private var mPhone: String? = null               //手机号
    private var mDistrictId: Int? = null             //县id
    private var mCapitalId: Int? = null              //创业资本
    private var mIndustryOfInterest: String? = null  //感兴趣行业
    //一些选填的内容
    private var mAvatar: String? = null              //头像地址
    private var mSex: Int? = null                    //年龄
    private var mTelephone: String? = null           //固定电话
    private var mWx: String? = null                  //微信
    private var mQQ: String? = null                  //qq
    private var mEmail: String? = null               //邮箱

    companion object {
        var mAllCityList = ArrayList<AllCity>()      //所有城市
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_my_settings_user
    }

    override fun initObject() {
        super.initObject()

        setHeadName(getString(R.string.my_settings_user_title))
        mMenu.text = getString(R.string.save)

        httpLoad()
    }

    override fun initListener() {
        super.initListener()

        //返回
        mBack.setOnClickListener {
            this.finish()
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

        //头像
        llMySettingsUserHead.setOnClickListener {
            //菜单内容
            val menuList = ArrayList<SelectBean>()
            menuList.add(SelectBean(name = getString(R.string.photograph)))
            menuList.add(SelectBean(name = getString(R.string.albumSelect)))
            mBottomDialog = SelectDialog(this, menuList, onItemClick = {
                //拍照
                if (menuList[it].name == getString(R.string.photograph)) {
                    //判断是否有权限
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        //打开相机方法
                        openCameraAndUploadServer()
                    } else {
                        //申请权限
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), RESULT_CODE_TAKE_CAMERA)
                    }
                } else {  //相册选择
                    //判断是否有权限
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        //打开相册方法
                        openAlbumAndUploadServer()
                    } else {
                        //申请权限
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), RESULT_CODE_OPEN_ALBUM)
                    }
                }
            })
            mBottomDialog.show()
        }

        //性别
        llMySettingsUserGender.setOnClickListener {
            //菜单内容
            val menuList = ArrayList<SelectBean>()
            menuList.add(SelectBean(name = getString(R.string.boy)))
            menuList.add(SelectBean(name = getString(R.string.girl)))
            when (mMySettingsUserBean.sex) {
                1 -> {
                    menuList[0].checkState = true
                    menuList[1].checkState = false
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
                //年龄赋值
                mSex = it + 1
                mMySettingsUserBean.sex = it + 1
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
        ApiUtils.getApi()
                .mySettingsUser(TEST_ACCESS_TOKEN)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    it.apply {
                        if (code == 12000) {
                            mMySettingsUserBean = data as MySettingsUserBean
                            setData(data as MySettingsUserBean)
                        } else {

                        }
                    }
                }, {

                })
    }

    //修改个人信息接口
    private fun updateUserHttp() {
//        mAvatar?.let { if (it != mMySettingsUserBean.avatar) it else null },
//        mName?.let { if (it != mMySettingsUserBean.name) it else null },
//        mSex?.let { if (it != mMySettingsUserBean.sex) it else null },
//        mPhone?.let { if (it != mMySettingsUserBean.phone) it else null },
//        mTelephone?.let { if (it != mMySettingsUserBean.telephone) it else null },
//        mWx?.let { if (it != mMySettingsUserBean.wx) it else null },
//        mQQ?.let { if (it != mMySettingsUserBean.qq) it else null },
//        mEmail?.let { if (it != mMySettingsUserBean.email) it else null },
//        mDistrictId?.let { if (it != mMySettingsUserBean.cityIds) it else null },
//        mCapitalId?.let { if (it != mMySettingsUserBean.capitalId) it else null },
        ApiUtils.getApi()
                .updateMySettingsUser(mAvatar, mName, mSex, mPhone, mTelephone, mWx, mQQ, mEmail, mDistrictId, mCapitalId, mIndustryOfInterest, token = TEST_ACCESS_TOKEN)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    it.apply {
                        if (code == 12000) {
                            ToastUtil.showShort("修改成功")
                            this@MySettingsUserActivity.finish()
                        }
                    }
                }, {

                })
    }

    //静态数据 创业资本接口请求
    private fun getMoneyStaticHttp() {
        mCanHttpLoad = false
        ApiUtils.getApi()
                .getMoneyStatic()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    mCanHttpLoad = true
                    it.apply {
                        if (code == 12000) {
                            mMoneyList = data?.capitalList as ArrayList<SelectBean>
                            //打开弹框
                            openMoneyDialog(mMoneyList)
                        }
                    }
                }, {
                    mCanHttpLoad = true
                })
    }

    //静态数据 感兴趣行业接口请求
    private fun getInterestStaticHttp() {
        mCanHttpLoad = false
        ApiUtils.getApi()
                .getInterestStatic()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    mCanHttpLoad = true
                    it.apply {
                        if (code == 12000) {
                            mInterestList = data?.industry as ArrayList<SelectBean>
                            //打开弹框
                            openInterestDialog(mInterestList)
                        }
                    }
                }, {
                    mCanHttpLoad = true
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
        list?.forEach {
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
        mBottomDialog = SelectDialog(this, mInterestList, isDefaultLayout = false, onCalcelClick = { _, list ->
            var str = ""
            list.forEach {
                if (it.checkState) {
                    str = str + it.id + ","
                }
            }
            if (str.isNotEmpty()) {
                str = str.substring(0, str.length - 1)
                tvMySettingsUserInterestIndustry.text = getString(R.string.my_settings_user_select_yes)
                tvMySettingsUserInterestIndustry.setTextColor(resources.getColor(R.color.color_333333))
            } else {
                tvMySettingsUserInterestIndustry.text = getString(R.string.my_settings_user_select_no)
                tvMySettingsUserInterestIndustry.setTextColor(resources.getColor(R.color.color_999999))
            }
            //选择好后重新赋值
            mInterestList = list
            //改变感兴趣行业
            mIndustryOfInterest = str
        })
        mBottomDialog.show()
    }

    //接口请求前内容验证
    private fun contentVerification() {
        if (!mName.isNullOrBlank()) {   //真实姓名
            if (!mPhone.isNullOrBlank() && checkTel(mPhone!!)) {    // 手机号
                if (mDistrictId != null && mDistrictId != 0) {  //城市id
                    if (mCapitalId != null && mCapitalId != 0) {    //创业资本
                        if (!mIndustryOfInterest.isNullOrBlank()) { //感兴趣行业
                            //一路过关斩将 终于过来了！(缓一缓还有第二轮！)
                            //如果这些选填的内容不为空的话 那么就判断它们格式正不正确 然后请求接口
                            if (!mTelephone.isNullOrBlank()) {
                                ToastUtil.showShort(getString(R.string.my_settings_user_telephone_tips))
                                return
                            }
                            if (!mWx.isNullOrBlank()) {
                                ToastUtil.showShort(getString(R.string.my_settings_user_wechat_tips))
                                return
                            }
                            if (!mQQ.isNullOrBlank()) {
                                ToastUtil.showShort(getString(R.string.my_settings_user_qq_tips))
                                return
                            }
                            if (!mEmail.isNullOrBlank()) {
                                ToastUtil.showShort(getString(R.string.my_settings_user_email_tips))
                                return
                            }
                            //这。。。比过五关斩六将还牛批啊
                            //请求修改个人信息接口
                            updateUserHttp()
                        } else {
                            ToastUtil.showShort(getString(R.string.my_settings_user_interestIndustry_tips))
                        }
                    } else {
                        ToastUtil.showShort(getString(R.string.my_settings_user_money_tips))
                    }
                } else {
                    ToastUtil.showShort(getString(R.string.popCitySelect_twoTips))
                }
            } else {
                if (mPhone.isNullOrBlank()) {   //空
                    ToastUtil.showShort(getString(R.string.phoneTips))
                } else {    //格式错误
                    ToastUtil.showShort(getString(R.string.phoneFormat_tips))
                }
            }
        } else {
            ToastUtil.showShort(getString(R.string.my_settings_user_name_tips))
        }
    }

    //正则验证手机号
    private fun checkTel(tel: String): Boolean {
        if (tel.isNullOrBlank()) {
            return false
        } else {
            val pattern = Pattern.compile("^[1][3,4,5,7,8][0-9]{9}$")
            val matcher = pattern.matcher(tel)
            return matcher.matches()
        }
    }

    //设置数据
    private fun setData(mySettingsUserBean: MySettingsUserBean) {
        //头像
        GlideLoader.load(this, mySettingsUserBean.avatar, ivMySettingsUserHead, cacheType = CacheType.All)
        tvMySettingsUserUserName.text = mySettingsUserBean.userName
        etMySettingsName.setText(mySettingsUserBean.name)
        if (mySettingsUserBean.sex == 1) {
            tvMySettingsUserGender.text = getString(R.string.boy)
            tvMySettingsUserGender.setTextColor(resources.getColor(R.color.color_333333))
        } else if (mySettingsUserBean.sex == 2) {
            tvMySettingsUserGender.text = getString(R.string.girl)
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
        if (!mySettingsUserBean.address.isNullOrBlank()) {
            tvMySettingsUserCity.text = mySettingsUserBean.address
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

        val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        when (requestCode) {
            RESULT_CODE_TAKE_CAMERA -> {
                if (cameraAccepted) {
                    //授权成功
                    openCameraAndUploadServer()
                } else {
                    //用户拒绝
                    ToastUtil.showShort(getString(R.string.permission_takeCamera))
                }
            }
            RESULT_CODE_OPEN_ALBUM -> {
                if (cameraAccepted) {
                    openAlbumAndUploadServer()
                } else {
                    ToastUtil.showShort(getString(R.string.permission_openAlbum))
                }
            }
        }
    }

    //打开相机拍照上传服务器
    private fun openCameraAndUploadServer() {
        SimplePhotoUtil.instance.setConfig(PhotoConfig(this, true, onPathCallback = { path ->
            //用ImageButton显示出来
            val bitmap = getLoacalBitmap(path)
            ivMySettingsUserHead.setImageBitmap(bitmap)
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
        }).apply {
            isCuted = true
            cutHeight = 300
            cutWidth = 300
        })
    }

    /**
     * 加载本地图片
     * @param url
     * @return
     */
    private fun getLoacalBitmap(url: String): Bitmap? {
        try {
            val fis = FileInputStream(url)
            return BitmapFactory.decodeStream(fis)  ///把流转化为Bitmap图片
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return null
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        mAllCityList.clear()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //选择照片（图库，拍照）
        SimplePhotoUtil.instance.onPhotoResult(requestCode, resultCode, data)
    }
}