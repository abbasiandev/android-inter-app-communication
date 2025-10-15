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

    @ColumnInfo(name = "accuracy")
    val accuracy: Float,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "provider")
    val provider: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as LocationEntity
        if (id != other.id) return false
        if (!encryptedLatitude.contentEquals(other.encryptedLatitude)) return false
        if (!encryptedLongitude.contentEquals(other.encryptedLongitude)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + encryptedLatitude.contentHashCode()
        result = 31 * result + encryptedLongitude.contentHashCode()
        return result
    }
}
