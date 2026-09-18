# ==============================================================================
# MorseGO - R8 Code Shrinking & Optimization Rules
# ==============================================================================

# Preserve custom views referenced from XML layouts
-keep public class com.morsego.app.ui.view.** extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Preserve Activities and Application class
-keep public class com.morsego.app.MainActivity { *; }
-keep public class com.morsego.app.MorseGoApp { *; }

# Preserve DialogFragments
-keep public class * extends androidx.fragment.app.DialogFragment {
    public <init>();
}

# Preserve Enums (e.g. KeyerSettings$Mode)
-keepclassmembers enum com.morsego.app.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Retain source file and line numbers for meaningful crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Strip verbose and debug logging calls in release builds for maximum optimization
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}

# Suppress harmless warnings from AndroidX and Material libraries
-dontwarn androidx.**
-dontwarn com.google.android.material.**
