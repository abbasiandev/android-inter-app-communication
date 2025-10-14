# Keep LocationData for Parcelable
-keep class dev.abbasian.protocol.LocationData { *; }
-keepclassmembers class dev.abbasian.protocol.LocationData { *; }

# Keep all protocol classes
-keep class dev.abbasian.protocol.** { *; }