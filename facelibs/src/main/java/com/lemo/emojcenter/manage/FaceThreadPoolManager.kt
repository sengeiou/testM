package com.lemo.emojcenter.manage

import java.util.concurrent.*

/**
 * Created by wangru
 * Date: 2018/4/12  16:11
 * mail: 1902065822@qq.com
 * describe:
 */

object FaceThreadPoolManager {
    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
    //核心线程的数量
    private val CORE_POOL_SIZE = CPU_COUNT + 1
    //线程池中最大线程数量
    private val MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1
    private  var mExecutorServiceDown: ExecutorService?=null
    private  var mExecutorServiceDownFile: ExecutorService?=null
    private  var mExecutorServiceSingle: ExecutorService?=null

    init {

    }

    fun newFixThreadPoolDown(): ExecutorService {
        if (mExecutorServiceDown == null) {
            synchronized(FaceThreadPoolManager::class.java) {
                if (mExecutorServiceDown == null) {

                    //非核心线程的超时时长
                    val KEEP_ALIVE = 1
                    val sPoolWorkQueue = LinkedBlockingQueue<Runnable>()
                    mExecutorServiceDown = ThreadPoolExecutor(3, 6, KEEP_ALIVE.toLong(), TimeUnit.MILLISECONDS, sPoolWorkQueue as BlockingQueue<Runnable>?)
                }
            }
        }
        return this!!.mExecutorServiceDown!!
    }

    fun newFixThreadPoolDownFile(): ExecutorService {
        if (mExecutorServiceDownFile == null) {
            synchronized(FaceThreadPoolManager::class.java) {
                if (mExecutorServiceDownFile == null) {
                    val KEEP_ALIVE = 1
                    val sPoolWorkQueue = LinkedBlockingQueue<Runnable>()
                    mExecutorServiceDownFile = ThreadPoolExecutor(3, 6, KEEP_ALIVE.toLong(), TimeUnit.MILLISECONDS, sPoolWorkQueue)
                }
            }
        }
        return this!!.mExecutorServiceDownFile!!
    }

    fun newFixThreadPoolSingle(): ExecutorService? {
        if (mExecutorServiceSingle == null) {
            synchronized(FaceThreadPoolManager::class.java) {
                if (mExecutorServiceSingle == null) {
                    mExecutorServiceSingle = Executors.newCachedThreadPool()
                }
            }
        }
        return mExecutorServiceSingle
    }
}
