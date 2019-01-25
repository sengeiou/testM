package com.qingmeng.mengmeng.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.view.View
import cn.jzvd.JZMediaManager
import cn.jzvd.Jzvd
import cn.jzvd.JzvdStd
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.DetailImgVpAdapter
import com.qingmeng.mengmeng.constant.IConstants.IMGS
import com.qingmeng.mengmeng.constant.IConstants.POSITION
import com.qingmeng.mengmeng.entity.ShopDetailImg
import kotlinx.android.synthetic.main.activity_video_detail.*

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@SuppressLint("SetTextI18n")
class VideoDetailActivity : BaseActivity() {
    private var position = 0
    private var totalImg = 0
    private lateinit var vpAdapter: DetailImgVpAdapter
    private lateinit var mImgList: ArrayList<ShopDetailImg>
    private var hasVideo = false

    override fun getLayoutId(): Int = R.layout.activity_video_detail

    override fun initObject() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        position = intent.getIntExtra(POSITION, 0)
        mImgList = intent.getSerializableExtra(IMGS) as ArrayList<ShopDetailImg>
        vpAdapter = DetailImgVpAdapter(this, mImgList)
        mImgDetailVp.adapter = vpAdapter
        mImgDetailVp.currentItem = position
        hasVideo = mImgList[0].isVideo
        totalImg = mImgList.filter { !it.isVideo }.size
        if (mImgList[position].isVideo) {
            mImgDetailCount.visibility = View.GONE
        } else {
            mImgDetailCount.visibility = View.VISIBLE
            mImgDetailCount.text = if (hasVideo) "$position/$totalImg" else "${position + 1}/$totalImg"
        }
    }

    override fun initListener() {
        mImgDetail.setOnClickListener { onBackPressed() }
        mImgDetailClose.setOnClickListener { onBackPressed() }
        mImgDetailVp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                if (mImgList[position].isVideo) {
                    mImgDetailCount.visibility = View.GONE
                    JzvdStd.goOnPlayOnResume()
                } else {
                    mImgDetailCount.visibility = View.VISIBLE
                    JzvdStd.goOnPlayOnPause()
                    mImgDetailCount.text = if (hasVideo) "$position/$totalImg" else "${position + 1}/$totalImg"
                }
            }
        })
    }

    override fun onBackPressed() {
        if (Jzvd.backPress()) return
        MainApplication.secondVideo?.let { second ->
            second.textureViewContainer.removeView(JZMediaManager.textureView)
            MainApplication.firstVideo?.apply {
                setUp(second.jzDataSource, JzvdStd.SCREEN_WINDOW_NORMAL)
                setState(second.currentState)
                addTextureView()
            }
        }
        setResult(Activity.RESULT_OK, Intent().putExtra(POSITION, mImgDetailVp.currentItem))
        super.onBackPressed()
    }

    override fun onDestroy() {
        MainApplication.secondVideo = null
        super.onDestroy()
    }
}