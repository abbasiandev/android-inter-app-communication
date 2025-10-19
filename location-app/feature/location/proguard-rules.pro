-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }

-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepnames class * implements android.os.Parcelable

-keep class androidx.core.** { *; }
-dontwarn androidx.core.**

-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.material.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep @androidx.compose.runtime.Stable class * { *; }
-keep @androidx.compose.runtime.Immutable class * { *; }
-keepclassmembers class androidx.compose.** { *; }
-dontwarn androidx.compose.**

-keep class androidx.compose.material.icons.** { *; }

-keep class androidx.activity.compose.** { *; }
-keep class androidx.activity.** { *; }
-dontwarn androidx.activity.**

-keep class androidx.lifecycle.** { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>();
}
-keepclassmembers class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}

-keep class dagger.hilt.** { *; }
-keep class dagger.hilt.android.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel
-keepclasseswithmembernames class * {
    @dagger.* <methods>;
    @dagger.* <fields>;
}
-keepclasseswithmembernames class * {
    @javax.inject.* <fields>;
    @javax.inject.* <methods>;
}

-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * {
    *;
}
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-dontwarn androidx.room.paging.**

-keep class com.google.android.gms.location.** { *; }
-keep class com.google.android.gms.common.** { *; }
-keep class com.google.android.gms.tasks.** { *; }
-keep interface com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.**

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

-keep class dev.locationapp.feature.location.** { *; }