package com.abhilash.weighttracker.chart.chart.ui

import androidx.compose.runtime.MutableState
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.*
import com.abhilash.weighttracker.chart.chart.data.*
import com.abhilash.weighttracker.chart.chart.ui.layout.ZDChartScreenLayout
import com.abhilash.weighttracker.chart.chart.ui.layout.getZeroOnEmpty
import com.abhilash.weighttracker.chart.chart.utils.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Combined Bar Chart is the combination of both line and bar chart.
 *
 * @param [dataSpacing] Spacing between each bar points in line
 * @param [xLabelOrientation] Angle at which the xLabels to be placed
 * @param [yLabelWidth] Width of y labels
 * @param [backgroundStyle] Styles on background like
 *          background color,
 *          background line color and
 *          background line path effect
 * @param [barSize] Size attributes for bar such as
 *                bar width,
 *                spacing between each stack and
 *                spacing between group
 *
 * @param [lineChartStyle] Styles applied to the line like
 *          line width,
 *          gradient,
 *          points in the line and
 *          whether the line should br curved or not
 * @param [labelTextStyles] Text Styles for both x and y labels
 * @param [chartPaddingValues] Padding to be applied to chart from x and y labels
 * @param [animationTimeMillis] Duration of animation
 * @param [animationDelayMillis] Initial delay of animation
 */


@Composable
fun ZDCombinedBarChart(
    modifier: Modifier = Modifier,
    barData: List<ZDBarData>,
    lineData: List<ZDLineChartData>,
    dataSpacing: Dp = 50.dp,
    xLabelOrientation: ZDXLabelOrientation = ZDXLabelOrientation.STRAIGHT,
    yLabelWidth: Dp = 0.dp,
    backgroundStyle: ZDBackgroundStyle = ZDBackgroundStyle(),
    barSize: ZDBarSize = ZDBarSize(),
    lineChartStyle: ZDLineChartStyle = ZDLineChartStyle(),
    labelTextStyles: ZDLabelTextStyles = ZDLabelTextStyles(),
    valueTextStyle: TextStyle? = null,
    chartPaddingValues: ZDChartPaddingValues = ZDChartPaddingValues(
        chartPaddingYAxis = 8.dp
    ),
    animationTimeMillis: Int = 0,
    animationDelayMillis: Int = 0
) {
    if(barData.isNotEmpty() || lineData.isNotEmpty()) {
        val max = rememberSaveable {
            mutableStateOf(
                kotlin.math.max(
                    barData.getMaxSum(),
                    lineData.getMaxSum()
                )
            )
        }

        val verticalShiftProperty =
            ZDVerticalShiftProperty(
                0f,
                max.value
            )
        val scrollState = rememberScrollState()

        val width = max(
            barData.getWidth(spacing = dataSpacing + barSize.barWidth),
            lineData.getWidth(spacing = dataSpacing + barSize.barWidth, removeBarWidth = dataSpacing)
        )

        val rect = Rect()
        val topPadding = with(LocalDensity.current) {
            val paint = Paint()
            paint.textSize = if(valueTextStyle?.fontSize?.isSpecified == true) {
                valueTextStyle.fontSize.toPx()
            } else {
                16.sp.toPx()
            }
            paint.getTextBounds("I", 0, 1, rect)
            rect.height().toDp()
        }

        ZDChartScreenLayout(
            modifier = modifier,
            xLabelOrientation = xLabelOrientation,
            backgroundStyle = backgroundStyle,
            verticalShiftProperty = verticalShiftProperty,
            chartWidth = width,
            chartTopPadding = topPadding,
            xAxisLabels = {
                if(labelTextStyles.showBottomLabel) {
                    var list = barData.getMaxLabelList()
                    val lineList = lineData.getMaxLabelList()

                    if (list.size < lineList.size)
                        list = lineList

                    XLabelsList(
                        list = list,
                        xLabelTextStyle = labelTextStyles.bottomLabelTextStyle,
                        maxLines = labelTextStyles.maxBottomLines,
                        dataSpacing = dataSpacing,
                        xLabelOrientation = xLabelOrientation
                    )
                }
            },
            yAxisLabels = {
                if(labelTextStyles.showDataLabel) {
                    val list = verticalShiftProperty.getYAxisLabels()
                    YLabelsList(
                        list = list,
                        yAxisWidth = yLabelWidth,
                        chartPaddingYAxis = chartPaddingValues.chartPaddingYAxis,
                        yLabelTextStyle = labelTextStyles.dataLabelTextStyle
                    )
                }
            },
            dataSpacing = dataSpacing,
            barWidth = barSize.barWidth,
            chartBottomPadding = chartPaddingValues.chartPaddingXAxis,
            scrollState = scrollState,
        ) {
            CombinedBarSegment(
                lineData = lineData,
                barData = barData,
                animationDuration = animationTimeMillis,
                animationDelay = animationDelayMillis,
                max = max,
                rect = rect,
                barWidth = barSize.barWidth,
                barTopSpacing = barSize.stackSpacing,
                barGroupSpacing = barSize.groupSpacing,
                dataSpacing = dataSpacing,
                verticalShiftProperty = verticalShiftProperty,
                lineStyle = lineChartStyle.lineStyle,
                lineWidth = lineChartStyle.lineWidth,
                gradient = lineChartStyle.gradientStyle,
                dataPointStyle = lineChartStyle.dataPointStyle,
                backgroundColor = backgroundStyle.backgroundColor,
                valueTextStyle = valueTextStyle,
            )
        }
    }
}

@Composable
private fun CombinedBarSegment(
    lineData: List<ZDLineChartData>,
    barData: List<ZDBarData>,
    animationDuration: Int,
    animationDelay: Int,
    max: MutableState<Float>,
    rect: Rect,
    barWidth: Dp,
    barTopSpacing: Dp,
    dataSpacing: Dp,
    barGroupSpacing: Dp,
    verticalShiftProperty: ZDVerticalShiftProperty,
    lineStyle: ZDLineStyle,
    gradient: ZDGradientStyle?,
    lineWidth: Dp,
    dataPointStyle: ZDDataPointStyle?,
    backgroundColor: Color,
    valueTextStyle: TextStyle?,
) {
    val barDataHolderSaver = remember {
        getSaver<ZDBarData> {
            Holder(it)
        }
    }
    val progressedBarData = rememberSaveable(stateSaver = barDataHolderSaver) {
        mutableStateOf(value = Holder(barData))
    }

    val lineDataHolderSaver = remember {
        getSaver<ZDLineChartData> {
            Holder(it)
        }
    }
    val progressedLineData = rememberSaveable(stateSaver = lineDataHolderSaver) {
        mutableStateOf(value = Holder(lineData))
    }

    val progressState = rememberSaveable { mutableStateOf(false) }

    updateBarData(
        lineData = lineData,
        barData = barData,
        savedBarData = progressedBarData,
        savedLineData = progressedLineData,
        max = max,
        progressState = progressState
    )

    updateLineData(
        chartData = lineData,
        animationMap = mutableMapOf(),
        data = progressedLineData,
        maxState = null,
        minMaxValue = null
    )

    val lineProgressMap = progressedLineData.value.bandwidthDataList.associateWith {
        rememberSaveable{ mutableStateOf(0f) }
    }.toMutableMap()

    val emptyLineChartData = remember {
        ZDLineChartData(
            color = Color.Transparent,
            dataValues = listOf()
        )
    }

    val lineAnimationMap = remember {
        progressedLineData.value.bandwidthDataList.associateWith { null }
            .toMutableMap<ZDLineChartData, Boolean?>()
    }

    val lineAnimatedValueMap : Map<ZDLineChartData, State<Float>> = getLineAnimatedMap(
        progressedLineData = progressedLineData,
        progressMap = lineProgressMap,
        animationDuration = animationDuration,
        animationDelay = animationDelay,
        animationMap = lineAnimationMap,
        ZDLineData = lineData,
        emptyBandwidthData = emptyLineChartData,
        maxState = null,
        minMaxValue = null
    )

    CombinedBarChartCanvas(
        barData = barData,
        lineData = lineData,
        savedLineData = progressedLineData,
        animationDelay = animationDelay,
        animationDuration = animationDuration,
        rect = rect,
        barWidth = barWidth,
        stackSpacing = barTopSpacing,
        groupSpacing = barGroupSpacing,
        dataSpacing = dataSpacing,
        verticalShiftProperty = verticalShiftProperty,
        lineStyle = lineStyle,
        lineWidth = lineWidth,
        gradient = gradient,
        dataPointStyle = dataPointStyle,
        backgroundColor = backgroundColor,
        lineProgressMap = lineProgressMap,
        lineAnimationMap = lineAnimationMap,
        lineAnimationValueMap = lineAnimatedValueMap,
        valueTextStyle = valueTextStyle,
        progressState = progressState,
        savedBarData = progressedBarData
    )
}

@Composable
private fun CombinedBarChartCanvas(
    barData: List<ZDBarData>,
    lineData: List<ZDLineChartData>,
    savedLineData: MutableState<Holder<ZDLineChartData>>,
    animationDelay: Int,
    animationDuration: Int,
    rect: Rect,
    barWidth: Dp,
    stackSpacing: Dp,
    groupSpacing: Dp,
    dataSpacing: Dp,
    verticalShiftProperty: ZDVerticalShiftProperty,
    lineStyle: ZDLineStyle,
    lineWidth: Dp,
    gradient: ZDGradientStyle?,
    dataPointStyle: ZDDataPointStyle?,
    lineProgressMap: MutableMap<ZDLineChartData, MutableState<Float>>,
    lineAnimationMap: MutableMap<ZDLineChartData, Boolean?>,
    lineAnimationValueMap: Map<ZDLineChartData, State<Float>>,
    backgroundColor: Color,
    valueTextStyle: TextStyle?,
    progressState: MutableState<Boolean>,
    savedBarData: MutableState<Holder<ZDBarData>>,
) {
    val coroutineScope = rememberCoroutineScope()

    val labelList = when {
        barData.isNotEmpty() -> barData.map { it.xLabel }
        lineData.isNotEmpty() -> lineData.getMaxLabelList()
        else -> listOf()
    }
    
    var initialValue by rememberSaveable {
        mutableStateOf(0f)
    }

    val barAnimationMap = getBarAnimatedMap(labelList = labelList, initialValue = initialValue)

    val barAnimationList = barAnimationMap.values.toList()

     DisposableEffect(key1 = barData) {
         if(progressState.value) {
             initialValue = 0f
             progressState.value = false
         }

         coroutineScope.launch {
            if(initialValue == 0f) {
                val job = async {
                    barAnimationList.forEach {
                        it.snapTo(0f)
                    }
                }

                job.invokeOnCompletion {
                    savedBarData.value = Holder(barData)

                    barAnimationList.forEach { animatable ->
                        launch {
                            animatable.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(
                                    durationMillis = animationDuration,
                                    delayMillis = animationDelay,
                                    easing = LinearEasing
                                )
                            )
                        }
                    }
                }
             }
         }

         onDispose {
             initialValue = 1f
         }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val canvasHeight = size.height

        val groupSpacingPx = groupSpacing.toPx()
        val stackSpacingPx = stackSpacing.toPx()
        val barWidthPx = barWidth.toPx()
        val spacing = dataSpacing.toPx()

        val groupSize = barData.getZeroOnEmpty { maxOf { it.dataValues.size } }
        val heightSpacing = canvasHeight / verticalShiftProperty.maxShiftValue

        val textPaint = valueTextStyle?.let { valueTextStyle ->
            Paint().apply {
                this.textSize = if(valueTextStyle.fontSize.isSpecified){
                    valueTextStyle.fontSize.toPx()
                } else {
                    16.sp.toPx()
                }
                color = valueTextStyle.color.toArgb()
                textAlign = Paint.Align.LEFT
            }
        }

        if(savedBarData.barData == barData) {
            barData.forEachIndexed { index, barData ->
                drawBar(
                    barData = barData,
                    index = index,
                    barWidth = barWidthPx,
                    groupSpacing = groupSpacingPx,
                    stackSpacing = stackSpacingPx,
                    heightSpacing = heightSpacing,
                    rect = rect,
                    animatedValue = barAnimationMap[barData.xLabel]?.value ?: 0f,
                    spacing = spacing,
                    groupSize = groupSize,
                    textPaint = textPaint,
                    textTopSpacing = BAR_TOP_SPACING.toPx()
                )
            }

            savedLineData.lineChartData.forEach { lineChartData ->
                drawLineChart(
                    lineChartData = lineChartData,
                    chartData = lineData,
                    verticalShiftProperty = verticalShiftProperty,
                    lineStyle = lineStyle,
                    lineWidth = lineWidth,
                    gradient = gradient,
                    dataPointStyle = dataPointStyle,
                    backgroundColor = backgroundColor,
                    xAxisSpacing = (dataSpacing + barWidth),
                    progressMap = lineProgressMap,
                    animationMap = lineAnimationMap,
                    animatedValueMap = lineAnimationValueMap,
                    startOffset = barWidthPx / 2
                )
            }
        }
    }
}

private fun updateBarData(
    lineData:List<ZDLineChartData> = listOf(),
    barData: List<ZDBarData>,
    savedBarData:MutableState<Holder<ZDBarData>>,
    savedLineData: MutableState<Holder<ZDLineChartData>>,
    progressState: MutableState<Boolean>,
    max:MutableState<Float>
){
    when {
        savedBarData.barData != barData -> {
            progressState.value = true
            max.value = kotlin.math.max(barData.getMaxSum(), lineData.getMaxSum())
        }
        savedLineData.lineChartData != lineData -> {
            max.value = kotlin.math.max(barData.getMaxSum(), lineData.getMaxSum())
        }
    }
}