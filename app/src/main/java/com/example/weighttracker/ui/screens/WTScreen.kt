package com.example.weighttracker.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.weighttracker.destinations.WTWeightInserterDestination
import com.example.weighttracker.ui.navigation.WTBottomBarDestination
import com.example.weighttracker.ui.navigation.WTNavGraph
import com.example.weighttracker.ui.theme.Language
import com.example.weighttracker.ui.theme.LocalLanguage
import com.example.weighttracker.ui.theme.LocalLanguageStrings
import com.example.weighttracker.ui.util.sizeInDp
import com.example.weighttracker.viewmodel.WTViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.navigation.navigateTo
import com.ramcosta.composedestinations.spec.NavHostEngine

@Composable
fun WTScreen(
    navHostEngine: NavHostEngine,
    navController: NavHostController,
    viewModel: WTViewModel,
    activity: Activity
) {
    val currentLanguage = remember { mutableStateOf(Language.ENGLISH) }
    CompositionLocalProvider(
        LocalLanguage provides currentLanguage.value
    ) {
        val localStrings = LocalLanguageStrings.current
        val systemUiController = rememberSystemUiController()
        systemUiController.setStatusBarColor(mainColor)
        systemUiController.setNavigationBarColor(Color.White)
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BottomBar(navController)
            },
            floatingActionButtonPosition = FabPosition.Center,
            isFloatingActionButtonDocked = true,
            floatingActionButton = {
                FloatingActionButton(
                    modifier = Modifier
                        .size(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    onClick = {
                        navController.navigateTo(WTWeightInserterDestination)
                        /*
                            currentLanguage.value = when(currentLanguage.value) {
                                Language.ENGLISH -> {
                                    Language.TAMIL
                                }
                                Language.TAMIL -> {
                                    Language.MALAYALAM
                                }
                                Language.MALAYALAM -> {
                                    Language.ENGLISH
                                }
                            }
                        */
                    },
                    backgroundColor = fabColor
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Data"
                    )
                }
            }
        ) { padding ->
            DestinationsNavHost(
                modifier = Modifier.padding(padding),
                navGraph = WTNavGraph.content,
                engine = navHostEngine,
                navController = navController,
                dependenciesContainerBuilder = {
                    dependency(viewModel)
                    dependency(activity)
                }
            )
        }
    }
}


//region Bottom Bar
@Composable
fun BottomBar(navController: NavController) {
    BottomAppBar(
        modifier = Modifier
            .height(60.dp)
            .clip(RoundedCornerShape(15.dp, 15.dp, 0.dp, 0.dp)),
        cutoutShape = CircleShape,
        elevation = 16.dp,
        backgroundColor = Color.White,
        contentPadding = PaddingValues(12.dp, 0.dp, 12.dp, 0.dp)
    ) {
        BottomNavigation(
            backgroundColor = Color.White,
            elevation = 0.dp
        ) {
            val bottomBarDestinations = WTBottomBarDestination.values()
            val mid = bottomBarDestinations.size / 2
            var selectedIndex by rememberSaveable { mutableStateOf(0) }
            (0 until mid).forEach { index ->
                val destination = bottomBarDestinations[index]

                BottomNavItem(
                    navController = navController,
                    isSelected = index == selectedIndex,
                    destination = destination
                ) {
                    selectedIndex = index
                }
            }

            CutOutSpace()

            (mid until bottomBarDestinations.size).forEach { index ->
                val destination = bottomBarDestinations[index]

                BottomNavItem(
                    navController = navController,
                    isSelected = index == selectedIndex,
                    destination = destination
                ) {
                    selectedIndex = index
                }
            }
        }
    }
}

@Composable
fun RowScope.BottomNavItem(
    navController: NavController,
    isSelected: Boolean,
    destination: WTBottomBarDestination,
    setSelected: () -> Unit
) {
    BottomNavigationItem(
        selected = isSelected,
        onClick = {
            navController.navigateTo(destination.direction)
            setSelected()
        },
        selectedContentColor = destination.selectedColor,
        unselectedContentColor = Color.Black,
        icon = {
            val bottomIcon = rememberAsyncImagePainter(
                if(isSelected) destination.selectedIcon else destination.unSelectedIcon
            )

            Icon(
                modifier = Modifier.sizeInDp(24),
                painter = bottomIcon,
                contentDescription = "ICON"
            )
        }
    )
}

@Composable
fun RowScope.CutOutSpace() {
    BottomNavigationItem(
        icon = {},
        label = {},
        selected = false,
        onClick = {},
        enabled = false
    )
}
//endregion

