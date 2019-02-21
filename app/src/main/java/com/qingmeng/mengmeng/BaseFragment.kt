package com.qingmeng.mengmeng

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.qingmeng.mengmeng.utils.SharedSingleton
import com.qingmeng.mengmeng.view.dialog.DialogCustom
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by zq on 2018/8/6
 */
abstract class BaseFragment : Fragment() {
    private val STATE_SAVE_IS_HIDDEN = "STATE_SAVE_IS_HIDDEN"
    protected val sharedSingleton = SharedSingleton.instance
    protected lateinit var mView: View
    protected lateinit var myDialog: DialogCustom
    private val mCompositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(getLayoutId(), container, false)
        return mView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        myDialog = DialogCustom(context)
        initObject()
        initListener()
        initData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //防止Fragment重叠
        savedInstanceState?.let {
            val isSupportHidden = it.getBoolean(STATE_SAVE_IS_HIDDEN)
            fragmentManager?.apply {
                val ft = beginTransaction()
                if (isSupportHidden) {
                    ft.hide(this@BaseFragment)
                } else {
                    ft.show(this@BaseFragment)
                }
                ft.commit()
            }

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putBoolean(STATE_SAVE_IS_HIDDEN, isHidden)
        }
    }

    protected abstract fun getLayoutId(): Int

    open fun initObject() {}

    open fun initData() {}

    open fun initListener() {}

    protected fun addSubscription(disposable: Disposable) {
        mCompositeDisposable.add(disposable)
    }

    override fun onDestroy() {
        super.onDestroy()
        mCompositeDisposable.clear()
        myDialog.unBindContext()
    }
}