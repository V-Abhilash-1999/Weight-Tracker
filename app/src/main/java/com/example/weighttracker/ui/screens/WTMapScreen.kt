package com.example.weighttracker.ui.screens

import androidx.compose.runtime.Composable
import com.example.weighttracker.ui.layout.wtlayout.WTBackgroundScreen
import com.example.weighttracker.viewmodel.WTViewModel
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun WTMapScreen(
    viewModel: WTViewModel
) {
    WTBackgroundScreen {
    }
}