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
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.CommonAdapter
import com.qingmeng.mengmeng.entity.ConditionBean
import com.qingmeng.mengmeng.entity.ConditionMoneyBean
import com.qingmeng.mengmeng.entity.SeachResultBean
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.BoxUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_condition_pop_window.view.*

@SuppressLint("CheckResult")
class PopSeachCondition : PopupWindow {

    private var mTextMoneyList = ArrayList<ConditionMoneyBean>()
    private var mTextJoinTypeList = ArrayList<ConditionBean>()
    private var mActivity: Activity
    private var mMenuView: View
    private lateinit var mGridManager: GridLayoutManager
    private lateinit var mMoneyAdapter: CommonAdapter<ConditionMoneyBean>
    private lateinit var mJoinModelAdapter: CommonAdapter<ConditionBean>
    private lateinit var mSelectCallBack: SelectCallBack                 //回调
    private var checkedMoneyData = StringBuffer()
    private var checkedTypeData = StringBuffer()

    constructor(mActivity: Activity) : super(mActivity) {
        this.mActivity = mActivity
        mMenuView = LayoutInflater.from(mActivity).inflate(R.layout.activity_condition_pop_window, null)
        this.contentView = mMenuView
        this.width = ViewGroup.LayoutParams.MATCH_PARENT
        this.height = ViewGroup.LayoutParams.WRAP_CONTENT
        this.animationStyle = R.style.TopPopWindow_animStyle
        this.setBackgroundDrawable(ColorDrawable(-0x00000000))
        mMenuView.search_result_condition_joinType.visibility = View.VISIBLE
        mMenuView.search_result_condition_Money.visibility = View.VISIBLE
        mMenuView.bottom_condition_view.setOnClickListener {
            dismiss()
        }
        getCacheData()
        initAdapter()
        initListener()
    }

    private fun getCacheData() {
        Observable.create<SeachResultBean> {
            val mMoneyList = BoxUtils.getMoneyType()
            val mJoinTypeList = BoxUtils.getJoinType()
            if (!mTextMoneyList.isEmpty()) {
                mTextMoneyList.clear()
            }
            if (!mTextJoinTypeList.isEmpty()) {
                mTextJoinTypeList.clear()
            }
            mTextMoneyList.addAll(mMoneyList)
            mTextJoinTypeList.addAll(mJoinTypeList)
            var mSeachCondition = SeachResultBean(ArrayList())
            if (!mTextMoneyList.isEmpty()) {
                mSeachCondition = SeachResultBean.fromString(mTextMoneyList[0].id)
            }
            if (!mTextJoinTypeList.isEmpty()) {
                mSeachCondition = SeachResultBean.fromString(mTextJoinTypeList[0].id)
            }
            it.onNext(mSeachCondition)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (!mTextMoneyList.isEmpty()) {
                        mMoneyAdapter.notifyDataSetChanged()
                    }
                    if (!mTextJoinTypeList.isEmpty()) {
                        mJoinModelAdapter.notifyDataSetChanged()
                    }
                    httpMoney()
                    httpJoinModel()
                }, {
                    httpMoney()
                    httpJoinModel()
                }, {}, {})
    }

    @SuppressLint("ResourceAsColor")
    private fun initListener() {
        mMenuView.search_condition_pop_button_CZ.setOnClickListener {
            //所有选中状态取消
            mTextMoneyList.forEach {
                it.checkState = false
                mMoneyAdapter.notifyDataSetChanged()
            }
            mTextJoinTypeList.forEach {
                it.checkState = false
                mJoinModelAdapter.notifyDataSetChanged()
            }
        }
        //确定  回调接口数据
        mMenuView.search_condition_pop_button_QD.setOnClickListener {
            checkedMoneyData.delete(0, checkedMoneyData.length)
            checkedTypeData.delete(0, checkedTypeData.length)
            mTextMoneyList.forEach {
                if (it.checkState) {
                    checkedMoneyData.append("${it.id},")
                }
                mMoneyAdapter.notifyDataSetChanged()
            }
            mTextJoinTypeList.forEach {
                if (it.checkState) {
                    checkedTypeData.append("${it.id},")
                }
                mJoinModelAdapter.notifyDataSetChanged()
            }
            mSelectCallBack.onSelectCallBack(checkedMoneyData, checkedTypeData)
            dismiss()
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun initAdapter() {
        //投资金额
        mGridManager = GridLayoutManager(mActivity, 3)
        mMenuView.search_result_condition_recycler_money.layoutManager = mGridManager
        mMenuView.search_result_condition_recycler_money.isNestedScrollingEnabled = false
        mMoneyAdapter = CommonAdapter(mActivity, R.layout.view_dialog_choose_item, mTextMoneyList, holderConvert = { holder, data, position, payloads ->
            holder.apply {
                getView<RelativeLayout>(R.id.rlSelectDialogRvMenuG).apply {
                    if (data.checkState) {
                        setBackgroundColor(resources.getColor(R.color.colorBlueBright))
                        getView<TextView>(R.id.tvSelectDialogRvMenuG).setTextColor(resources.getColor(R.color.color_5ab1e1))
                        getView<ImageView>(R.id.ivSelectDialogRvMenuG).visibility = View.VISIBLE
                    } else {
                        setBackgroundResource(R.color.dialog_item_bg)
                        getView<TextView>(R.id.tvSelectDialogRvMenuG).setTextColor(resources.getColor(R.color.black))
                        getView<ImageView>(R.id.ivSelectDialogRvMenuG).visibility = View.GONE
                    }
                }
                setText(R.id.tvSelectDialogRvMenuG, data.name)
            }
        }, onItemClick = { view, holder, position ->
            mTextMoneyList[position].let {
                it.checkState = !it.checkState
            }
            mMoneyAdapter.notifyDataSetChanged()
        })
        mMenuView.search_result_condition_recycler_money.adapter = mMoneyAdapter

        //投资模式
        mGridManager = GridLayoutManager(mActivity, 3)
        mMenuView.search_result_condition_recycler_joinType.layoutManager = mGridManager
        mMenuView.search_result_condition_recycler_joinType.isNestedScrollingEnabled = false
        mJoinModelAdapter = CommonAdapter(mActivity, R.layout.view_dialog_choose_item, mTextJoinTypeList, holderConvert = { holder, data, position, payloads ->
            holder.apply {
                getView<RelativeLayout>(R.id.rlSelectDialogRvMenuG).apply {
                    if (data.checkState) {
                        setBackgroundColor(resources.getColor(R.color.colorBlueBright))
                        getView<TextView>(R.id.tvSelectDialogRvMenuG).setTextColor(resources.getColor(R.color.color_5ab1e1))
                        getView<ImageView>(R.id.ivSelectDialogRvMenuG).visibility = View.VISIBLE
                    } else {
                        setBackgroundResource(R.color.dialog_item_bg)
                        getView<TextView>(R.id.tvSelectDialogRvMenuG).setTextColor(resources.getColor(R.color.black))
                        getView<ImageView>(R.id.ivSelectDialogRvMenuG).visibility = View.GONE
                    }
                }
                setText(R.id.tvSelectDialogRvMenuG, data.name)
            }
        }, onItemClick = { view, holder, position ->
            mTextJoinTypeList[position].let {
                it.checkState = !it.checkState
            }
            mJoinModelAdapter.notifyDataSetChanged()
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
                                    BoxUtils.removeMoneyType(mTextMoneyList)
                                    mTextMoneyList.clear()
                                }
                                mTextMoneyList.addAll(it.capitalList)
                                BoxUtils.saveMoneyType(mTextMoneyList)
                                mMoneyAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }, {
                    ToastUtil.showNetError()
                }, {}, {})
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
                                mTextJoinTypeList.addAll(it.joinModes)
                                BoxUtils.saveJoinType(mTextJoinTypeList)
                                mJoinModelAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }, {
                    ToastUtil.showNetError()
                }, {}, {})
    }

    override fun showAsDropDown(anchor: View, xoff: Int, yoff: Int, gravity: Int) {
//        backgroundAlphaExt(0.5f)
        mMenuView.bottom_condition_view.visibility = View.VISIBLE
        //解决7.0showAsDropDown  失效
        if (Build.VERSION.SDK_INT == 24) {
            val rect = Rect()
            anchor.getGlobalVisibleRect(rect)
            val h = anchor.resources.displayMetrics.heightPixels - rect.bottom
            height = h
        }
        super.showAsDropDown(anchor, xoff, yoff, gravity)
    }

    //回调方法
    fun setOnSelectListener(selectListener: SelectCallBack) {
        mSelectCallBack = selectListener
    }

    interface SelectCallBack {
        fun onSelectCallBack(selectMoney: StringBuffer, selectType: StringBuffer)
    }

    override fun dismiss() {
        super.dismiss()
//        backgroundAlphaExt(1f)
        mMenuView.bottom_condition_view.visibility = View.GONE
    }


}