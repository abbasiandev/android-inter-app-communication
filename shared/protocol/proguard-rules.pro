# Keep LocationData for Parcelable
-keep class dev.abbasian.protocol.domain.model.LocationData { *; }
-keepclassmembers class dev.abbasian.protocol.domain.model.LocationData { *; }

# Keep all protocol classes
-keep class dev.abbasian.protocol.** { *; }