package com.propertymanager.app

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.propertymanager.app.data.AppDatabase
import com.propertymanager.app.data.Repository

class AppViewModel(app: Application) : AndroidViewModel(app) {
    val repo = Repository(AppDatabase.get(app))
    val light = LightForm()
    val rent = RentForm()
    val deposit = DepositForm()

    /** Bumped after names are saved so other screens re-read the shared names. */
    var namesVersion by mutableIntStateOf(0)
}
