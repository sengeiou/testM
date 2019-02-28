package com.qingmeng.mengmeng.view.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.CommonAdapter
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.entity.*
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.BoxUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_common_pop_window.view.*

/**
 * Created by fyf on 2019/1/16
 * 搜索结果筛选菜单PopWindow
 */
@SuppressLint("CheckResult")
class PopSeachSelect//设置宽高popWindow  动画 背景
//点击popwindow 外部消失
//1 为餐饮类型  2为加盟区域 3 为综合排序
(private var mActivity: Activity, type: Int, mfatherId: Int) : PopupWindow(mActivity) {

    private var mMenuView: View
    private lateinit var mLauyoutManger: LinearLayoutManager
    private lateinit var mGridManager: GridLayoutManager
    private lateinit var mProvinceAdapter: CommonAdapter<FatherDto>      //省适配
    private lateinit var mCityAdapter: CommonAdapter<CityFilter>         //市适配
    private var mProvinceList = ArrayList<FatherDto>()                   //省数据
    private var mCityList = ArrayList<CityFilter>()                      //市数据
    private lateinit var mSelectCallBack: SelectCallBack                 //回调
    private var mRankingList = ArrayList<StaticBean>()                   //综合排序数据
    private lateinit var mRankingAdapter: CommonAdapter<StaticBean>      //综合排序适配
    private lateinit var mFoodTypeAdapter: CommonAdapter<FoodType>        //餐饮左类型适配
    private lateinit var mFoodAdapter: CommonAdapter<FoodTypeDto>             //餐饮右适配
    private var mFoodTypeList = ArrayList<FoodType>()                      //餐饮左数据
    private var mFoodList = ArrayList<FoodTypeDto>()                           //餐饮右数据
    private var mFathId = 0

    init {
        mMenuView = LayoutInflater.from(mActivity).inflate(R.layout.activity_common_pop_window, null)
        contentView = mMenuView
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        animationStyle = R.style.TopPopWindow_animStyle
        setBackgroundDrawable(ColorDrawable(-0x00000000))
        mMenuView.bottom_view.setOnClickListener {
            dismiss()
        }
        if (mfatherId > 0) {
            mFathId = mfatherId
        } else {
            mFathId = 1
        }
        when (type) {
            1 -> getFoodTypeCache()
            2 -> getJoinAreaCache()
            3 -> getRankingCache()
        }
        initLeftAdapter(type)
        initRightAdapter(type)
    }

    //加载   左边适配
    private fun initLeftAdapter(type: Int) {
        mLauyoutManger = LinearLayoutManager(mActivity)
        mMenuView.left_recyclerView_pop.layoutManager = mLauyoutManger
        if (type == 1) {
            mFoodTypeAdapter = CommonAdapter(mActivity, R.layout.seach_result_left_item, mFoodTypeList, holderConvert = { holder, data, _, _ ->
                holder.apply {
                    if (data.checkState) {
                        getView<ImageView>(R.id.search_result_pop_left_arrows).visibility = View.VISIBLE
                    } else {
                        getView<ImageView>(R.id.search_result_pop_left_arrows).visibility = View.GONE
                    }
                    setText(R.id.search_result_pop_left_item, data.name)
                }
            }, onItemClick = { _, _, position ->
                mFoodTypeList.forEach {
                    it.checkState = false
                }
                mFoodTypeList[position].checkState = true
                mFoodTypeAdapter.notifyDataSetChanged()
                mFoodList.clear()
                mFoodList.addAll(mFoodTypeList[position].foodTypeDto)
                mFoodAdapter.notifyDataSetChanged()
            })
            mMenuView.left_recyclerView_pop.adapter = mFoodTypeAdapter
        } else if (type == 2) {
            mProvinceAdapter = CommonAdapter(mActivity, R.layout.seach_result_left_item, mProvinceList, holderConvert = { holder, data, _, _ ->
                holder.apply {
                    if (data.checkState) {
                        getView<ImageView>(R.id.search_result_pop_left_arrows).visibility = View.VISIBLE
                    } else {
                        getView<ImageView>(R.id.search_result_pop_left_arrows).visibility = View.GONE
                    }
                    setText(R.id.search_result_pop_left_item, data.name)
                }
            }, onItemClick = { _, _, position ->
                mProvinceList.forEach {
                    it.checkState = false
                }
                mProvinceList[position].checkState = true
                mProvinceAdapter.notifyDataSetChanged()
                mCityList.clear()
                mCityList.add(CityFilter(mProvinceList[position].id, mProvinceList[position].id, 0, 2, "全部"))
                mCityList.addAll(mProvinceList[position].cityFilter)
                mCityAdapter.notifyDataSetChanged()
            })
            mMenuView.left_recyclerView_pop.adapter = mProvinceAdapter
        } else if (type == 3) {
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            mRankingAdapter = CommonAdapter(mActivity, R.layout.seach_result_left_item, mRankingList, holderConvert = { holder, data, _, _ ->
                holder.apply {
                    getView<LinearLayout>(R.id.seach_ranking_linear).apply {
                        if (data.checkState) {
                            getView<TextView>(R.id.search_result_pop_left_item).setTextColor(resources.getColor(R.color.color_5ab1e1))
                        } else {
                            getView<TextView>(R.id.search_result_pop_left_item).setTextColor(resources.getColor(R.color.black))
                        }
                        setText(R.id.search_result_pop_left_item, data.title)
                    }
                }
            }, onItemClick = { _, _, position ->
                mRankingList.forEach {
                    it.checkState = false
                }
                mRankingList[position].checkState = true
                mRankingAdapter.notifyDataSetChanged()
                mSelectCallBack.onSelectCallBack(position + 1, 0, mRankingList[position].title)
                dismiss()
            })
            mMenuView.left_recyclerView_pop.adapter = mRankingAdapter
        }
    }

    private fun initRightAdapter(type: Int) {
        if (type == 1) {
            mGridManager = GridLayoutManager(mActivity, 3)
            mMenuView.right_recyclerView_pop.layoutManager = mGridManager
            mMenuView.right_recyclerView_pop.isNestedScrollingEnabled = false
            mFoodAdapter = CommonAdapter(mActivity, R.layout.seach_food_type_right_in_item, mFoodList, holderConvert = { holder, data, _, _ ->
                holder.apply {
                    mMenuView.seach_result_right_text_type.visibility = View.VISIBLE
                    mMenuView.seach_result_right_text_type.setOnClickListener {
                        if (mFoodList.isEmpty()) {
                            mSelectCallBack.onSelectCallBack(0, 0, "全部")
                            dismiss()
                        } else {
                            var mFatherName = ""
                            mFoodTypeList.forEach {
                                if (it.id == data.fahterId) {
                                    mFatherName = it.name
                                }
                            }
                            mSelectCallBack.onSelectCallBack(data.fahterId, 0, mFatherName)
                            dismiss()
                        }
                    }
                    Glide.with(mActivity).load(data.logo).apply(RequestOptions()
                            .placeholder(R.drawable.default_img_icon).error(R.drawable.default_img_icon)).into(getView(R.id.Seach_food_type_right_inImageView))
                    setText(R.id.Seach_food_type_right_inContent, data.name)
                }
            }, onItemClick = { _, _, position ->
                mSelectCallBack.onSelectCallBack(mFoodList[position].id, mFoodList[position].fahterId, mFoodList[position].name)
                dismiss()
            })
            mMenuView.right_recyclerView_pop.adapter = mFoodAdapter
        } else if (type == 2) {
            mLauyoutManger = LinearLayoutManager(mActivity)
            mMenuView.right_recyclerView_pop.layoutManager = mLauyoutManger
            mMenuView.right_recyclerView_pop.isNestedScrollingEnabled = false
            mCityAdapter = CommonAdapter(mActivity, R.layout.seach_result_right_item, mCityList, holderConvert = { holder, data, _, _ ->
                holder.apply {
                    setText(R.id.search_result_pop_right_item, data.name)
                }
            }, onItemClick = { _, _, position ->
                var mFatherProvinceName = String()
                if (position == 0) {
                    mProvinceList.forEach {
                        if (it.id == mCityList[position].fatherId) {
                            mFatherProvinceName = it.name
                        }
                    }
                    mSelectCallBack.onSelectCallBack(mCityList[position].fatherId, mCityList[position].fatherId, mFatherProvinceName)
                } else {
                    mSelectCallBack.onSelectCallBack(mCityList[position].id, mCityList[position].fatherId, mCityList[position].name)
                }
                dismiss()
            })
            mMenuView.right_recyclerView_pop.adapter = mCityAdapter
        }
    }

    //加盟区域接口
    private fun httpSeachJoinArea(version: String) {
        ApiUtils.getApi()
                .getSeachJoinArea(version)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    if (bean.code == 12000) {
                        bean.data?.let {
                            //如果数据不为空   再清缓存
                            if (!it.fatherDtos.isEmpty()) {
                                if (!mProvinceList.isEmpty()) {
                                    //清除缓存
                                    BoxUtils.removeCache(IConstants.SEACH_RESULT_AREA)
                                    mProvinceList.clear()
                                }
                                mProvinceList.add(FatherDto(ArrayList<CityFilter>(), 0, 0, 0, 0, "全国", "1"))
                                mProvinceList.addAll(it.fatherDtos)
                                if (!mProvinceList.isEmpty()) {
                                    mProvinceList[1].checkState = true
                                }
                                if (!mCityList.isEmpty()) {
                                    mCityList.clear()
                                }
                                mCityList.add(CityFilter(it.fatherDtos[0].id, it.fatherDtos[0].id, 0, 2, "全部"))
                                mCityList.addAll(it.fatherDtos[0].cityFilter)
                                BoxUtils.saveCache(it, IConstants.SEACH_RESULT_AREA)
                                mProvinceAdapter.notifyDataSetChanged()
                                mCityAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }, {
                    ToastUtil.showNetError()
                }, {}, {})
    }

    //加盟区域缓存
    private fun getJoinAreaCache() {
        Observable.create<SeachJoinAreaBean> {
            val joinAreaData = BoxUtils.getCache<SeachJoinAreaBean>(IConstants.SEACH_RESULT_AREA)
            if (!mProvinceList.isEmpty()) {
                mProvinceList.clear()
            }
            if (!mCityList.isEmpty()) {
                mCityList.clear()
            }
            mProvinceList.addAll(joinAreaData.fatherDtos)
            mCityList.addAll(joinAreaData.fatherDtos[0].cityFilter)
            it.onNext(joinAreaData)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (!mProvinceList.isEmpty()) {
                        mProvinceAdapter.notifyDataSetChanged()
                    }
                    if (!mCityList.isEmpty()) {
                        mCityAdapter.notifyDataSetChanged()
                    }
                    httpSeachJoinArea(getVersion(1))
                }, {
                    httpSeachJoinArea(getVersion(1))
                }, {}, {})
    }

    // 获取筛选栏餐饮类型接口
    private fun httpFoodType(version: String) {
        ApiUtils.getApi()
                .getSeachFoodType(version)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    if (bean.code == 12000) {
                        bean.data?.let {
                            //如果数据不为空   再清缓存
                            if (!it.foodType.isEmpty()) {
                                if (!mFoodTypeList.isEmpty()) {
                                    //缓存清空
                                    BoxUtils.removeCache(IConstants.SEACH_RESULT_FOOD)
                                    mFoodTypeList.clear()
                                }
                                mFoodTypeList.add(FoodType(ArrayList(), 0, "", 0, "", "全部", "1"))
                                mFoodTypeList.addAll(it.foodType)
                                if (!mFoodList.isEmpty()) {
                                    mFoodList.clear()
                                }
                                if (!mFoodTypeList.isEmpty()) {
                                    //打开直接选中所在分组 mFathId
                                    mFoodTypeList.forEach {
                                        if (it.id == mFathId) {
                                            it.checkState = true
                                            //打开直接选中所在分组 mFathId
                                            mFoodList.addAll(it.foodTypeDto)
                                            val index = mFoodTypeList.indexOfFirst { it.id == mFathId }
                                            mMenuView.left_recyclerView_pop.scrollToPosition(index)
                                        }
                                    }
                                }
                                //加入缓存
                                // mFoodList.add(FoodTypeDto(it.foodType[0].id, "", 0, logoUrl, "全部"))
                                BoxUtils.saveCache(it, IConstants.SEACH_RESULT_FOOD)
                                mFoodTypeAdapter.notifyDataSetChanged()
                                mFoodAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }, {
                    ToastUtil.showNetError()
                }, {}, {})
    }

    //餐饮类型缓存
    private fun getFoodTypeCache() {
        Observable.create<SeachFoodTypeBean> {
            val foodTypeData = BoxUtils.getCache<SeachFoodTypeBean>(IConstants.SEACH_RESULT_FOOD)
            if (!mFoodTypeList.isEmpty()) {
                mFoodTypeList.clear()
            }
            if (!mFoodList.isEmpty()) {
                mFoodList.clear()
            }
            mFoodTypeList.add(FoodType(ArrayList(), 0, "", 0, "", "全部", "1"))
            mFoodTypeList.addAll(foodTypeData.foodType)
            mFoodTypeList.forEach {
                it.checkState = false
                if (it.id == mFathId) {
                    it.checkState = true
                    //打开直接选中所在分组 mFathId
                    mFoodList.addAll(it.foodTypeDto)
                    val index = mFoodTypeList.indexOfFirst { it.id == mFathId }
                    mMenuView.left_recyclerView_pop.scrollToPosition(index)
                }
            }
            it.onNext(foodTypeData)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (!mFoodTypeList.isEmpty()) {
                        mFoodTypeAdapter.notifyDataSetChanged()
                    }
                    if (!mFoodList.isEmpty()) {
                        mFoodAdapter.notifyDataSetChanged()
                    }
                    httpFoodType(getVersion(0))
                }, {
                    httpFoodType(getVersion(0))
                }, {}, {})
    }

    /**
     * 获取静态数据  筛选栏综合排序
     * @param type 类型：1.首页banner8个icon 2.首页列表模块 3.列表筛选标题 4.综合排序 5.反馈类型
     */
    private fun httpRangking(version: String, type: Int) {
        ApiUtils.getApi()
                .getStaticInfo(version, type)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    if (bean.code == 12000) {
                        bean.data?.let {
                            if (!it.systemStatic.isEmpty()) {
                                if (!mRankingList.isEmpty()) {
                                    //清除缓存
                                    BoxUtils.removeStatic(mRankingList)
                                    mRankingList.clear()
                                }
                                it.setVersion()
                                mRankingList.addAll(it.systemStatic)
                                BoxUtils.saveStatic(mRankingList)
                                mRankingAdapter.notifyDataSetChanged()
                                //存入缓存
                            }
                        }
                    }
                }, {
                    ToastUtil.showNetError()
                }, {}, {})
    }

    //综合排序缓存
    private fun getRankingCache() {
        Observable.create<SeachResultBean> {
            val rankData = BoxUtils.getStaticByType(4)
            if (!mRankingList.isEmpty()) {
                BoxUtils.removeStatic(mRankingList)
                mRankingList.clear()
            }
            mRankingList.addAll(rankData)
            var mSeachResult = SeachResultBean(ArrayList())
            if (!mRankingList.isEmpty()) {
                mSeachResult = SeachResultBean.fromString(mRankingList[1].id)
            }
            it.onNext(mSeachResult)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (!mRankingList.isEmpty()) {
                        mRankingAdapter.notifyDataSetChanged()
                    }
                    httpRangking(getVersion(2), 4)
                }, {
                    httpRangking(getVersion(2), 4)
                }, {}, {})
    }

    override fun showAsDropDown(anchor: View, xoff: Int, yoff: Int, gravity: Int) {
        //背景显示
        mMenuView.bottom_view.visibility = View.VISIBLE
        //解决7.0 showAsDropDowm  无效果适配
        if (Build.VERSION.SDK_INT == 24) {
            var rect = Rect()
            anchor.getGlobalVisibleRect(rect)
            var heigt = anchor.resources.displayMetrics.heightPixels - rect.bottom
            height = heigt
        }
        super.showAsDropDown(anchor, xoff, yoff, gravity)
    }

    override fun dismiss() {
        super.dismiss()
        //背景变回
        mMenuView.bottom_view.visibility = View.GONE
    }

    /**
     * @param type 0:餐饮类型 1:加盟区域 2:综合排序
     **/
    private fun getVersion(type: Int): String {
        return when {
            type == 0 && !mFoodTypeList.isEmpty() -> mFoodTypeList[0].version
            type == 1 && !mProvinceList.isEmpty() -> mProvinceList[0].version
            type == 2 && !mRankingList.isEmpty() -> mRankingList[0].version
            else -> ""
        }
    }


    //回调方法
    fun setOnSelectListener(selectListener: SelectCallBack) {
        mSelectCallBack = selectListener
    }

    interface SelectCallBack {
        fun onSelectCallBack(selectId: Int, selectFatherId: Int, selectName: String)
    }
}