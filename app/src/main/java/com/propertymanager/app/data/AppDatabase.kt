package com.propertymanager.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [RoomName::class, LightBill::class, LightReading::class, RentRecord::class, Deposit::class, Due::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun nameDao(): NameDao
    abstract fun lightDao(): LightDao
    abstract fun rentDao(): RentDao
    abstract fun depositDao(): DepositDao
    abstract fun dueDao(): DueDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext, AppDatabase::class.java, "property_manager.db"
            ).build().also { instance = it }
        }
    }
}
