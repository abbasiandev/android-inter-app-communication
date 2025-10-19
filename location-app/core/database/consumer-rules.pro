-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-keepclassmembers class net.sqlcipher.** {
    *;
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keep @androidx.room.Database class *
-keepclassmembers class * extends androidx.room.RoomDatabase {
    *;
}
-keepclassmembers @androidx.room.Entity class * {
    *;
}

-keep class dev.locationapp.core.database.** { *; }
-keep class dev.locationapp.feature.location.data.local.** { *; }

-keep class androidx.sqlite.** { *; }
-keep interface androidx.sqlite.** { *; }
-dontwarn androidx.sqlite.**