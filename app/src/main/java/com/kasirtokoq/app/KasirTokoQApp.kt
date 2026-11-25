package com.kasirtokoq.app

import android.app.Application
import com.kasirtokoq.app.data.local.AppDatabase

class KasirTokoQApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
}
