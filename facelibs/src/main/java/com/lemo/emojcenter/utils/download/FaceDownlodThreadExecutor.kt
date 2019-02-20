package com.lemo.emojcenter.utils.download

import java.util.concurrent.BlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Description :
 *
 *
 * Author:fangxu
 *
 *
 * Email:634804858@qq.com
 *
 *
 * Date: 2018/2/5
 */

class FaceDownlodThreadExecutor(corePoolSize: Int, maximumPoolSize: Int, keepAliveTime: Long, unit: TimeUnit, workQueue: BlockingQueue<Runnable>) : ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue)
