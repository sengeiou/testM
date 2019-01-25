package com.mogujie.tt.push

import android.app.Application
import android.content.Context


/**
 * Created by wr
 * Date: 2019/1/8  10:59
 * mail: 1902065822@qq.com
 * describe:
 */
object NullPush : PushBase {

    override fun register(context: Application) {
    }

    override fun unRegister(context: Application) {
    }

    override fun setAlias(context: Context, alias: String) {
    }

    override fun deleteAlias(context: Context, alias: String) {
    }

    override fun setTag(context: Context, tag: String) {
    }

    override fun deleteTag(context: Context, tag: String) {
    }

    override fun openNotify(context: Context) {
    }

    override fun closeNotify(context: Context) {
    }
}