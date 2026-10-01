package com.propertymanager.app

import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.rint

val MONTHS = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)
val YEARS: List<Int> = (2024..2035).toList()
val LIGHT_ROOMS = listOf("A", "B", "C", "D")
val RENT_ROOMS = listOf("A", "B", "C")
val DUE_ROOMS = listOf("Room A", "Room B", "Room C", "Room D")
val DUE_TYPES = listOf("Rent", "Light Bill", "Other")

fun currentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1
fun currentYear(): Int = Calendar.getInstance().get(Calendar.YEAR).coerceIn(YEARS.first(), YEARS.last())

/** Same as JS: Number(x) || 0 */
fun num(s: String): Double = s.trim().toDoubleOrNull()?.takeIf { it.isFinite() } ?: 0.0
fun String.toAmount(): Double? = trim().toDoubleOrNull()?.takeIf { it.isFinite() }
fun fmt(d: Double): String = if (abs(d) < 1e15 && d == rint(d)) d.toLong().toString() else d.toString()
fun Double?.toInput(): String = if (this == null) "" else fmt(this)
fun money2(d: Double): String = String.format(Locale.US, "%.2f", d)
fun money4(d: Double): String = String.format(Locale.US, "%.4f", d)

data class RoomResult(val units: Double, val amount: Double)
data class LightResult(val perUnit: Double, val rooms: Map<String, RoomResult>)

/** Light bill logic, identical to the original L_calc(). Room D = total units - (A+B+C). */
fun calcLight(tu: Double, tp: Double, cur: Map<String, Double>, prev: Map<String, Double>): LightResult {
    val pu = if (tu != 0.0) tp / tu else 0.0
    val out = LinkedHashMap<String, RoomResult>()
    var abc = 0.0
    for (r in listOf("A", "B", "C")) {
        val u = (cur[r] ?: 0.0) - (prev[r] ?: 0.0)
        abc += u
        out[r] = RoomResult(u, u * pu)
    }
    val du = tu - abc
    out["D"] = RoomResult(du, du * pu)
    return LightResult(pu, out)
}

fun unitsText(r: RoomResult) = "Units: " + (if (r.units == 0.0) "-" else fmt(r.units))
fun amountText(r: RoomResult) = if (r.units == 0.0) "₹ -" else "₹ " + money2(r.amount)
