package com.example.weighttracker.ui.navigation

import com.example.weighttracker.NavGraph
import com.example.weighttracker.destinations.*

object WTNavGraph {
    val content = NavGraph(
        route = "content",
        startRoute = WTDashboardScreenDestination,
        destinations = listOf(
            WTDashboardScreenDestination,
            WTWeightInserterDestination,
            WTMapScreenDestination,
            WTWeightScreenDestination,
            WTProfileScreenDestination,
            WTMobileVerificationCodeScreenDestination
        )
    )

    val logInScreen = NavGraph(
        route = "signIn",
        startRoute = WTSignInScreenDestination,
        destinations = listOf(
            WTSignInScreenDestination,
            WTResultScreenDestination,
            WTMobileSignInDestination,
            WTMobileVerificationCodeScreenDestination
        )
    )

    val initialScreens = NavGraph(
        route = "signIn",
        startRoute = WTLoggedOutScreenDestination,
        destinations = listOf(
            WTLoggedOutScreenDestination,
            WTLoggedInScreenDestination
        )
    )

}