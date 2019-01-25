package com.app.common.greendao

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.app.common.greendao.gen.DaoMaster
import com.app.common.greendao.gen.DaoSession
import com.app.common.greendao.utils.MySQLiteOpenHelper

/**
 * 数据库
 * Created by wangru
 * Date: 2017/12/20  9:47
 * mail: 1902065822@qq.com
 * describe:
 */
class DbManager {
    private var mDevOpenHelper: MySQLiteOpenHelper? = null
    private var mContext: Context? = null
    //获取可读数据
    val readableDatabase: SQLiteDatabase
        get() = mDevOpenHelper!!.readableDatabase

    //获取可写数据库
    val writableDatabase: SQLiteDatabase
        get() = mDevOpenHelper!!.writableDatabase

    //获取DaoMaster
    val daoMaster: DaoMaster
        get() = DaoMaster(writableDatabase)

    //获取DaoSession
    val daoSession: DaoSession
        get() = daoMaster.newSession()

    fun initDB(context: Context, dbName: String? = "app.db") {
        this.mContext = context
        // 初始化数据库信息
        mDevOpenHelper = MySQLiteOpenHelper(context, dbName, null)
    }

    companion object {
        var instance: DbManager = DbManager()
    }
}