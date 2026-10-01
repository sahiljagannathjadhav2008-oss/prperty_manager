package com.propertymanager.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

// key = year * 12 + month

@Dao
interface NameDao {
    @Query("SELECT name FROM room_names WHERE roomId = :room AND (year * 12 + month) <= :key ORDER BY (year * 12 + month) DESC LIMIT 1")
    suspend fun latestUpTo(room: String, key: Int): String?

    @Query("SELECT name FROM room_names WHERE roomId = :room AND (year * 12 + month) < :key ORDER BY (year * 12 + month) DESC LIMIT 1")
    suspend fun latestBefore(room: String, key: Int): String?

    @Query("SELECT COUNT(*) FROM room_names WHERE roomId = :room AND year = :year AND month = :month")
    suspend fun countExact(room: String, year: Int, month: Int): Int

    @Upsert
    suspend fun upsert(n: RoomName)
}

@Dao
interface LightDao {
    @Query("SELECT * FROM light_bills WHERE year = :y AND month = :m")
    suspend fun bill(y: Int, m: Int): LightBill?

    @Query("SELECT * FROM light_readings WHERE year = :y AND month = :m")
    suspend fun readings(y: Int, m: Int): List<LightReading>

    @Upsert
    suspend fun upsertBill(b: LightBill)

    @Upsert
    suspend fun upsertReadings(list: List<LightReading>)
}

@Dao
interface RentDao {
    @Query("SELECT * FROM rent_records WHERE year = :y AND month = :m")
    suspend fun records(y: Int, m: Int): List<RentRecord>

    @Upsert
    suspend fun upsertAll(list: List<RentRecord>)
}

@Dao
interface DepositDao {
    @Query("SELECT * FROM deposits")
    suspend fun all(): List<Deposit>

    @Upsert
    suspend fun upsertAll(list: List<Deposit>)
}

@Dao
interface DueDao {
    @Query("SELECT * FROM dues ORDER BY id")
    fun all(): Flow<List<Due>>

    @Insert
    suspend fun insert(d: Due)

    @Query("DELETE FROM dues WHERE id = :id")
    suspend fun delete(id: Long)
}
