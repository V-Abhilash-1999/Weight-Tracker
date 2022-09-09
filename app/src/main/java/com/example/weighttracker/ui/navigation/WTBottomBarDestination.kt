package com.example.weighttracker.ui.navigation

import androidx.compose.ui.graphics.Color
import com.example.weighttracker.R
import com.example.weighttracker.destinations.*
import com.example.weighttracker.ui.screens.mainColor
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec

enum class WTBottomBarDestination(
    val direction: DirectionDestinationSpec,
    val selectedColor: Color,
    val selectedIcon: Int,
    val unSelectedIcon: Int
) {
    Home(WTDashboardScreenDestination, Color.Unspecified, R.drawable.ic_wt_house_filled, R.drawable.ic_wt_house_outline),
    Weight(WTDashboardScreenDestination, mainColor, R.drawable.ic_wt_dumbbell, R.drawable.ic_wt_dumbbell),
    Map(WTMapScreenDestination, mainColor, R.drawable.ic_wt_map_filled, R.drawable.ic_wt_map_outline),
    Profile(WTProfileScreenDestination, mainColor, R.drawable.ic_wt_person_filled, R.drawable.ic_wt_person_outline)
}