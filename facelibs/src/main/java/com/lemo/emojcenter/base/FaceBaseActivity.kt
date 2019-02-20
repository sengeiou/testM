package com.lemo.emojcenter.base

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import com.lemo.emojcenter.R
import com.lemo.emojcenter.utils.UiUtil


/**
 * Description:
 * Author:wxw
 * Date:2018/1/23.
 */
abstract class FaceBaseActivity : AppCompatActivity(), FaceBaseViewInterface {
    private var mIsVisible: Boolean = false
    private var mLoadingDialog: Dialog? = null
    private val mWaitDialog: ProgressDialog? = null
    private val TAG = javaClass.simpleName

    protected lateinit var mInflater: LayoutInflater

    protected open fun getActionBarTitle(): Int {
        return R.string.app_name
    }

    protected open fun getLayoutId(): Int {
        return 0;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (getLayoutId() != 0) {

            Log.e("className", javaClass.name)
            setContentView(getLayoutId())
        }
        FaceAppManager.getAppManager().addActivity(this)
        requestedOrientation = SCREEN_ORIENTATION_PORTRAIT
        //        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mInflater = layoutInflater

        init(savedInstanceState)
        mIsVisible = true
        initView()
        initData()
        initListener()
    }

    override fun onPause() {
        super.onPause()
        if (this.isFinishing) {
            UiUtil.hideSoftKeyboard(currentFocus)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        FaceAppManager.getAppManager().removeActivity(this)
    }

    protected open fun hasToolbar(): Boolean {
        return true
    }

    protected fun inflateView(resId: Int): View {
        return mInflater.inflate(resId, null)
    }

    protected fun hasBackButton(): Boolean {
        return false
    }

    protected fun init(savedInstanceState: Bundle?) {}

    protected fun initToolbar(toolbar: Toolbar?) {
        if (toolbar == null) {
            return
        }
        toolbar.setPadding(0, UiUtil.getStatusBarHeight(this), 0, 0)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (hasBackButton()) {
            actionBar!!.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
        } else {
            actionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_TITLE
            actionBar.setDisplayUseLogoEnabled(false)
        }
        val titleRes = getActionBarTitle()
        if (titleRes != 0) {
            actionBar.setTitle(titleRes)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun initData() {}

    override fun initView() {}

    override fun initListener() {}

    //设置app内字体不受系统字体影响
    override fun getResources(): Resources {
        val res = super.getResources()
        val config = Configuration()
        config.setToDefaults()
        res.updateConfiguration(config, res.displayMetrics)
        return res
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragmentManager = supportFragmentManager
        val fragmentList = fragmentManager.fragments
        if (fragmentList != null) {
            for (indext in fragmentList.indices) {
                val fragment = fragmentList[indext] //找到第一层Fragment
                if (fragment == null) {
                    Log.w(TAG, "Activity result no fragment exists for index: 0x" + Integer.toHexString(requestCode))
                } else {
                    handleResult(fragment, requestCode, resultCode, data)
                }
            }
        }
    }

    /**
     * 递归调用，对所有的子Fragment生效.
     */
    private fun handleResult(fragment: Fragment, requestCode: Int, resultCode: Int, data: Intent?) {
        fragment.onActivityResult(requestCode, resultCode, data)//调用每个Fragment的onActivityResult
        Log.e(TAG, "MyBaseFragmentActivity")
        val childFragment = fragment.childFragmentManager.fragments //找到第二层Fragment
        if (childFragment != null) {
            for (f in childFragment) {
                if (f != null) {
                    handleResult(f, requestCode, resultCode, data)
                }
            }
        }
        if (childFragment == null) {
            Log.e(TAG, "MyBaseFragmentActivity1111")
        }
    }

    fun showLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog!!.isShowing) {
            return
        }
    }

    fun dismissLoadingDialog() {
        if (mLoadingDialog != null && !isFinishing && mLoadingDialog!!.isShowing) {
            mLoadingDialog!!.dismiss()
        }
    }
}

