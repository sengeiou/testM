# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-ignorewarnings

-dontwarn
#指定代码的压缩级别
-optimizationpasses 5
#包明不混合大小写
-dontusemixedcaseclassnames
#不去忽略非公共的库类,不混淆第三方引用的库
-dontskipnonpubliclibraryclasses
 #优化  不优化输入的类文件
-dontoptimize
 #预校验
-dontpreverify
 #混淆时是否记录日志
-verbose
 # 混淆时所采用的算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
#保护注解
-keepattributes *Annotation*
# 保持哪些类不被混淆
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
#如果有引用v4包可以添加下面这行
-keep public class * extends android.support.v4.app.Fragment

#忽略警告
-ignorewarning

##记录生成的日志数据,gradle build时在本项目根目录输出##
#apk 包内所有 class 的内部结构
-dump proguard/class_files.txt
#未混淆的类和成员
-printseeds proguard/seeds.txt
#列出从 apk 中删除的代码
-printusage proguard/unused.txt
########记录生成的日志数据，gradle build时 在本项目根目录输出-end######

#如果引用了v4或者v7包
-dontwarn android.support.**

####混淆保护自己项目的部分代码以及引用的第三方jar包library-end####

-keep class android.** {	*;}

#保持 native 方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}

#保持自定义控件类不被混淆
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

#保持自定义控件类不被混淆
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

#保持 Parcelable 不被混淆
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

#保持 Serializable 不被混淆
-keepnames class * implements java.io.Serializable

#保持 Serializable 不被混淆并且enum 类也不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

#保持枚举 enum 类不被混淆
-keepclassmembers enum * {
  public static **[] values();
  public static ** valueOf(java.lang.String);
}

-keepclassmembers class * {
    public void *ButtonClicked(android.view.View);
}

#不混淆资源类
-keepclassmembers class **.R$* {
    public static <fields>;
}

#避免混淆泛型 如果混淆报错建议关掉
-keepattributes Signature

#抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable

#重命名抛出异常时的文件名称
-renamesourcefileattribute SourceFile

#移除Log类打印各个等级日志的代码，打正式包的时候可以做为禁log使用，这里可以作为禁止log打印的功能使用，另外的一种实现方案是通过BuildConfig.DEBUG的变量来控制
#-assumenosideeffects class android.util.Log {
#    public static *** v(...);
#    public static *** i(...);
#    public static *** d(...);
#    public static *** w(...);
#    public static *** e(...);
#}
#############################################################################################
########################                 以上通用           #################################
#############################################################################################


#
#----------------------------- 第三方 -----------------------------
#

#support-v7-appcompat
-keep public class android.support.v7.** { *; }

#support-v4
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }

-keep public class * extends android.support.v4.view.ActionProvider {
    public <init>(android.content.Context);
}

-keep public class * extends android.support.annotation.**


#webview
-keepclassmembers class fqcn.of.javascript.interface.for.Webview {
   public *;
}
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, jav.lang.String);
}

#协程
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

#bugly
-dontwarn com.mTencent.bugly.**
-keep public class com.mTencent.bugly.**{*;}

#百度地图
-keep class com.baidu.** {*;}
-keep class mapsdkvi.com.** {*;}
-dontwarn com.baidu.**


#retrofit2
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions


-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-dontwarn rx.*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.examples.android.model.** { *; }


#okhttp3
-dontwarn com.squareup.okhttp3.**
-keep class com.squareup.okhttp3.** { *;}
-keep class okhttp3.**{*;}
-dontwarn okio.**
-dontwarn okhttp3.**

#rxjava
-keep class rx.schedulers.Schedulers {
    public static <methods>;
}
-keep class rx.schedulers.ImmediateScheduler {
    public <methods>;
}
-keep class rx.schedulers.TestScheduler {
    public <methods>;
}
-keep class rx.schedulers.Schedulers {
    public static ** test();
}
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; } 注释掉
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer


#glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

#leakcanary
-dontnote com.squareup.leakcanary.**
-keep class com.squareup.leakcanary.** {*;}
-dontnote android.app.NotificationManager.getNotificationChanne
-keep class android.app.NotificationManager.getNotificationChanne
-dontnote android.content.Context.checkSelfPermission
-keep class android.content.Context.checkSelfPermission

#腾讯
#-dontwarn class com.mTencent.**
-keep class com.mTencent.**{*;}

#eventbus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

#greendao
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
    public static java.lang.String TABLENAME;
}
-keep class **$Properties

# If you do not use SQLCipher:
-dontwarn org.greenrobot.greendao.database.**
# If you do not use Rx:
-dontwarn rx.**


#QQ
-keep class com.mTencent.open.TDialog$*
-keep class com.mTencent.open.TDialog$* {*;}
-keep class com.mTencent.open.PKDialog
-keep class com.mTencent.open.PKDialog {*;}
-keep class com.mTencent.open.PKDialog$*
-keep class com.mTencent.open.PKDialog$* {*;}

#新浪微博
-keep class com.sina.weibo.sdk.** { *; }


# banner
-keep class com.youth.banner.** { *;}

#
-dontnote com.app.common.**
-keep class com.app.common.** {*;}
-dontnote com.mogujie.tt.**
-keep class com.mogujie.tt.** {*;}


-dontnote com.google..**
-dontnote org.**


-keep class org.python.core.** { *; }
-dontwarn org.python.core.**

-keep class javax.servlet.** { *; }
-dontwarn javax.servlet.**

-keep class org.mozilla.javascript.** { *; }
-dontwarn org.mozilla.javascript.**

-keep class org.dom4j.** { *; }
-dontwarn org.dom4j.**

-keep class org.zeroturnaround.** { *; }
-dontwarn org.zeroturnaround.**

-keep class org.jaxen.** { *; }
-dontwarn org.jaxen.**

-keep class com.sun.org.apache.** { *; }
-dontwarn com.sun.org.apache.**

-keep class org.apache.** { *; }
-dontwarn org.apache.**

-keep class com.alibaba.fastjson.** { *; }
-dontwarn com.alibaba.fastjson.**

-keep class javax.swing.** { *; }
-dontwarn javax.swing.**

-keep class java.beans.** { *; }
-dontwarn java.beans.**

-keep class java.rmi.** { *; }
-dontwarn java.rmi.**

-keep class javax.el.** { *; }
-dontwarn javax.el.**

-keep class android.os.** { *; }
-dontwarn android.os.**

-keep class org.** { *; }
-dontwarn org.**

-keep class freemarker.ext.** { *; }
-dontwarn freemarker.ext.**


#
#----------------------------- 自己的类 -----------------------------
#

#-dontwarn com.youke.yingba.base.bean.**
#-keep class com.youke.yingba.base.bean.**{*;}
#
#-dontwarn com.youke.yingba.base.BaseBean
#-keep class com.youke.yingba.base.BaseBean{*;}
#
#-dontwarn com.youke.yingba.login.bean.**
#-keep class com.youke.yingba.login.bean.**{*;}


