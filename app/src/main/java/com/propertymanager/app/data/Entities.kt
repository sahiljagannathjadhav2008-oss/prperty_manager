package com.propertymanager.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Tenant name for a room. A row exists only for months where the name was explicitly
 * entered/changed. Months without a row inherit the latest earlier row (carry-forward).
 * month = 1..12
 */
@Entity(tableName = "room_names", primaryKeys = ["roomId", "year", "month"])
data class RoomName(val roomId: String, val year: Int, val month: Int, val name: String)

@Entity(tableName = "light_bills", primaryKeys = ["year", "month"])
data class LightBill(val year: Int, val month: Int, val totalUnits: Double?, val totalAmount: Double?)

@Entity(
    tableName = "light_readings",
    primaryKeys = ["year", "month", "roomId"],
    foreignKeys = [ForeignKey(
        entity = LightBill::class,
        parentColumns = ["year", "month"],
        childColumns = ["year", "month"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class LightReading(
    val year: Int,
    val month: Int,
    val roomId: String,
    val currentUnit: Double?,
    val previousUnit: Double?,
    val paid: Boolean
)

@Entity(tableName = "rent_records", primaryKeys = ["year", "month", "roomId"])
data class RentRecord(val year: Int, val month: Int, val roomId: String, val rent: Double?)

/** Deposits are not month based (same as the original app). */
@Entity(tableName = "deposits", primaryKeys = ["roomId"])
data class Deposit(val roomId: String, val name: String, val amount: Double?)

@Entity(tableName = "dues")
data class Due(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val room: String,
    val type: String,
    val amount: Double,
    val note: String,
    val date: String
)
