package com.lemo.emojcenter.activity

import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.lemo.emojcenter.FaceConfigInfo
import com.lemo.emojcenter.FaceInitData
import com.lemo.emojcenter.R
import com.lemo.emojcenter.adapter.FaceMyEmojAdapter
import com.lemo.emojcenter.base.FaceBaseActivity
import com.lemo.emojcenter.bean.EmojInfoBean
import com.lemo.emojcenter.bean.EmojOpeBean
import com.lemo.emojcenter.constant.FaceConstants
import com.lemo.emojcenter.constant.FaceEmojOpeType
import com.lemo.emojcenter.constant.FaceIConstants
import com.lemo.emojcenter.constant.FaceNetConst
import com.lemo.emojcenter.manage.FaceEmojManage
import com.lemo.emojcenter.utils.SimpleToast
import com.zhy.http.okhttp.OkHttpUtils
import com.zhy.http.okhttp.callback.StringCallback
import kotlinx.android.synthetic.main.face_activity_my_emoj.*
import okhttp3.Call
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.util.*

/**
 * Description :我的表情页面
 * Author:fangxu
 * Date: 2018/1/24
 */

class FaceMyEmojActivity : FaceBaseActivity(), View.OnClickListener {

    private lateinit var mMyEmojAdapter: FaceMyEmojAdapter
    private lateinit var mFootView: View
    private lateinit var mHeadView: View
    private lateinit var rlFootMyEmojAddEmoj: RelativeLayout
    private lateinit var rlFootMyEmojDownRecord: RelativeLayout
    private lateinit var mDatas: ArrayList<EmojInfoBean>

    private val mSize: Int = 0
    private var isChange: Boolean = false

    override fun getLayoutId(): Int {
        return R.layout.face_activity_my_emoj
    }

    override fun initView() {
        super.initView()
        mDatas = ArrayList()

        top_view.setRightOption()
        initHead()
        initFoot()
        initAdapter()
        EventBus.getDefault().register(this)
    }

    override fun initData() {
        super.initData()
        getData()
    }


    private fun initFoot() {
        mFootView = layoutInflater.inflate(R.layout.face_foot_my_emoj, null)
        rlFootMyEmojAddEmoj = mFootView.findViewById(R.id.rl_foot_my_emoj_add_emoj)
        rlFootMyEmojDownRecord = mFootView.findViewById(R.id.rl_foot_my_emoj_down_record)
        //        mMyEmojAdapter.addFooterView(mFootView);
    }

    private fun initHead() {
        mHeadView = layoutInflater.inflate(R.layout.face_head_my_emoj, null)
    }

    private fun initAdapter() {
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rclv_my_emoj.layoutManager = linearLayoutManager
        mMyEmojAdapter = FaceMyEmojAdapter(R.layout.face_item_my_emoj, mDatas)
        rclv_my_emoj.adapter = mMyEmojAdapter
    }

    override fun onResume() {
        super.onResume()
        if (isChange) {
            getData()
        }
    }

    override fun initListener() {
        super.initListener()
        rlFootMyEmojDownRecord.setOnClickListener(this)
        rlFootMyEmojAddEmoj.setOnClickListener(this)

        //设置item子控件移除按钮的点击事件
        mMyEmojAdapter.onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
            val itemEmojId = mDatas[position].faceId//条目子控件表情包id
            var fileName = mDatas[position].resource
            if (fileName != null) {
                fileName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.indexOf("."))
            }
            removefile(FaceInitData.userId, itemEmojId.toString(), position)
            Log.d(TAG, "onItemChildClick: #remove faceId=" + itemEmojId)
        }

    }

    fun removefile(userID: String, faceid: String, position: Int) {
        showLoadingDialog()
        //请求接口移除数据
        OkHttpUtils.post()
                .url(FaceIConstants.REMOVE_MY_EMOJ)
                .addHeader(FaceIConstants.APP_KEY, FaceConfigInfo.appKeyValue)
                .addParams(FaceNetConst.USERID, userID)
                .addParams(FaceNetConst.FACEID, faceid)
                .build()
                .execute(object : StringCallback() {
                    override fun onError(call: Call, e: Exception, id: Int) {
                        SimpleToast.show(applicationContext, "移除失败")
                        dismissLoadingDialog()
                    }

                    override fun onResponse(response: String, id: Int) {
                        dismissLoadingDialog()
                        val jsonObject = JSONObject(response)
                        val code = jsonObject.optString("code")
                        if (TextUtils.equals(code, FaceConstants.CODE_SUCCESS)) {
                            removeFaceById(faceid, position)
                            setHeadAndFootView()
                            FaceEmojManage.instance.removeEmojByFaceId(applicationContext, Integer.parseInt(faceid))
                            val emojOpe = EmojOpeBean(faceid)
                            emojOpe.emojOpeType = FaceEmojOpeType.EmojDelete
                            EventBus.getDefault().postSticky(emojOpe)
                        } else {
                            SimpleToast.show(applicationContext, "移除失败")
                        }
                    }
                })
    }

    @Synchronized
    private fun removeFaceById(faceId: String, position: Int) {
        val iterator = mDatas.iterator()
        while (iterator.hasNext()) {
            val emojBean = iterator.next()
            if (TextUtils.equals(emojBean.faceId.toString(), faceId)) {
                iterator.remove()
                Log.d(TAG, "removeFaceById: #remove=" + faceId)
            }
        }
        //        mMyEmojAdapter.notifyDataSetChanged();
        val item = position + mMyEmojAdapter.headerLayoutCount
        mMyEmojAdapter.notifyItemRemoved(item)
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onEvent(emojOpe: EmojOpeBean) {
        isChange = true
        getData()
    }

    private fun getData() {
        mDatas.clear()
        FaceEmojManage.instance.emojList?.let { mDatas.addAll(it) }
        setHeadAndFootView()
        mMyEmojAdapter.notifyDataSetChanged()

    }

    private fun setHeadAndFootView() {
        if (mDatas.size > 0 && mMyEmojAdapter.headerLayoutCount == 0) {
            mMyEmojAdapter.addHeaderView(mHeadView)
        }
        if (mDatas.size == 0 && mMyEmojAdapter.headerLayoutCount > 0) {
            mMyEmojAdapter.removeHeaderView(mHeadView)
        }
        if (mMyEmojAdapter.footerLayoutCount == 0) {
            mMyEmojAdapter.addFooterView(mFootView)
        }
    }

    companion object {
        private val TAG = "MyEmojActivity"
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.rl_foot_my_emoj_add_emoj) {
            startActivity(Intent(this@FaceMyEmojActivity, FaceMyAddEmojActivity::class.java))

        } else if (i == R.id.rl_foot_my_emoj_down_record) {
            startActivity(Intent(this@FaceMyEmojActivity, FaceDownloadRecordActivity::class.java))

        } else {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        OkHttpUtils.getInstance().cancelTag(this)
        EventBus.getDefault().unregister(this)
    }
}
