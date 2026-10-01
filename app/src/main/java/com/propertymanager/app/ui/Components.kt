package com.propertymanager.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.propertymanager.app.MONTHS
import com.propertymanager.app.YEARS

val LightColor = Color(0xFF2563EB)
val RentColor = Color(0xFF16A34A)
val DepositColor = Color(0xFF1E293B)
val DueColor = Color(0xFFB45309)
val GreenText = Color(0xFF16A34A)
val RedText = Color(0xFFDC2626)

@Composable
fun ScreenScaffold(title: String, color: Color, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Box(
            Modifier.fillMaxWidth().background(color).statusBarsPadding().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Column(
            Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
fun AppCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
fun SectionTitle(text: String) = Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold)

@Composable
fun RoomBox(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth()
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Composable
fun NameField(value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text("Name") },
        singleLine = true,
        trailingIcon = { Text("✏️") },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFE2E8F0),
            unfocusedContainerColor = Color(0xFFF1F5F9)
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

private val numRegex = Regex("^-?\\d*\\.?\\d*$")

@Composable
fun NumField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (numRegex.matches(it)) onChange(it) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun StatusChip(text: String, positive: Boolean, onClick: (() -> Unit)? = null) {
    val bg = if (positive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
    val fg = if (positive) Color(0xFF166534) else Color(0xFF991B1B)
    var m = Modifier.fillMaxWidth().background(bg, RoundedCornerShape(10.dp))
    if (onClick != null) m = m.clickable(onClick = onClick)
    Text(text, modifier = m.padding(10.dp), color = fg, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
}

@Composable
fun Badge(text: String) {
    Text(
        text,
        modifier = Modifier.fillMaxWidth().background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp)).padding(8.dp),
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center
    )
}

@Composable
fun Dropdown(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth()) {
            Text(options.getOrElse(selected) { "" } + " ▾")
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEachIndexed { i, o ->
                DropdownMenuItem(text = { Text(o) }, onClick = { open = false; onSelect(i) })
            }
        }
    }
}

@Composable
fun MonthYearRow(month: Int, year: Int, onChange: (Int, Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Dropdown(MONTHS, month - 1, { onChange(it + 1, year) }, Modifier.weight(1f))
        Dropdown(
            YEARS.map { it.toString() }, YEARS.indexOf(year).coerceAtLeast(0),
            { onChange(month, YEARS[it]) }, Modifier.weight(1f)
        )
    }
}

@Composable
fun SummaryDialog(
    title: String,
    showFilter: Boolean,
    initMonth: Int,
    initYear: Int,
    onDismiss: () -> Unit,
    body: @Composable (Int, Int) -> Unit
) {
    var m by remember { mutableIntStateOf(initMonth) }
    var y by remember { mutableIntStateOf(initYear) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text(title) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if (showFilter) {
                    MonthYearRow(m, y) { nm, ny -> m = nm; y = ny }
                    Spacer(Modifier.height(12.dp))
                }
                body(m, y)
            }
        }
    )
}
