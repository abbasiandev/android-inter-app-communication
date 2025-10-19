-keep class dev.abbasian.protocol.** { *; }
-keep interface dev.abbasian.protocol.** { *; }

-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepnames class * implements android.os.Parcelable

-keep class androidx.core.** { *; }
-dontwarn androidx.core.**

-dontwarn org.jetbrains.annotations.**