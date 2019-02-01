package com.qingmeng.mengmeng.view.dialog

/**
 * Created by fyf on 2019/1/18
 * 搜索结果筛选菜单加盟模式、投资金额PopWindow
 */
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.CommonAdapter
import com.qingmeng.mengmeng.entity.ConditionBean
import com.qingmeng.mengmeng.entity.SeachConBean
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.BoxUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_condition_pop_window.view.*

@SuppressLint("CheckResult")
class PopSeachCondition : PopupWindow {

    private var mTextMoneyList = ArrayList<ConditionBean>()
    private var mTextJoinTypeList = ArrayList<ConditionBean>()
    private var mActivity: Activity
    private var mMenuView: View
    private lateinit var mGridManager: GridLayoutManager
    private lateinit var mMoneyAdapter: CommonAdapter<ConditionBean>
    private lateinit var mJoinModelAdapter: CommonAdapter<ConditionBean>
    private lateinit var mSelectCallBack: SelectCallBack                 //回调

    constructor(mActivity: Activity) : super(mActivity) {
        this.mActivity = mActivity
        mMenuView = LayoutInflater.from(mActivity).inflate(R.layout.activity_condition_pop_window, null)
        initListener()
        initAdapter()
        getCacheData()
//        httpMoney()
//        httpJoinModel()
        this.contentView = mMenuView
        this.width = ViewGroup.LayoutParams.MATCH_PARENT
        this.height = ViewGroup.LayoutParams.WRAP_CONTENT
        this.animationStyle = R.style.TopPopWindow_animStyle
        this.setBackgroundDrawable(ColorDrawable(-0x00000000))


        mMenuView.bottom_condition_view.setOnClickListener {
            dismiss()
        }
    }

    private fun getCacheData() {
        Observable.create<SeachConBean> {
            val mMoneyList = BoxUtils.getJoinType()
            val mJoinTypeList = BoxUtils.getJoinType()
            if (!mTextMoneyList.isEmpty()) {
                BoxUtils.removeJoinType(mTextMoneyList)
                mTextMoneyList.clear()
            }
            if (!mTextJoinTypeList.isEmpty()) {
                BoxUtils.removeJoinType(mTextJoinTypeList)
                mTextJoinTypeList.clear()
            }
            mTextMoneyList.addAll(mMoneyList)
            mTextJoinTypeList.addAll(mJoinTypeList)
            var mSeachCondition = SeachConBean(ArrayList())
            if (!mTextMoneyList.isEmpty()) {
                mSeachCondition = SeachConBean.fromString(mTextMoneyList[1].id)
            }
            if (!mTextJoinTypeList.isEmpty()) {
                mSeachCondition = SeachConBean.fromString(mTextJoinTypeList[1].id)
            }
            it.onNext(mSeachCondition)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (!mTextMoneyList.isEmpty()) {
                        mMoneyAdapter.notifyDataSetChanged()
                    }
                    if (!mTextMoneyList.isEmpty()) {
                        mMoneyAdapter.notifyDataSetChanged()
                    }
                    httpMoney()
                    httpJoinModel()
                }, {
                    httpMoney()
                    httpJoinModel()
                })


    }

    private fun initListener() {
        mMenuView.search_condition_pop_button_CZ.setOnClickListener {
            dismiss()
        }
        mMenuView.search_condition_pop_button_QD.setOnClickListener {

        }
    }

    private fun initAdapter() {
        //投资金额
        mGridManager = GridLayoutManager(mActivity, 3)
        mMenuView.search_result_condition_recycler_money.layoutManager = mGridManager
        mMenuView.search_result_condition_recycler_money.isNestedScrollingEnabled = false
        mMoneyAdapter = CommonAdapter(mActivity, R.layout.red_shop_result_condition_in_item, mTextMoneyList, holderConvert = { holder, data, position, payloads ->
            holder.apply {
                if (mTextMoneyList.isNotEmpty()) {
                    mMenuView.search_result_condition_Money.visibility = View.VISIBLE
                } else {
                    mMenuView.search_result_condition_Money.visibility = View.GONE
                }
                setText(R.id.search_result_condition_in_button, data.name)
            }
        }, onItemClick = { view, holder, position ->

        })
        mMenuView.search_result_condition_recycler_money.adapter = mMoneyAdapter

        //投资模式
        mGridManager = GridLayoutManager(mActivity, 3)
        mMenuView.search_result_condition_recycler_joinType.layoutManager = mGridManager
        mMenuView.search_result_condition_recycler_joinType.isNestedScrollingEnabled = false
        mJoinModelAdapter = CommonAdapter(mActivity, R.layout.red_shop_result_condition_in_item, mTextJoinTypeList, holderConvert = { holder, data, position, payloads ->
            holder.apply {
                if (mTextJoinTypeList.isNotEmpty()) {
                    mMenuView.search_result_condition_joinType.visibility = View.VISIBLE
                } else {
                    mMenuView.search_result_condition_joinType.visibility = View.GONE
                }
                setText(R.id.search_result_condition_in_button, data.name)
            }
        }, onItemClick = { view, holder, position ->

        })
        mMenuView.search_result_condition_recycler_joinType.adapter = mJoinModelAdapter


    }

    //加盟金额接口
    private fun httpMoney() {
        ApiUtils.getApi()
                .getSeachConditionMoney()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    if (bean.code == 12000) {
                        bean.data?.let {
                            //如果数据不为空   再清缓存
                            if (!it.capitalList.isEmpty()) {
                                if (!mTextMoneyList.isEmpty()) {
                                    //清除缓存
                                    BoxUtils.removeJoinType(mTextMoneyList)
                                    mTextMoneyList.clear()
                                }
                                //it.setVersion()
                                mTextMoneyList.addAll(it.capitalList)
                                BoxUtils.saveJoinType(mTextMoneyList)
                                //加入缓存
                                mMoneyAdapter.notifyDataSetChanged()
                            }
                        }
                    } else if (bean.code != 12000) {
                        ToastUtil.showShort(bean.msg)
                    }
                }, {
                    ToastUtil.showNetError()
                })
    }

    //加盟模式接口
    private fun httpJoinModel() {
        ApiUtils.getApi()
                .getSeachConditionJoinModel()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    if (bean.code == 12000) {
                        bean.data?.let {
                            //如果数据不为空   再清缓存
                            if (!it.joinModes.isEmpty()) {
                                if (!mTextJoinTypeList.isEmpty()) {
                                    //清除缓存
                                    BoxUtils.removeJoinType(mTextJoinTypeList)
                                    mTextJoinTypeList.clear()
                                }
                                //  it.setVersion()
                                mTextJoinTypeList.addAll(it.joinModes)
                                BoxUtils.saveJoinType(mTextJoinTypeList)
                                mJoinModelAdapter.notifyDataSetChanged()
                                //加入缓存 未写
                            }
                        }
                    } else if (bean.code != 12000) {
                        ToastUtil.showShort(bean.msg)
                    }
                }, {
                    ToastUtil.showNetError()
                })
    }


    override fun showAsDropDown(anchor: View, xoff: Int, yoff: Int, gravity: Int) {
//        backgroundAlphaExt(0.5f)
        mMenuView.bottom_condition_view.visibility = View.VISIBLE
        //解决7.0showAsDropDown  失效
        if (Build.VERSION.SDK_INT == 24) {
            var rect = Rect()
            anchor.getGlobalVisibleRect(rect)
            var h = anchor.resources.displayMetrics.heightPixels - rect.bottom
            height = h
        }
        super.showAsDropDown(anchor, xoff, yoff, gravity)

    }

    //回调方法
    fun setOnSelectListener(selectListener: SelectCallBack) {
        mSelectCallBack = selectListener
    }

    interface SelectCallBack {
        fun onSelectCallBack(select: IntArray)
    }

    override fun dismiss() {
        super.dismiss()
//        backgroundAlphaExt(1f)
        mMenuView.bottom_condition_view.visibility = View.GONE
    }


}