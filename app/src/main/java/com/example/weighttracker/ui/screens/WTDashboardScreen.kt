package com.example.weighttracker.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.ImageDecoderDecoder
import com.abhilash.weighttracker.chart.chart.data.*
import com.example.weighttracker.viewmodel.WTViewModel
import com.abhilash.weighttracker.chart.chart.ui.ZDLineChart
import com.example.weighttracker.R
import com.example.weighttracker.ui.util.*
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.delay

val mainColor = Color(253,155,117,255)
val mainColor1 = Color(250,226,217,255)
val mainColor2 = Color(249,244,244,255)
val mainColor3 = Color(249,245,242,255)

val lightTextColor = Color(190, 185, 183)

val colorList = listOf(mainColor, mainColor1, mainColor2, mainColor3)

val fabColor = Color(254,145,102)

val cardShape = RoundedCornerShape(16.dp)

//region content
@Destination(start = true)
@Composable
fun WTDashboardScreen(viewModel: WTViewModel) {
    val lifeCycleOwner = LocalLifecycleOwner.current

    val data = produceState(initialValue = listOf()) {
        viewModel.getData().observe(lifeCycleOwner) { dataList ->
            val dataValues = dataList.toLineDataValues()
            if(dataValues.all { it.value != 0f }) {
                value = listOf(
                    ZDLineChartData(
                        color = Color(243,182,158,255),
                        dataValues = dataList.toLineDataValues()
                    )
                )
            }
        }
    }
    Column(
        modifier = Modifier
            .screenBackground(rememberScrollState())
    ) {
        WeightCard(data = data)
        StatsSection(modifier = Modifier.padding(top = 32.dp))
    }
}

//region Weight Card
@Composable
private fun WeightCard(data: State<List<ZDLineChartData>>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .cardShadow()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            WeightCardHeader()

            val isDataEmpty = data.value.isEmpty() || data.value.all { it.dataValues.isEmpty() }

            if(isDataEmpty) {
                NoDataCard(false)
            } else {
                ChartCard(data)
            }
        }
    }
}

@Composable
private fun WeightCardHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
    ) {
        IconButton(
            onClick = {

            }
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color(140,135,132,255)
            )
        }

        Text(
            text = "WEIGHT",
            modifier = Modifier
                .weight(3f, true)
                .padding(top = 14.dp),
            textAlign = TextAlign.Center,
            color = Color(213,210,211,255)
        )


        IconButton(
            onClick = {

            }
        ) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "Menu",
                tint = Color(140,135,132,255)
            )
        }
    }
}

@Composable
private fun ChartCard(data: State<List<ZDLineChartData>>) {
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        data.value.lastOrNull()?.let {
            it.dataValues.lastOrNull()?.let { data ->
                Row(
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    Text(
                        modifier = Modifier
                            .alignByBaseline(),
                        text = "${data.value}",
                        fontSize = 32.sp,
                        color = Color.Black
                    )
                    Text(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .alignByBaseline(),
                        text = "kg",
                        fontSize = 14.sp,
                        color = Color(205, 203, 201, 255)
                    )
                }
            }
        }
        LineChart(data.value)
    }
}

@Composable
private fun LineChart(lineData: List<ZDLineChartData>) {
    val scrollState = remember { Animatable(0f) }
    val scrollTo = remember { mutableStateOf(0) }
    ZDLineChart(
        modifier = Modifier
            .height(300.dp)
            .fillMaxWidth(),
        chartData = lineData,
        lineChartStyle = ZDLineChartStyle(
            dataPointStyle = Outline(
                size = DatapointSize,
                color = Color(254,143,101,255).toArgb()
            ),
            lineWidth = 4.dp,
            lineStyle = ZDLineStyle.CURVED
        ),
        backgroundStyle = ZDBackgroundStyle(
            drawBgLines = false,
            backgroundColor = Color.Transparent//MaterialTheme.colors.background
        ),
        animationTimeMillis = 750,
        dataSpacing = 75.dp,
        labelTextStyles = ZDLabelTextStyles(
            showBottomLabel = true,
            bottomLabelTextStyle = TextStyle(
                color =  Color(140,135,132,255)
            ),
            showDataLabel = true
        ),
        scrollStateAnimatable = ZDChartScrollState(scrollState, scrollTo),
        calculateMaxValue =  {
            it * 1.15f
        }
    )


    LaunchedEffect(Unit) {
        delay(200)
        scrollState.lowerBound?.let { lowerBound ->
            scrollState.animateTo(
                lowerBound,
                animationSpec = TweenSpec(
                    easing = FastOutSlowInEasing,
                    durationMillis = 750
                )
            )
        }
    }
}

@Composable
private fun NoDataCard(isSignedIn: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
        ) {
            Text(
                text = "0",
                fontSize = 32.sp,
                color = Color.Black
            )
            Text(
                modifier = Modifier
                    .align(Alignment.Bottom)
                    .padding(bottom = 6.dp, start = 8.dp),
                text = "kg",
                fontSize = 14.sp,
                color = Color(205, 203, 201, 255)
            )
        }
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                val context = LocalContext.current
                val imgLoader = ImageLoader(context = context)
                    .newBuilder()
                    .components {
                        add(ImageDecoderDecoder.Factory())
                    }
                    .build()

                Image(
                    painter = rememberAsyncImagePainter(R.drawable.g_no_data, imgLoader),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            if(isSignedIn) {
                Text(text = "Try Signing in from a account")
            } else {
                Text(text = "Try Signing in from a different account")
            }
        }
    }
}
//endregion

//region Stats
@Composable
private fun StatsSection(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "This Week's Stats",
            color = Color.Black,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            AvgHeartRateCard(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(0.5f)
                    .cardShadow()
                    .background(Color.White)
            )

            WeeklyCaloriesCard(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
                    .cardShadow()
                    .background(Color.White)
            )
        }
    }
}

@Composable
private fun WeekStatsCard(
    modifier: Modifier = Modifier,
    headerText: String,
    headerIconRes: Int,
    value: String,
    unit: String
) {
    Column(
        modifier = modifier
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f),
                text = headerText.uppercase(),
                color = lightTextColor,
                fontSize = 14.sp
            )

            Image(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1.5f),
                painter = rememberAsyncImagePainter(headerIconRes),
                contentDescription = null
            )
        }

        Row(
            modifier = Modifier.padding(top = 16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                modifier = Modifier
                    .padding(end = 6.dp)
                    .alignByBaseline(),
                text = value,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                modifier = Modifier
                    .alignByBaseline(),
                text = unit,
                color = lightTextColor,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun AvgHeartRateCard(
    modifier: Modifier = Modifier
) {
    WeekStatsCard(
        modifier = modifier,
        headerText = "Avg.Heart Rate",
        headerIconRes = R.drawable.ic_wt_heart,
        value = "77",
        unit = "BPM"
    )
}

@Composable
private fun WeeklyCaloriesCard(
    modifier: Modifier = Modifier
) {
    WeekStatsCard(
        modifier = modifier,
        headerText = "Weekly Calories",
        headerIconRes = R.drawable.ic_wt_fire,
        value = "2.5K",
        unit = "CAL"
    )
}

@Composable
fun WeightLossCard() {
    Column(
        modifier = Modifier
            .height(70.dp)
            .width(200.dp)
            .clip(cardShape)
            .cardShadow()
            .wavyBackground(
                waveColor = Color(254, 155, 118, 255),
                background = Color(255, 144, 102, 255)
            )
    ) {

    }
}

//endregion

//endregion

//ABHI Bounce Line Chart