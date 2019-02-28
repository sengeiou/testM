package com.qingmeng.mengmeng.fragment

import android.annotation.SuppressLint
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.qingmeng.mengmeng.BaseFragment
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.activity.RedShopSeachResult
import com.qingmeng.mengmeng.adapter.CommonAdapter
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.entity.RedShopBean
import com.qingmeng.mengmeng.entity.RedShopLeftBean
import com.qingmeng.mengmeng.utils.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_red_shop.*
import kotlinx.android.synthetic.main.layout_red_news_head.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.support.v4.startActivity

@SuppressLint("CheckResult")
class RedShopFragment : BaseFragment() {
    private lateinit var mLauyoutManger: LinearLayoutManager
    private lateinit var mGridLayoutManager: GridLayoutManager
    private lateinit var mLeftAdapter: CommonAdapter<RedShopLeftBean>
    private lateinit var mRightAdapterType: CommonAdapter<RedShopLeftBean>
    private lateinit var mRightAdapterHost: CommonAdapter<RedShopLeftBean>
    private var mLeftList = ArrayList<RedShopLeftBean>()
    private var mRightInListType = ArrayList<RedShopLeftBean>()
    private var mRightInListHost = ArrayList<RedShopLeftBean>()
    override fun getLayoutId(): Int = R.layout.fragment_red_shop

    override fun initObject() {
        super.initObject()
        mRedNewsTitle.setText(R.string.tab_name_red_shop)
        // 获得状态栏高度
        val statusBarHeight = getBarHeight(context!!)
        //给布局的高度重新设置一下 加上状态栏高度
        mRedNewsHead.layoutParams.height = mRedNewsHead.layoutParams.height + getBarHeight(context!!)
        mRedNewsTitle.setMarginExt(top = statusBarHeight + context!!.dp2px(60))
        mRedNewsBack.visibility = View.GONE

        initLeftAdapter()
        initRightAdapter()
    }

    override fun initData() {
        super.initData()
        getCacheData()

    }

    private fun getCacheData() {
        Observable.create<RedShopBean> {
            val leftList = BoxUtils.getAllRedShop(0, 0)
            val rightInListType = BoxUtils.getAllRedShop(1, 2)
            val rightInListHost = BoxUtils.getAllRedShop(1, 1)
            if (!mLeftList.isEmpty()) {
                BoxUtils.removeAllRedShop(mLeftList)
                mLeftList.clear()
            }
            if (!mRightInListType.isEmpty()) {
                BoxUtils.removeAllRedShop(mRightInListType)
                mRightInListType.clear()
            }
            if (!mRightInListHost.isEmpty()) {
                BoxUtils.removeAllRedShop(mRightInListHost)
                mRightInListHost.clear()
            }
            mLeftList.addAll(leftList)
            mRightInListType.addAll(rightInListType)
            mRightInListHost.addAll(rightInListHost)
            var mRedShop = RedShopBean(ArrayList())
            if (!mLeftList.isEmpty()) {
                mRedShop = RedShopBean.fromString(mLeftList[0].id)
                // mLeftList[0].checkState = true
            }
            if (!mRightInListType.isEmpty()) {
                mRedShop = RedShopBean.fromString(mRightInListType[0].id)
            }
            if (!mRightInListHost.isEmpty()) {
                mRedShop = RedShopBean.fromString(mRightInListHost[0].id)
            }
            it.onNext(mRedShop)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (!mLeftList.isEmpty()) {
                        mLeftAdapter.notifyDataSetChanged()
                    }
                    if (!mRightInListType.isEmpty()) {
                        mRightAdapterType.notifyDataSetChanged()
                    }
                    mRightAdapterHost.notifyDataSetChanged()
                    httpLoad(0, getVersion(0))
                    httpLoad2(1, getVersion(1))
                }, {

                    httpLoad(0, getVersion(0))
                    httpLoad2(1, getVersion(1))
                }, {}, { addSubscription(it) })

    }

    //点击缓存
    private fun getClickCache(fahterId: Long) {
        Observable.create<RedShopBean> {
            val rightInListType = BoxUtils.getAllRedShop(fahterId, 2)
            val rightInListHost = BoxUtils.getAllRedShop(fahterId, 1)
            if (!mRightInListType.isEmpty()) {
                mRightInListType.clear()
            }
            if (!mRightInListHost.isEmpty()) {
                mRightInListHost.clear()
            }
            mRightInListType.addAll(rightInListType)
            mRightInListHost.addAll(rightInListHost)
            var mRedShop = RedShopBean(ArrayList())
            if (!mRightInListType.isEmpty()) {
                mRedShop = RedShopBean.fromString(mRightInListType[0].id)
            }
            if (!mRightInListHost.isEmpty()) {
                mRedShop = RedShopBean.fromString(mRightInListHost[0].id)
            }
            it.onNext(mRedShop)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (!mRightInListType.isEmpty()) {
                        mRightAdapterType.notifyDataSetChanged()
                    }
                    mRightAdapterHost.notifyDataSetChanged()
                    httpLoad2(fahterId, getVersion(1))
                }, {
                    httpLoad2(fahterId, getVersion(1))
                }, {}, { addSubscription(it) })

    }

    //加载   左边适配
    private fun initLeftAdapter() {
        mLauyoutManger = LinearLayoutManager(context)
        red_shop_left_recyclerview.layoutManager = mLauyoutManger
        mLeftAdapter = CommonAdapter(context!!, R.layout.red_shop_left_item, mLeftList, holderConvert = { holder, data, _, _ ->
            holder.apply {
                if (data.checkState) {
                    getView<RelativeLayout>(R.id.red_shop_left_lineralayout).backgroundColor = resources.getColor(R.color.white)
                    getView<TextView>(R.id.red_shop_left_textview).setTextColor(resources.getColor(R.color.color_5ab1e1))
                } else {
                    getView<RelativeLayout>(R.id.red_shop_left_lineralayout).backgroundColor = resources.getColor(R.color.page_background_f5)
                    getView<TextView>(R.id.red_shop_left_textview).setTextColor(resources.getColor(R.color.color_666666))
                }
                setText(R.id.red_shop_left_textview, data.name)
            }
        }, onItemClick = { _, _, position ->
            mLeftList.forEach {
                it.checkState = false
            }
            mLeftList[position].checkState = true
            getClickCache(mLeftList[position].id.toLong())
            mLeftAdapter.notifyDataSetChanged()
        })
        red_shop_left_recyclerview.adapter = mLeftAdapter
    }

    //右边适配
    private fun initRightAdapter() {
        //分类列表
        mGridLayoutManager = GridLayoutManager(context, 3)
        red_shop_right_recyclerview_type.layoutManager = mGridLayoutManager
        //recyclerView禁止滑动
        red_shop_right_recyclerview_type.isNestedScrollingEnabled = false
        mRightAdapterType = CommonAdapter(context!!, R.layout.red_shop_right_in_item, mRightInListType, holderConvert = { holder, data, _, _ ->
            holder.apply {
                if (mRightInListType.isNotEmpty()) {
                    red_shop_right_text_type.visibility = View.VISIBLE
                } else {
                    red_shop_right_text_type.visibility = View.GONE
                }
                setText(R.id.red_shop_right_inContent, data.name)
                Glide.with(this@RedShopFragment).load(data.logo).apply(RequestOptions().fitCenter()
                        .placeholder(R.drawable.default_img_icon).error(R.drawable.default_img_icon)).into(getView(R.id.red_shop_right_inImageView))
                getView<LinearLayout>(R.id.red_shop_all).setOnClickListener {
                    startActivity<RedShopSeachResult>(IConstants.firstLevel to data.fahterId.toInt(), IConstants.secondLevel to data.id, IConstants.THREE_LEVEL to data.name)
                }
            }
        })
        red_shop_right_recyclerview_type.adapter = mRightAdapterType

        //热门品牌列表
        mGridLayoutManager = GridLayoutManager(context, 3)
        red_shop_right_recyclerview_host.layoutManager = mGridLayoutManager
        red_shop_right_recyclerview_host.isNestedScrollingEnabled = false
        mRightAdapterHost = CommonAdapter(context!!, R.layout.red_shop_right_in_item, mRightInListHost, holderConvert = { holder, data, _, _ ->
            holder.apply {
                if (mRightInListHost.isNotEmpty()) {
                    red_shop_right_text_host.visibility = View.VISIBLE
                } else {
                    red_shop_right_text_host.visibility = View.GONE
                }
                setText(R.id.red_shop_right_inContent, data.name)
                Glide.with(this@RedShopFragment).load(data.logo).apply(RequestOptions()
                        .placeholder(R.drawable.default_img_icon).error(R.drawable.default_img_icon)).into(getView(R.id.red_shop_right_inImageView))
                getView<LinearLayout>(R.id.red_shop_all).setOnClickListener {
                    startActivity<RedShopSeachResult>(IConstants.firstLevel to data.fahterId.toInt(), IConstants.secondLevel to data.id, IConstants.THREE_LEVEL to data.name)
                }
            }
        })
        red_shop_right_recyclerview_host.adapter = mRightAdapterHost
    }

    @SuppressLint("CheckResult")
    private fun httpLoad(type: Long, version: String) {
        ApiUtils.getApi()
                .getRedShopRight(type, version)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    it.apply {
                        if (code == 12000) {
                            data?.let {
                                if (!mLeftList.isEmpty()) {
                                    BoxUtils.removeAllRedShop(mLeftList)
                                    mLeftList.clear()
                                }
                                it.setVersion()
                                BoxUtils.saveAllRedShop(mLeftList)
                                mLeftList.addAll(it.type.typeList)//1级分类
                                mLeftList.forEach {
                                    if (it.id == 1) {
                                        it.checkState = true
                                    }
                                }
                                mLeftAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }, {
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

    private fun httpLoad2(type: Long, version: String) {
        ApiUtils.getApi()
                .getRedShopRight(type, version)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    it.apply {
                        if (code == 12000) {
                            data?.let {
                                if (!mRightInListType.isEmpty()) {
                                    BoxUtils.removeAllRedShop(mRightInListType)
                                    mRightInListType.clear()
                                }
                                if (!mRightInListHost.isEmpty()) {
                                    BoxUtils.removeAllRedShop(mRightInListHost)
                                    mRightInListHost.clear()
                                }
                                it.setVersion()
                                mRightInListType.addAll(it.type.typeList)
                                BoxUtils.saveAllRedShop(mRightInListType)
                                mRightInListHost.addAll(it.popularBrands.hotBrands!!)
                                BoxUtils.saveAllRedShop(mRightInListHost)
                                mRightAdapterType.notifyDataSetChanged()
                                mRightAdapterHost.notifyDataSetChanged()
                            }
                        }
                    }
                }, {
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

    /**
     * @param type 0:左边数据 1:右边数据
     **/
    private fun getVersion(type: Int): String {
        return when {
            type == 0 && !mLeftList.isEmpty() -> mLeftList[0].version
            type == 1 && !mRightInListType.isEmpty() -> mRightInListType[0].version
            else -> ""
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            mLeftList.forEach {
                if (it.id == 1) {
                    it.checkState = true
                    getClickCache(it.id.toLong())
                }
            }
            mLeftAdapter.notifyDataSetChanged()
        }
    }
}