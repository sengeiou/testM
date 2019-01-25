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


#华为
-ignorewarning
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keep class com.hianalytics.android.**{*;}
-keep class com.huawei.updatesdk.**{*;}
-keep class com.huawei.hms.**{*;}

-keep class com.huawei.android.hms.agent.**{*;}


#小米
#这里com.xiaomi.mipushdemo.DemoMessageRreceiver改成app中定义的完整类名
#-keep class com.xiaomi.mipushdemo.DemoMessageReceiver {*;}
-keep class com.mogujie.tt.push.XiaomiPushReceiver {*;}
#可以防止一个误报的 warning 导致无法成功编译，如果编译使用的 Android 版本是 23。
-dontwarn com.xiaomi.push.**

#vivo
-dontwarn com.vivo.push.**-keep class com.vivo.push.**{*; }
-keep class com.vivo.vms.**{*; }
-keep class xxx.xxx.xxx.PushMessageReceiverImpl{*;}

#魅族
# for google protobuf
-keep public class * extends com.google.protobuf.GeneratedMessage { *; }
-keep class com.google.protobuf.** { *; }
-keep public class * extends com.google.protobuf.** { *; }
#for okhttp
-keep class okio.** {*;}
-dontwarn okio.**
-keep class com.squareup.okhttp.** {*;}
-dontwarn com.squareup.okhttp.**
# for push sdk
-keep class com.meizu.cloud.pushsdk.** { *; }
-dontwarn  com.meizu.cloud.pushsdk.**

-keep class com.meizu.nebula.** { *; }
-dontwarn com.meizu.nebula.**
-keep class com.meizu.push.** { *; }
-dontwarn com.meizu.push.**