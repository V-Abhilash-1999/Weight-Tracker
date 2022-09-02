package com.example.weighttracker.ui.navigation

import com.example.weighttracker.R
import com.example.weighttracker.destinations.*
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec

enum class WTBottomBarDestination(
    val direction: DirectionDestinationSpec,
    val selectedIcon: Int,
    val unSelectedIcon: Int
) {
    Home(WTDashboardScreenDestination, R.drawable.ic_wt_house_filled, R.drawable.ic_wt_house_outline),
    Weight(WTDashboardScreenDestination, R.drawable.ic_wt_dumbbell, R.drawable.ic_wt_dumbbell),
    Map(WTMapScreenDestination, R.drawable.ic_wt_map_filled, R.drawable.ic_wt_map_outline),
    Profile(WTDashboardScreenDestination, R.drawable.ic_wt_person_filled, R.drawable.ic_wt_person_outline)
}