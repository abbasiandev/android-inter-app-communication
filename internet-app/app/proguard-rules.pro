-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}

-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepnames class * implements android.os.Parcelable

-keep class androidx.core.** { *; }
-dontwarn androidx.core.**

-keep class org.koin.** { *; }
-keep class org.koin.core.** { *; }
-keep class org.koin.android.** { *; }
-keep class org.koin.androidx.** { *; }
-keepnames class * extends org.koin.core.module.Module
-keepclassmembers class * {
    public <init>(...);
}
-keep class kotlin.reflect.** { *; }

-dontwarn org.jetbrains.annotations.**

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

-keep class dev.internetapp.** { *; }
-keep interface dev.internetapp.** { *; }

-keep class dev.internetapp.BuildConfig { *; }

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}