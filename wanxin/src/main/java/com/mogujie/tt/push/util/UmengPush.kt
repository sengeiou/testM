//package com.mogujie.tt.push
//
//import android.app.Application
//import android.content.Context
//import com.app.common.logger.Logger
//import com.umeng.commonsdk.UMConfigure
//import com.umeng.message.IUmengRegisterCallback
//import com.umeng.message.PushAgent
//import com.umeng.message.UTrack
//import org.android.agoo.huawei.HuaWeiRegister
//import org.android.agoo.mezu.MeizuRegister
//import org.android.agoo.xiaomi.MiPushRegistar
//
//
///**
// * Created by wr
// * Date: 2019/1/4  13:31
// * mail: 1902065822@qq.com
// * describe:
// * @see 修改华为通道 需修改 AndroidManifest.xml com.huawei.hms.client.appid
// */
//object UmengPush {
//    val TAG = "UmengPush"
//
//    fun init(context: Application) {
//        initUmeng(context)
//        initUmengTongdao(context)
//    }
//
//    fun setAlias(context: Context, aligsName: String) {
//        //别名绑定，将某一类型的别名ID绑定至某设备，老的绑定设备信息被覆盖，别名ID和deviceToken是一对一的映射关系
//        PushAgent.getInstance(context).setAlias(aligsName, "wanxinId", UTrack.ICallBack { isSuccess, message ->
//            Logger.d("设置别名 isSuccess=${isSuccess} message=${message}")
//        })
//    }
//
//    private fun initUmengTongdao(context: Application) {
//        //小米
//        MiPushRegistar.register(context, UmengConfig.xiaomiAppId, UmengConfig.xiaomiAppKey)
//        //华为
//        HuaWeiRegister.register(context)
//        //魅族
//        MeizuRegister.register(context, UmengConfig.meizuAppId, UmengConfig.meizuAppKey)
//    }
//
//    private fun initUmeng(context: Application) {
//        // 在此处调用基础组件包提供的初始化函数 相应信息可在应用管理 -> 应用信息 中找到 http://message.umeng.com/list/apps
//        // 参数一：当前上下文context；
//        // 参数二：应用申请的Appkey（需替换）；
//        // 参数三：渠道名称；
//        // 参数四：设备类型，必须参数，传参数为UMConfigure.DEVICE_TYPE_PHONE则表示手机；传参数为UMConfigure.DEVICE_TYPE_BOX则表示盒子；默认为手机；
//        // 参数五：Push推送业务的secret 填充Umeng Message Secret对应信息（需替换）
//        UMConfigure.init(context, UmengConfig.appkey, "Umeng", UMConfigure.DEVICE_TYPE_PHONE, UmengConfig.umengSecret)
//
//        //注册推送服务，每次调用register方法都会回调该接口
//        PushAgent.getInstance(context).register(object : IUmengRegisterCallback {
//            override fun onSuccess(deviceToken: String) {
//                //注册成功会返回deviceToken deviceToken是推送消息的唯一标志
//                Logger.d("注册成功：deviceToken：-------->  $deviceToken")
//            }
//
//            override fun onFailure(s: String, s1: String) {
//                Logger.d("注册失败：-------->  s:$s,s1:$s1")
//            }
//        })
//    }
//}