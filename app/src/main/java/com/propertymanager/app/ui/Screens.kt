package com.propertymanager.app.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.propertymanager.app.AppViewModel
import com.propertymanager.app.DUE_ROOMS
import com.propertymanager.app.DUE_TYPES
import com.propertymanager.app.LIGHT_ROOMS
import com.propertymanager.app.RENT_ROOMS
import com.propertymanager.app.RoomResult
import com.propertymanager.app.amountText
import com.propertymanager.app.calcLight
import com.propertymanager.app.currentMonth
import com.propertymanager.app.currentYear
import com.propertymanager.app.data.Deposit
import com.propertymanager.app.data.LightBill
import com.propertymanager.app.data.LightReading
import com.propertymanager.app.data.RentRecord
import com.propertymanager.app.data.Repository
import com.propertymanager.app.fmt
import com.propertymanager.app.money2
import com.propertymanager.app.money4
import com.propertymanager.app.num
import com.propertymanager.app.toAmount
import com.propertymanager.app.toInput
import com.propertymanager.app.unitsText
import kotlinx.coroutines.launch

private fun roomLabel(name: String?, room: String) = (name ?: "").ifBlank { "Room $room" }

// =============================== LIGHT BILL ===============================

@Composable
fun LightScreen(vm: AppViewModel) {
    val f = vm.light
    val repo = vm.repo
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var showView by remember { mutableStateOf(false) }

    // Load saved numbers for the selected month/year (only when the month changed).
    LaunchedEffect(f.month, f.year) {
        val key = f.year * 12 + f.month
        if (f.dataKey != key) {
            val bill = repo.lightBill(f.year, f.month)
            val rs = repo.lightReadings(f.year, f.month).associateBy { it.roomId }
            f.tu = bill?.totalUnits.toInput()
            f.tp = bill?.totalAmount.toInput()
            for (r in LIGHT_ROOMS) {
                f.cur[r] = rs[r]?.currentUnit.toInput()
                f.prev[r] = rs[r]?.previousUnit.toInput()
                f.paid[r] = rs[r]?.paid ?: false
            }
            f.dataKey = key
        }
    }
    // Load names: explicit name for this month, else carried forward from earlier months.
    LaunchedEffect(f.month, f.year, vm.namesVersion) {
        val stamp = Pair(f.year * 12 + f.month, vm.namesVersion)
        if (f.nameStamp != stamp) {
            for (r in LIGHT_ROOMS) f.names[r] = repo.nameFor(r, f.year, f.month)
            f.nameStamp = stamp
        }
    }

    val result = calcLight(
        num(f.tu), num(f.tp),
        LIGHT_ROOMS.associateWith { num(f.cur[it] ?: "") },
        LIGHT_ROOMS.associateWith { num(f.prev[it] ?: "") }
    )

    ScreenScaffold("Light Bill", LightColor) {
        AppCard { MonthYearRow(f.month, f.year) { m, y -> f.month = m; f.year = y } }
        AppCard {
            SectionTitle("Main Bill")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NumField("Total Units", f.tu, { f.tu = it }, Modifier.weight(1f))
                NumField("Total Amount", f.tp, { f.tp = it }, Modifier.weight(1f))
            }
            OutlinedTextField(
                value = if (result.perUnit != 0.0) money4(result.perUnit) else "",
                onValueChange = {},
                enabled = false,
                label = { Text("Per Unit Price") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        AppCard {
            SectionTitle("Rooms")
            for (r in LIGHT_ROOMS) {
                val res = result.rooms.getValue(r)
                RoomBox {
                    Text(roomLabel(f.names[r], r), fontWeight = FontWeight.Bold)
                    NameField(f.names[r] ?: "") { f.names[r] = it }
                    NumField("Current Unit", f.cur[r] ?: "", { f.cur[r] = it })
                    NumField("Previous Unit", f.prev[r] ?: "", { f.prev[r] = it })
                    if (r != "D") {
                        val paid = f.paid[r] == true
                        StatusChip(if (paid) "Paid" else "Unpaid", paid) { f.paid[r] = !paid }
                    }
                    Badge(unitsText(res))
                    Badge(amountText(res))
                }
            }
        }
        AppCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        val y = f.year
                        val m = f.month
                        scope.launch {
                            val bill = LightBill(y, m, f.tu.toAmount(), f.tp.toAmount())
                            val readings = LIGHT_ROOMS.map { r ->
                                LightReading(
                                    y, m, r,
                                    (f.cur[r] ?: "").toAmount(), (f.prev[r] ?: "").toAmount(),
                                    r != "D" && f.paid[r] == true
                                )
                            }
                            repo.saveLight(bill, readings, LIGHT_ROOMS.associateWith { f.names[it] ?: "" })
                            vm.namesVersion++
                            Toast.makeText(ctx, "Saved", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("💾 SAVE") }
                OutlinedButton(onClick = { showView = true }, modifier = Modifier.weight(1f)) { Text("👁 VIEW") }
            }
        }
    }

    if (showView) {
        SummaryDialog("Light Summary", true, f.month, f.year, { showView = false }) { m, y ->
            LightSummaryBody(repo, m, y)
        }
    }
}

private class LightSummary(val bill: LightBill?, val readings: Map<String, LightReading>, val names: Map<String, String>)

@Composable
private fun LightSummaryBody(repo: Repository, m: Int, y: Int) {
    val data by produceState<LightSummary?>(null, m, y) {
        value = null
        val bill = repo.lightBill(y, m)
        val rs = repo.lightReadings(y, m).associateBy { it.roomId }
        val names = LIGHT_ROOMS.associateWith { repo.nameFor(it, y, m) }
        value = LightSummary(bill, rs, names)
    }
    val d = data ?: return
    val bill = d.bill
    if (bill == null) {
        Text("No Data")
        return
    }
    val tu = bill.totalUnits ?: 0.0
    val tp = bill.totalAmount ?: 0.0
    val res = calcLight(
        tu, tp,
        LIGHT_ROOMS.associateWith { d.readings[it]?.currentUnit ?: 0.0 },
        LIGHT_ROOMS.associateWith { d.readings[it]?.previousUnit ?: 0.0 }
    )
    val pu = res.perUnit
    for (r in listOf("A", "B", "C")) {
        val u = res.rooms.getValue(r).units
        val paid = d.readings[r]?.paid == true
        Text(roomLabel(d.names[r], r), fontWeight = FontWeight.Bold)
        Text("${fmt(u)} Units → ₹${money2(u * pu)}")
        Text(
            "Status: " + if (paid) "Paid" else "Unpaid",
            color = if (paid) GreenText else RedText, fontWeight = FontWeight.Bold
        )
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
    }
    val du = res.rooms.getValue("D").units
    Text(roomLabel(d.names["D"], "D"), fontWeight = FontWeight.Bold, color = Color(0xFFF97316))
    Text("${fmt(du)} Units → ₹${money2(du * pu)}", color = Color(0xFFF97316))
}

// =============================== RENT ===============================

@Composable
fun RentScreen(vm: AppViewModel) {
    val f = vm.rent
    val repo = vm.repo
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var showView by remember { mutableStateOf(false) }

    LaunchedEffect(f.month, f.year) {
        val key = f.year * 12 + f.month
        if (f.dataKey != key) {
            val rs = repo.rents(f.year, f.month).associateBy { it.roomId }
            for (r in RENT_ROOMS) f.rents[r] = rs[r]?.rent.toInput()
            f.dataKey = key
        }
    }
    LaunchedEffect(f.month, f.year, vm.namesVersion) {
        val stamp = Pair(f.year * 12 + f.month, vm.namesVersion)
        if (f.nameStamp != stamp) {
            for (r in RENT_ROOMS) f.names[r] = repo.nameFor(r, f.year, f.month)
            f.nameStamp = stamp
        }
    }

    ScreenScaffold("Rent Manager", RentColor) {
        AppCard { MonthYearRow(f.month, f.year) { m, y -> f.month = m; f.year = y } }
        AppCard {
            SectionTitle("Rooms Rent")
            for (r in RENT_ROOMS) {
                RoomBox {
                    Text("Room $r", fontWeight = FontWeight.Bold)
                    NameField(f.names[r] ?: "") { f.names[r] = it }
                    NumField("Rent ₹", f.rents[r] ?: "", { f.rents[r] = it })
                    // Same rule as the original: any rent amount > 0 means Paid.
                    val paid = num(f.rents[r] ?: "") > 0
                    StatusChip(if (paid) "Paid" else "Unpaid", paid)
                }
            }
        }
        AppCard {
            OutlinedButton(
                onClick = {
                    val y = f.year
                    val m = f.month
                    scope.launch {
                        val recs = RENT_ROOMS.map { r -> RentRecord(y, m, r, (f.rents[r] ?: "").toAmount()) }
                        repo.saveRent(y, m, recs, RENT_ROOMS.associateWith { f.names[it] ?: "" })
                        vm.namesVersion++
                        Toast.makeText(ctx, "Rent Saved", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("💾 Save Rent") }
            OutlinedButton(onClick = { showView = true }, modifier = Modifier.fillMaxWidth()) { Text("👁 View Data") }
        }
    }

    if (showView) {
        SummaryDialog("Rent Summary", true, f.month, f.year, { showView = false }) { m, y ->
            RentSummaryBody(repo, m, y)
        }
    }
}

private class RentSummary(val records: Map<String, RentRecord>, val names: Map<String, String>)

@Composable
private fun RentSummaryBody(repo: Repository, m: Int, y: Int) {
    val data by produceState<RentSummary?>(null, m, y) {
        value = null
        val rs = repo.rents(y, m).associateBy { it.roomId }
        value = RentSummary(rs, RENT_ROOMS.associateWith { repo.nameFor(it, y, m) })
    }
    val d = data ?: return
    if (d.records.isEmpty()) {
        Text("No Data")
        return
    }
    for (r in RENT_ROOMS) {
        val rent = d.records[r]?.rent ?: 0.0
        val paid = rent > 0
        Text(roomLabel(d.names[r], r), fontWeight = FontWeight.Bold)
        Text("Rent: ₹${fmt(rent)}")
        Text(if (paid) "Paid" else "Unpaid", color = if (paid) GreenText else RedText, fontWeight = FontWeight.Bold)
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
    }
}

// =============================== DEPOSIT ===============================

@Composable
fun DepositScreen(vm: AppViewModel) {
    val f = vm.deposit
    val repo = vm.repo
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var showView by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!f.loaded) {
            val saved = repo.deposits().associateBy { it.roomId }
            for (r in RENT_ROOMS) {
                val d = saved[r]
                // No saved deposit yet: offer this month's tenant name as a starting point.
                f.names[r] = if (d != null) d.name else repo.nameFor(r, currentYear(), currentMonth())
                f.amounts[r] = d?.amount.toInput()
            }
            f.loaded = true
        }
    }

    ScreenScaffold("Deposit Manager", DepositColor) {
        AppCard {
            SectionTitle("Rooms Deposit")
            for (r in RENT_ROOMS) {
                RoomBox {
                    Text("Room $r", fontWeight = FontWeight.Bold)
                    NameField(f.names[r] ?: "") { f.names[r] = it }
                    NumField("Deposit ₹", f.amounts[r] ?: "", { f.amounts[r] = it })
                    val received = num(f.amounts[r] ?: "") > 0
                    StatusChip(if (received) "Received" else "Pending", received)
                }
            }
        }
        AppCard {
            OutlinedButton(
                onClick = {
                    scope.launch {
                        val list = RENT_ROOMS.map { r ->
                            Deposit(r, (f.names[r] ?: "").trim(), (f.amounts[r] ?: "").toAmount())
                        }
                        repo.saveDeposits(list)
                        Toast.makeText(ctx, "Deposit Saved", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("💾 Save Deposit") }
            OutlinedButton(onClick = { showView = true }, modifier = Modifier.fillMaxWidth()) { Text("👁 View Data") }
        }
    }

    if (showView) {
        SummaryDialog("Deposit Summary", false, 1, 2024, { showView = false }) { _, _ ->
            DepositSummaryBody(repo)
        }
    }
}

@Composable
private fun DepositSummaryBody(repo: Repository) {
    val data by produceState<Map<String, Deposit>?>(null) {
        value = repo.deposits().associateBy { it.roomId }
    }
    val d = data ?: return
    if (d.isEmpty()) {
        Text("No Data")
        return
    }
    for (r in RENT_ROOMS) {
        val dep = d[r]
        val amt = dep?.amount ?: 0.0
        val received = amt > 0
        Text(roomLabel(dep?.name, r), fontWeight = FontWeight.Bold)
        Text("Deposit: ₹${fmt(amt)}")
        Text(
            if (received) "Received" else "Pending",
            color = if (received) GreenText else RedText, fontWeight = FontWeight.Bold
        )
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
    }
}

// =============================== DUE ===============================

@Composable
fun DueScreen(vm: AppViewModel) {
    val repo = vm.repo
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var room by rememberSaveable { mutableIntStateOf(0) }
    var type by rememberSaveable { mutableIntStateOf(0) }
    var amt by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    val dues by remember { repo.dues() }.collectAsState(initial = emptyList())

    ScreenScaffold("Due Summary", DueColor) {
        AppCard {
            SectionTitle("Add Due")
            Dropdown(DUE_ROOMS, room, { room = it })
            Dropdown(DUE_TYPES, type, { type = it })
            NumField("Due Amount ₹", amt, { amt = it })
            OutlinedTextField(
                value = note, onValueChange = { note = it },
                label = { Text("Note (optional)") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    val a = amt.toAmount()
                    if (a == null) {
                        Toast.makeText(ctx, "Enter amount", Toast.LENGTH_SHORT).show()
                    } else {
                        scope.launch {
                            repo.addDue(DUE_ROOMS[room], DUE_TYPES[type], a, note.trim())
                            amt = ""
                            note = ""
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DueColor),
                modifier = Modifier.fillMaxWidth()
            ) { Text("➕ Add Due") }
        }
        AppCard {
            SectionTitle("Pending Dues")
            if (dues.isEmpty()) {
                Text("No pending dues 🎉")
            } else {
                for (d in dues) {
                    RoomBox {
                        Text("${d.room} - ${d.type}", fontWeight = FontWeight.Bold)
                        Text("₹ ${fmt(d.amount)}", color = RedText, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text("${d.note} (${d.date})", fontSize = 12.sp)
                        OutlinedButton(
                            onClick = { scope.launch { repo.receiveDue(d.id) } },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Mark Received", color = GreenText) }
                    }
                }
            }
        }
    }
}
