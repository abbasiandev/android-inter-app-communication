-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}

-keep class androidx.core.** { *; }
-dontwarn androidx.core.**

-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

-keep class dagger.hilt.** { *; }
-keep class dagger.hilt.android.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel
-keep @dagger.hilt.android.HiltAndroidApp class *

-keepclasseswithmembernames class * {
    @dagger.* <methods>;
    @dagger.* <fields>;
}
-keepclasseswithmembernames class * {
    @javax.inject.* <fields>;
    @javax.inject.* <methods>;
}

-dontwarn org.jetbrains.annotations.**

-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-dontwarn net.sqlcipher.**

-keep class dev.locationapp.** { *; }
-keep interface dev.locationapp.** { *; }

-keep class dev.locationapp.BuildConfig { *; }

-keep class * extends android.content.ContentProvider
-keep class dev.locationapp.feature.command.presentation.provider.** { *; }

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

-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepnames class * implements android.os.Parcelable

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**