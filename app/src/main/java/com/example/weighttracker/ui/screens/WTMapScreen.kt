package com.example.weighttracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.weighttracker.ui.util.WTBackgroundScreen
import com.example.weighttracker.ui.util.WTCard
import com.example.weighttracker.ui.util.cardShadow
import com.example.weighttracker.ui.util.screenBackground
import com.example.weighttracker.viewmodel.WTViewModel
import com.google.maps.android.compose.GoogleMap
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun WTMapScreen(
    viewModel: WTViewModel
) {
    WTBackgroundScreen {
    }
}