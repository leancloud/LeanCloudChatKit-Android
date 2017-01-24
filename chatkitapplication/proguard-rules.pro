# proguard

# ------------------------ leancloud sdk ------------------------
-keepattributes Signature
-dontwarn com.jcraft.jzlib.**
-keep class com.jcraft.jzlib.**  { *;}

-dontwarn sun.misc.**
-keep class sun.misc.** { *;}

-dontwarn com.alibaba.fastjson.**
-dontnote com.alibaba.fastjson.**
-keep class com.alibaba.fastjson.** { *;}

-dontwarn sun.security.**
-keep class sun.security.** { *; }

-dontwarn com.google.**
-keep class com.google.** { *;}

-dontwarn com.avos.**
-dontnote com.avos.**
-keep class com.avos.** { *;}

-keep public class android.net.http.SslError
-keep public class android.webkit.WebViewClient

-dontwarn android.webkit.WebView
-dontwarn android.net.http.SslError
-dontwarn android.webkit.WebViewClient
-dontnote android.net.http.**

-dontwarn android.support.**

-dontwarn org.apache.**
-dontnote org.apache.**
-keep class org.apache.** { *;}

-dontwarn org.jivesoftware.smack.**
-keep class org.jivesoftware.smack.** { *;}

-dontwarn com.loopj.**
-keep class com.loopj.** { *;}

-dontwarn com.squareup.okhttp.**
-keep class com.squareup.okhttp.** { *;}
-keep interface com.squareup.okhttp.** { *; }

-dontwarn okio.**

-keep class com.google.protobuf.** { *; }
-keep public class * extends com.google.protobuf.** { *; }
-dontwarn com.google.protobuf.**

-dontwarn org.xbill.**
-keep class org.xbill.** { *;}

-keepattributes *Annotation*


# ------------------------ ChatKit ------------------------
-dontwarn cn.leancloud.chatkit.**
-keep class cn.leancloud.chatkit.** { *;}
-dontnote cn.leancloud.chatkit.**

# ------------------------ picasso ------------------------
-dontwarn com.squareup.picasso**
-keep class com.squareup.picasso.**{*;}

# ------------------------ eventbus ------------------------
-keepclassmembers class ** {
    public void onEvent*(**);
}

-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

-dontnote org.greenrobot.eventbus.*