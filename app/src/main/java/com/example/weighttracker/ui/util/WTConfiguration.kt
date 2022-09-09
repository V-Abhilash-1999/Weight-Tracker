package com.example.weighttracker.ui.util

import android.util.Log

object WTConfiguration {
    private val isLogEnabled = true

    fun checkAndLog(message: String?, tag: String = "WeightTrackerLog") {
        if(isLogEnabled) {
            Log.e(tag, message.toString())
        }
    }
}