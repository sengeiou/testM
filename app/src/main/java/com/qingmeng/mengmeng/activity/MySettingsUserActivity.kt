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
import com.qingmeng.mengmeng.entity.MySettingsUserBean
import com.qingmeng.mengmeng.entity.SelectDialogBean
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.utils.imageLoader.CacheType
import com.qingmeng.mengmeng.utils.imageLoader.GlideLoader
import com.qingmeng.mengmeng.utils.photo.InstallApp
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


/**
 *  Description :设置 - 修改用户信息

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/3
 */
class MySettingsUserActivity : BaseActivity() {
    private lateinit var mBottomDialog: SelectDialog                //自定义dialog
    private lateinit var mPopCity: PopCitySelect                    //城市选择pop
    private lateinit var mMySettingsUserBean: MySettingsUserBean    //个人信息bean

    override fun getLayoutId(): Int {
        return R.layout.activity_my_settings_user
    }

    override fun initObject() {
        super.initObject()

        setHeadName(getString(R.string.my_settings_user_title))

        httpLoad()
    }

    override fun initListener() {
        super.initListener()

        //返回
        mBack.setOnClickListener {
            this.finish()
        }

        //头像
        llMySettingsUserHead.setOnClickListener {
            //菜单内容
            val menuList = ArrayList<SelectDialogBean>()
            menuList.add(SelectDialogBean(getString(R.string.photograph)))
            menuList.add(SelectDialogBean(getString(R.string.albumSelect)))
            mBottomDialog = SelectDialog(this, menuList, onItemClick = {
                //拍照
                if (menuList[it].menu == getString(R.string.photograph)) {
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
            val menuList = ArrayList<SelectDialogBean>()
            menuList.add(SelectDialogBean(getString(R.string.boy)))
            menuList.add(SelectDialogBean(getString(R.string.girl)))
            mBottomDialog = SelectDialog(this, menuList, onItemClick = {
                //设置内容
                tvMySettingsUserGender.text = menuList[it].menu
            })
            mBottomDialog.show()
        }

        //所在城市
        llMySettingsUserCity.setOnClickListener {
            val list = arrayListOf("A", "AA", "B", "D", "E", "EE", "F", "G", "H", "K", "L", "M", "N", "W")
            mPopCity = PopCitySelect(this, list)
            mPopCity.showAtLocation(llMySettingsUser, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0)
        }

        //创业资本
        llMySettingsUserMoney.setOnClickListener {
            //菜单内容
            val menuList = ArrayList<SelectDialogBean>()
            menuList.add(SelectDialogBean(getString(R.string.money_3)))
            menuList.add(SelectDialogBean(getString(R.string.money_3_5)))
            menuList.add(SelectDialogBean(getString(R.string.money_5_10)))
            menuList.add(SelectDialogBean(getString(R.string.money_10_20)))
            menuList.add(SelectDialogBean(getString(R.string.money_20_50)))
            menuList.add(SelectDialogBean(getString(R.string.money_50)))
            mBottomDialog = SelectDialog(this, menuList, onItemClick = {
                ToastUtil.showShort(menuList[it].menu)
            })
            mBottomDialog.show()
        }

        //感兴趣行业
        llMySettingsUserInterestIndustry.setOnClickListener {
            //菜单内容
            val menuList = ArrayList<SelectDialogBean>()
            menuList.add(SelectDialogBean("游泳"))
            menuList.add(SelectDialogBean("钓鱼", true))
            menuList.add(SelectDialogBean("健身"))
            menuList.add(SelectDialogBean("拳击"))
            menuList.add(SelectDialogBean("滑雪"))
            menuList.add(SelectDialogBean("LOL", true))
            menuList.add(SelectDialogBean("自我"))
            menuList.add(SelectDialogBean("开发"))
            mBottomDialog = SelectDialog(this, menuList, isDefaultLayout = false, onOtherDismiss = {
                var str = ""
                it.forEachIndexed { index, selectDialogBean ->
                    str = str + selectDialogBean.checkState + "  "
                    //加个换行符
                    if (index == 2 || index == 5) {
                        str = str + "\n"
                    }
                }
                ToastUtil.showLong(str)
            })
            mBottomDialog.show()
        }
    }

    //接口请求
    private fun httpLoad() {
        ApiUtils.getApi()
                .mySettingsUser(TEST_ACCESS_TOKEN)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    if (it.code == 12000) {
                        mMySettingsUserBean = it.data as MySettingsUserBean
                        setData(it.data as MySettingsUserBean)
                    } else {

                    }
                }, {

                })
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

    //设置数据
    private fun setData(mySettingsUserBean: MySettingsUserBean) {
        //头像
        GlideLoader.load(this, mySettingsUserBean.avatar, ivMySettingsUserHead, cacheType = CacheType.All)
        tvMySettingsUserUserName.text = mySettingsUserBean.name
        if (mySettingsUserBean.sex == 1) {
            tvMySettingsUserGender.text = getString(R.string.boy)
        } else {
            tvMySettingsUserGender.text = getString(R.string.girl)
        }
        tvMySettingsPhone.text = mySettingsUserBean.phone
        tvMySettingsUserTelephone.text = mySettingsUserBean.telephone
        tvMySettingsUserWechat.text = mySettingsUserBean.wx
        tvMySettingsUserQQ.text = mySettingsUserBean.qq
        tvMySettingsUserEmail.text = mySettingsUserBean.email
        tvMySettingsUserCity.text = mySettingsUserBean.address
        tvMySettingsUserMoney.text = mySettingsUserBean.capital
        tvMySettingsUserInterestIndustry.text = mySettingsUserBean.industryOfInterest
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //选择照片（图库，拍照）
        SimplePhotoUtil.instance.onPhotoResult(requestCode, resultCode, data)
        if (requestCode == InstallApp.INSTALL_PERMISS_CODE) {
            InstallApp.installApk(this)
        }
    }
}