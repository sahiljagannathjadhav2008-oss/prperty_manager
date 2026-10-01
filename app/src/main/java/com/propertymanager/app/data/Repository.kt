package com.propertymanager.app.data

import androidx.room.withTransaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Repository(private val db: AppDatabase) {

    // ---- Names (carry-forward) ----

    /** Explicit name for this month, otherwise the latest earlier month's name, otherwise "". */
    suspend fun nameFor(room: String, year: Int, month: Int): String =
        db.nameDao().latestUpTo(room, year * 12 + month) ?: ""

    /**
     * Writes an explicit row only when the name differs from what would be inherited,
     * or when this month already has an explicit row. Earlier months are never touched.
     */
    private suspend fun saveName(room: String, year: Int, month: Int, raw: String) {
        val dao = db.nameDao()
        val name = raw.trim()
        val exists = dao.countExact(room, year, month) > 0
        val inherited = dao.latestBefore(room, year * 12 + month) ?: ""
        if (exists || name != inherited) dao.upsert(RoomName(room, year, month, name))
    }

    private suspend fun saveNames(year: Int, month: Int, names: Map<String, String>) {
        names.forEach { (room, name) -> saveName(room, year, month, name) }
    }

    // ---- Light ----
    suspend fun lightBill(y: Int, m: Int) = db.lightDao().bill(y, m)
    suspend fun lightReadings(y: Int, m: Int) = db.lightDao().readings(y, m)

    suspend fun saveLight(bill: LightBill, readings: List<LightReading>, names: Map<String, String>) =
        db.withTransaction {
            db.lightDao().upsertBill(bill)
            db.lightDao().upsertReadings(readings)
            saveNames(bill.year, bill.month, names)
        }

    // ---- Rent ----
    suspend fun rents(y: Int, m: Int) = db.rentDao().records(y, m)

    suspend fun saveRent(y: Int, m: Int, records: List<RentRecord>, names: Map<String, String>) =
        db.withTransaction {
            db.rentDao().upsertAll(records)
            saveNames(y, m, names)
        }

    // ---- Deposit ----
    suspend fun deposits() = db.depositDao().all()
    suspend fun saveDeposits(list: List<Deposit>) = db.depositDao().upsertAll(list)

    // ---- Due ----
    fun dues() = db.dueDao().all()

    suspend fun addDue(room: String, type: String, amount: Double, note: String) {
        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        db.dueDao().insert(Due(room = room, type = type, amount = amount, note = note, date = date))
    }

    suspend fun receiveDue(id: Long) = db.dueDao().delete(id)
}
