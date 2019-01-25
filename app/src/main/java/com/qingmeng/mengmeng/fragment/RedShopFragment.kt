package com.qingmeng.mengmeng.fragment

import android.annotation.SuppressLint
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.qingmeng.mengmeng.BaseFragment
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.activity.RedShopSeachResult
import com.qingmeng.mengmeng.adapter.CommonAdapter
import com.qingmeng.mengmeng.entity.RedShopLeftBean
import com.qingmeng.mengmeng.utils.*
import com.qingmeng.mengmeng.utils.imageLoader.GlideLoader
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
        httpLoad()
//        setData()
//        setRightdata()
    }

    override fun initData() {
        super.initData()
    }

    override fun initListener() {
        super.initListener()
        mRedNewsTitle.setOnClickListener {
            httpLoad()
        }
    }

    //加载   左边适配
    private fun initLeftAdapter() {
        mLauyoutManger = LinearLayoutManager(context)
        red_shop_left_recyclerview.layoutManager = mLauyoutManger
        mLeftAdapter = CommonAdapter(context!!, R.layout.red_shop_left_item, mLeftList, holderConvert = { holder, data, position, payloads ->
            holder.apply {
                if (position == 0) {
                    data.checkState = true
                }
                if (data.checkState) {
                    getView<RelativeLayout>(R.id.red_shop_left_lineralayout).backgroundColor = resources.getColor(R.color.white)
                } else {
                    getView<RelativeLayout>(R.id.red_shop_left_lineralayout).backgroundColor = resources.getColor(R.color.page_background_f5)
                }
                setText(R.id.red_shop_left_textview, data.name)
            }

        }, onItemClick = { view, holder, position ->
            mLeftList.forEach {
                it.checkState = false
            }
            mLeftList[position].checkState = true
            httpLoad(mLeftList[position].id)
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
        mRightAdapterType = CommonAdapter(context!!, R.layout.red_shop_right_in_item, mRightInListType, holderConvert = { holder, data, position, payloads ->
            holder.apply {
                if (mRightInListType.isNotEmpty()) {
                    red_shop_right_text_type.visibility = View.VISIBLE
                } else {
                    red_shop_right_text_type.visibility = View.GONE
                }
                setText(R.id.red_shop_right_inContent, data.name)
                GlideLoader.load(this@RedShopFragment, data.logo, getView(R.id.red_shop_right_inImageView))
            }
        }, onItemClick = { view, holder, position ->
            startActivity<RedShopSeachResult>()
        })
        red_shop_right_recyclerview_type.adapter = mRightAdapterType

        //热门品牌列表
        mGridLayoutManager = GridLayoutManager(context, 3)
        red_shop_right_recyclerview_host.layoutManager = mGridLayoutManager
        red_shop_right_recyclerview_host.isNestedScrollingEnabled = false
        mRightAdapterHost = CommonAdapter(context!!, R.layout.red_shop_right_in_item, mRightInListHost, holderConvert = { holder, data, position, payloads ->
            holder.apply {
                if (mRightInListHost.isNotEmpty()) {
                    red_shop_right_text_host.visibility = View.VISIBLE
                } else {
                    red_shop_right_text_host.visibility = View.GONE
                }
                setText(R.id.red_shop_right_inContent, data.name)
                GlideLoader.load(this@RedShopFragment, data.logo, getView(R.id.red_shop_right_inImageView))
            }
        }, onItemClick = { view, holder, position ->
            startActivity<RedShopSeachResult>()
        })
        red_shop_right_recyclerview_host.adapter = mRightAdapterHost
    }

    @SuppressLint("CheckResult")
    private fun httpLoad(type: Int = 0, version: String? = null) {
        ApiUtils.getApi()
                .getRedShopRight(type)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    it.apply {
                        if (code == 12000) {
                            if (type == 0) {
                                data?.let {
                                    mLeftList.clear()
                                    mLeftList.addAll(it.type.typeList)
                                }
                                httpLoad(1)
                            } else {
                                data?.let {
                                    mRightInListType.clear()
                                    mRightInListHost.clear()
                                    mRightInListType.addAll(it.type.typeList)
                                    mRightInListHost.addAll(it.popularBrands.hotBrands)
                                    mLeftAdapter.notifyDataSetChanged()
                                    mRightAdapterType.notifyDataSetChanged()
                                    mRightAdapterHost.notifyDataSetChanged()
                                }
                            }
                        } else {
                            ToastUtil.showShort(it.msg)
                        }
                    }
                }, {
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

//   @SuppressLint("CheckResult")
//    private fun httpLoad2(type: Int = 0, version: String? = null) {
//        ApiUtils.getApi()
//                .getRedShopRight(type)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe({
//                    it.apply {
//                        if (code == 12000) {
//                            data?.let {
//                                mRightInListType.clear()
//                                mRightInListHost.clear()
//                                mRightInListType.addAll(it.type.typeList)
//                                mRightInListHost.addAll(it.popularBrands.hotBrands)
//                                mLeftAdapter.notifyDataSetChanged()
//                                mRightAdapterType.notifyDataSetChanged()
//                                mRightAdapterHost.notifyDataSetChanged()
//                            }
//                        } else {
//                            ToastUtil.showShort(it.msg)
//                        }
//                    }
//                }, {
//                    ToastUtil.showNetError()
//                }, {}, { addSubscription(it) })
//    }


    private fun setData() {

    }
}