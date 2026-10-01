package com.propertymanager.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Screen state lives in the ViewModel so unsaved input survives tab switches and rotation. */
class LightForm {
    var month by mutableIntStateOf(currentMonth())
    var year by mutableIntStateOf(currentYear())
    var tu by mutableStateOf("")
    var tp by mutableStateOf("")
    val names = mutableStateMapOf<String, String>()
    val cur = mutableStateMapOf<String, String>()
    val prev = mutableStateMapOf<String, String>()
    val paid = mutableStateMapOf<String, Boolean>()
    var dataKey: Int? = null
    var nameStamp: Pair<Int, Int>? = null
}

class RentForm {
    var month by mutableIntStateOf(currentMonth())
    var year by mutableIntStateOf(currentYear())
    val names = mutableStateMapOf<String, String>()
    val rents = mutableStateMapOf<String, String>()
    var dataKey: Int? = null
    var nameStamp: Pair<Int, Int>? = null
}

class DepositForm {
    val names = mutableStateMapOf<String, String>()
    val amounts = mutableStateMapOf<String, String>()
    var loaded = false
}
