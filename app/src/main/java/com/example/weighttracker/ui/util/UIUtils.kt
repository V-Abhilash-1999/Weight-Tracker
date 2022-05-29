package com.example.weighttracker.ui.util

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext


@Composable
fun MakeToast(
    message: String,
    length: Int = Toast.LENGTH_SHORT
) {
    LocalContext.current.makeToast(
        message = message,
        length = length
    )
}
