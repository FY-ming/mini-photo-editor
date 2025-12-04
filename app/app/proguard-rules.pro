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

# 保留 Fragment 参数传递与反射调用（防止导航崩溃）
-keep public class * extends androidx.fragment.app.Fragment
-keepclassmembers class * extends androidx.fragment.app.Fragment { *; }

# 保留自定义 View 的构造器（防止UI崩溃）
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Gson、序列化等如果你用了，也建议保留模型
-keep class com.example.** { *; }

# 打印 log 代码可移除（可选）
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# 防止 GLRenderer 或自定义裁剪Overlay View被错误混淆
-keep class com.example.mini_photo_editor.ui.editor.opengl.GLRenderer { *; }
