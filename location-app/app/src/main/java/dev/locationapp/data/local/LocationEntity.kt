package dev.locationapp.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "encrypted_latitude", typeAffinity = ColumnInfo.BLOB)
    val encryptedLatitude: ByteArray,
    @ColumnInfo(name = "encrypted_longitude", typeAffinity = ColumnInfo.BLOB)
    val encryptedLongitude: ByteArray,
    @ColumnInfo(name = "iv_latitude", typeAffinity = ColumnInfo.BLOB)
    val ivLatitude: ByteArray,
    @ColumnInfo(name = "iv_longitude", typeAffinity = ColumnInfo.BLOB)
    val ivLongitude: ByteArray,
    @ColumnInfo(name = "accuracy")
    val accuracy: Float,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    @ColumnInfo(name = "provider")
    val provider: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as LocationEntity
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
