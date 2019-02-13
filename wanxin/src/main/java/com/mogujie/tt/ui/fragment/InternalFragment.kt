package com.mogujie.tt.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import com.app.common.save.Preference

import com.leimo.wanxin.R
import com.mogujie.tt.config.IntentConstant
import com.mogujie.tt.ui.activity.WebViewFragmentActivity
import com.mogujie.tt.ui.adapter.InternalAdapter
import com.mogujie.tt.ui.base.TTBaseFragment

class InternalFragment : TTBaseFragment() {

    private var curView: View? = null
    private var internalListView: ListView? = null
    private var mAdapter: InternalAdapter? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (null != curView) {
            (curView!!.parent as ViewGroup).removeView(curView)
            return curView
        }
        curView = inflater!!.inflate(R.layout.tt_fragment_internal,
                topContentView)

        initRes()
        mAdapter = InternalAdapter(this.activity)
        internalListView!!.adapter = mAdapter
        mAdapter!!.update()
        return curView
    }

    private fun initRes() {
        // 设置顶部标题栏
        setTopTitle(activity.getString(R.string.main_innernet))
        internalListView = curView!!.findViewById<View>(R.id.internalListView) as ListView
        internalListView!!.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val url = mAdapter!!.getItem(i)!!.itemUrl
            val intent = Intent(this@InternalFragment.activity, WebViewFragmentActivity::class.java)
            intent.putExtra(IntentConstant.WEBVIEW_URL, url)
            startActivity(intent)
        }
        val tokenDB by Preference(activity.applicationContext, "token", "")
        curView?.findViewById<TextView>(R.id.tvToken)?.text = tokenDB
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun initHandler() {}

}
