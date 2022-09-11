package com.example.weighttracker.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.vector.ImageVector

enum class WTUserInfo(
    val icon: ImageVector,
    val desc: String,
    val state: MutableState<Boolean> = mutableStateOf(false)
) {
    EMAIL(Icons.Filled.Email, "Add Email"),
    PHONE(Icons.Filled.Phone, "Add Phone No"),
    NOTES(Icons.Filled.DateRange, "Add Notes")
}