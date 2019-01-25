package com.app.common.base

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import com.app.common.view.LoadingDialogFragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 *
 */
abstract class AppBaseActivity : AppCompatActivity(), IBase {
    protected var activity = this
    private val mCompositeDisposable by lazy { CompositeDisposable() }
    private val mLoadingDialog by lazy { LoadingDialogFragment() }
    private var mIsShowLoading = false

    var isResume = false

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            savedInstanceState.putParcelable("android:support:fragments", null)
        }
        super.onCreate(savedInstanceState ?: Bundle())
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        initTop()
        bindLayout()?.let {
            setContentView(it)
        }
        initData()
        initView()
        initValue()
        initListener()
    }

    abstract fun bindLayout(): Int?
    override fun initTop() {}
    override fun initView() {}
    override fun initValue() {}
    override fun initData() {}
    override fun initListener() {}

    override fun onResume() {
        isResume = true
        super.onResume()
    }

    fun addSubscription(disposable: Disposable) {
        mCompositeDisposable.add(disposable)
    }

    /**
     * @param isCanCancel 是否能被取消
     */
    fun showLoadingDialog(isCanCancel: Boolean = true) {
        if (!mLoadingDialog.isShowing()) {
            mIsShowLoading = true
            mLoadingDialog.setIsBackCanceled(isCanCancel)
            mLoadingDialog.show(supportFragmentManager, "loading", isResume)
        }
    }
    /**
     * 为fragment设置functions，具体实现子类来做
     * @param fragmentId
     */
    open fun setFunctionsForFragment(fragmentTag: String) {}

    fun dismissLoadingDialog() {
        mLoadingDialog.dismiss()
    }

    override fun onPause() {
        isResume = false
        super.onPause()
    }

    override fun onDestroy() {
        mCompositeDisposable.clear()
        super.onDestroy()
    }
}