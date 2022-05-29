package com.example.weighttracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weighttracker.ui.theme.*
import com.example.weighttracker.ui.util.toLineDataValues
import com.example.weighttracker.viewmodel.WTViewModel
import com.abhilash.weighttracker.chart.chart.data.ZDBackgroundStyle
import com.abhilash.weighttracker.chart.chart.data.ZDLineChartData
import com.abhilash.weighttracker.chart.chart.ui.ZDLineChart
import com.example.weighttracker.ui.screens.destinations.WTWeightInserterDestination
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterialNavigationApi::class)
@Destination(start = true)
@Composable
fun WTDashboard(
    navController: DestinationsNavigator,
    viewModel: WTViewModel,
) {
    val currentLanguage = remember { mutableStateOf(Language.ENGLISH) }
    CompositionLocalProvider(
        LocalLanguage provides currentLanguage.value
    ) {
        val localStrings = LocalLanguageStrings.current
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(topBarText = localStrings.weightTracker)
            },
            bottomBar = {
                val doneByString = localStrings.doneBy + " " + localStrings.name
                BottomBar(bottomBarText = doneByString)
            },
            floatingActionButton = {
                Button(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(MaterialTheme.colors.onSecondary),
                    onClick = {
                        navController.navigate(WTWeightInserterDestination)
//                        currentLanguage.value = when(currentLanguage.value) {
//                            Language.ENGLISH -> {
//                                Language.TAMIL
//                            }
//                            Language.TAMIL -> {
//                                Language.MALAYALAM
//                            }
//                            Language.MALAYALAM -> {
//                                Language.ENGLISH
//                            }
//                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Data"
                    )
                }
            }
        ) {
            WTDashboardContentExtension(viewModel)
        }
    }
}

@Composable
fun TopAppBar(
    topBarText: String
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colors.primarySurface)
            .height(50.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = topBarText,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colors.onPrimary
        )
    }
}

@Composable
fun BottomBar(
    bottomBarText: String
) {
    Column {
        Box(
            modifier = Modifier
                .height(20.dp)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = gradientColors
                    )
                )
        )

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.LightGray)
        )

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp),
            text = bottomBarText,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center,
            fontSize = 12.sp
        )
    }
}


@Composable
fun WTDashboardContentExtension(viewModel: WTViewModel) {
    val lifeCycleOwner = LocalLifecycleOwner.current
    val lineColor = MaterialTheme.colors.onSecondary

    val data = produceState(initialValue = listOf()) {
        viewModel.getData().observe(lifeCycleOwner) { dataList ->
            value = listOf(
                ZDLineChartData(
                    color = lineColor,
                    dataValues = dataList.toLineDataValues()
                )
            )
        }
    }
    
    LineChart(data.value)
}


@Composable
fun LineChart(lineData: List<ZDLineChartData>) {
    ZDLineChart(
        modifier = Modifier
            .height(300.dp)
            .fillMaxWidth()
            .padding(16.dp),
        chartData = lineData,
        backgroundStyle = ZDBackgroundStyle(
            drawBgLines = true,
            backgroundColor = MaterialTheme.colors.background
        ),
        dataSpacing = 75.dp
    )
}