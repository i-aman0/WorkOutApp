package com.example.workoutapp

import android.app.Application

class WorkOutApp: Application() {
    // here lazy means it adds the value to our variable only when it is needed, not directly
    val db by lazy {
        HistoryDatabase.getInstance(this)
    }
}