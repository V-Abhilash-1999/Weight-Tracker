package com.abhilash.weighttracker.chart.chart.ui

import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.*
import com.abhilash.weighttracker.chart.chart.data.*
import com.abhilash.weighttracker.chart.chart.data.ZDVerticalShiftProperty
import com.abhilash.weighttracker.chart.chart.ui.layout.ZDChartScreenLayout
import com.abhilash.weighttracker.chart.chart.utils.*
import kotlinx.coroutines.launch

/**
 * Bar Chart is the used to represent the data in bar.
 * Text on top of bars will only be written in case of single group.
 *
 * @param [data] The outer list is used to represent group and
 *               The inner list is used to represent stack in each group
 * @param [dataSpacing] Spacing between each bar points in line
 * @param [barSize] Size attributes for bar such as
 *                bar width,
 *                spacing between each stack and
 *                spacing between group
 * @param [xLabelOrientation] Angle at which the xLabels to be placed
 * @param [yLabelWidth] Width of y labels
 * @param [animationTimeMillis] Duration of animation
 * @param [animationDelayMillis] Initial delay of animation
 * @param [backgroundStyle] Styles on background like
 *          background color,
 *          background line color and
 *          background line path effect
 * @param [chartPaddingValues] Padding to be applied to chart from x and y labels
 * @param [labelTextStyles] Text Styles for both x and y labels
 * @param [valueTextStyle] Text style applied to value on top of canvas
 */

@Composable
fun ZDBarChart(
    modifier: Modifier = Modifier,
    data: List<ZDBarData>,
    dataSpacing: Dp = 24.dp,
    barSize: ZDBarSize = ZDBarSize(),
    xLabelOrientation: ZDXLabelOrientation = ZDXLabelOrientation.STRAIGHT,
    yLabelWidth: Dp = 0.dp,
    animationTimeMillis:Int = 0,
    animationDelayMillis:Int = 0,
    backgroundStyle: ZDBackgroundStyle = ZDBackgroundStyle(),
    chartPaddingValues: ZDChartPaddingValues = ZDChartPaddingValues(
        chartPaddingXAxis = 8.dp,
        chartPaddingYAxis = 16.dp
    ),
    labelTextStyles: ZDLabelTextStyles = ZDLabelTextStyles(
        bottomLabelTextStyle = LocalTextStyle.current,
        dataLabelTextStyle = LocalTextStyle.current,
    ),
    valueTextStyle: TextStyle? = null
) {
    val max = rememberSaveable { mutableStateOf(data.getMaxSum()) }
    val verticalShiftProperty = ZDVerticalShiftProperty(0f, max.value)

    val width = data.getWidth(barSize.barWidth + dataSpacing)

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
        dataSpacing = dataSpacing,
        chartWidth = width,
        barWidth = barSize.barWidth,
        chartTopPadding = topPadding,
        chartBottomPadding = chartPaddingValues.chartPaddingXAxis,
        xAxisLabels = {
             if(labelTextStyles.showBottomLabel) {
                val list = data.getMaxLabelList()
                XLabelsList(
                    list = list,
                    xLabelTextStyle = labelTextStyles.bottomLabelTextStyle,
                    maxLines = labelTextStyles.maxBottomLines,
                    xLabelOrientation = xLabelOrientation,
                    dataSpacing = (barSize.barWidth + dataSpacing)
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
        }
    ) {
        BarChartSegment(
            data = data,
            barSize = barSize,
            dataSpacing = dataSpacing,
            verticalShiftProperty = verticalShiftProperty,
            animationDuration = animationTimeMillis,
            animationDelay = animationDelayMillis,
            max = max,
            rect = rect,
            valueTextStyle = valueTextStyle
        )
    }
}

@Composable
private fun BarChartSegment(
    data: List<ZDBarData>,
    barSize: ZDBarSize,
    dataSpacing: Dp,
    verticalShiftProperty: ZDVerticalShiftProperty,
    max:MutableState<Float>,
    animationDuration: Int,
    animationDelay: Int,
    rect: Rect,
    valueTextStyle: TextStyle?,
) {
    val saver = getSaver<ZDBarData> {
        Holder(it)
    }

    val savedData = rememberSaveable(stateSaver = saver) {
        mutableStateOf(value = Holder(data))
    }

    val progressState = rememberSaveable { mutableStateOf(false) }

    updateBarData(
        data = data,
        savedData = savedData,
        max = max,
        progressState = progressState,
    )

    BarChartAnimatedCanvas(
        dataList = savedData.barData,
        verticalShiftProperty = verticalShiftProperty,
        animationDuration = animationDuration,
        animationDelay = animationDelay,
        progressState = progressState,
        rect = rect,
        barSize = barSize,
        dataSpacing = dataSpacing,
        valueTextStyle = valueTextStyle
    )
}

@Composable
private fun BarChartAnimatedCanvas(
    dataList: List<ZDBarData>,
    verticalShiftProperty: ZDVerticalShiftProperty,
    animationDuration: Int,
    animationDelay: Int,
    progressState: MutableState<Boolean>,
    rect: Rect,
    barSize: ZDBarSize,
    dataSpacing: Dp,
    valueTextStyle: TextStyle?
) {
    val labelList = dataList.getMaxLabelList()
    val coroutineScope = rememberCoroutineScope()

    var initialValue by rememberSaveable {
        mutableStateOf(0f)
    }

    val animationMap = getBarAnimatedMap(labelList = labelList, initialValue = initialValue)

    val animationList = animationMap.values.toList()

    DisposableEffect(key1 = dataList) {
        if(progressState.value) {
            initialValue = 0f
            progressState.value = false
        }

        if(initialValue == 0f) {
            animationList.forEach {
                coroutineScope.launch {
                    it.snapTo(0f)
                }
            }

            coroutineScope.launch {
                animationList.forEach { animatable ->
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

        onDispose {
            initialValue = 1f
        }
    }

    BarChartCanvas(
        dataList = dataList,
        animationMap = animationMap,
        verticalShiftProperty = verticalShiftProperty,
        rect = rect,
        barSize = barSize,
        dataSpacing = dataSpacing,
        valueTextStyle = valueTextStyle
    )
}

@Composable
private fun BarChartCanvas(
    dataList: List<ZDBarData>,
    animationMap: Map<String, Animatable<Float, AnimationVector1D>>,
    verticalShiftProperty: ZDVerticalShiftProperty,
    rect: Rect,
    barSize: ZDBarSize,
    dataSpacing: Dp,
    valueTextStyle: TextStyle?
) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val canvasHeight = size.height
        val barWidth = barSize.barWidth.toPx()
        val spacing = dataSpacing.toPx()

        val groupSpacing = barSize.groupSpacing.toPx()
        val stackSpacing = barSize.stackSpacing.toPx()

        val groupSize = dataList.maxOf { it.dataValues.size }
        val heightSpacing = canvasHeight / verticalShiftProperty.maxShiftValue

        val  textPaint = valueTextStyle?.let { valueTextStyle ->
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

        dataList.forEachIndexed { index, barData ->
            drawBar(
                barData = barData,
                index = index,
                groupSpacing = groupSpacing,
                stackSpacing = stackSpacing,
                heightSpacing = heightSpacing,
                barWidth = barWidth,
                spacing = spacing,
                groupSize = groupSize,
                rect = rect,
                animatedValue = animationMap[barData.xLabel]?.value ?: 1f,
                textPaint = textPaint,
                textTopSpacing = BAR_TOP_SPACING.toPx(),
            )
        }
    }
}

internal fun DrawScope.drawBar(
    barData: ZDBarData,
    index: Int,
    groupSpacing: Float,
    stackSpacing: Float,
    heightSpacing: Float,
    barWidth: Float,
    spacing: Float,
    groupSize: Int,
    rect: Rect,
    animatedValue: Float,
    textPaint: Paint? = null,
    textTopSpacing: Float? = null
) {
    val barSize = (barWidth - ((groupSize - 1) * groupSpacing)) / groupSize
    var x = (index * (spacing + barWidth)) - groupSpacing - barSize
    barData.dataValues.forEach { stackList ->
        x += groupSpacing + barSize
        var y = size.height
        val stackListSize = stackList.size
        var totalStackValue = 0f
        stackList.forEachIndexed { stackIndex, barDataValue ->
            totalStackValue += barDataValue.value
            val yOffset = if(stackIndex == 0) {
                0f
            } else {
                stackSpacing
            }
            val startY = y - yOffset
            val path = Path().apply {
                moveTo(x, startY)
                val stackOffset = if(stackListSize > 1) {
                    stackSpacing/2
                } else {
                    0f
                }
                val barHeight = (barDataValue.value * heightSpacing - stackOffset) * animatedValue
                y -= barHeight
                lineTo(x, y)
                lineTo(x + barSize, y)
                lineTo(x + barSize, startY)
                lineTo(x, startY)
            }
            drawPath(path = path, color = barDataValue.color)
        }

        textPaint?.let { textPaint ->
            if (groupSize == 1) {
                val text = "${totalStackValue.toInt()}"
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.getClipBounds(rect)
                    textPaint.getTextBounds(text, 0, text.length, rect)
                    if(stackList.isNotEmpty()) {
                        textPaint.color = stackList.last().color.toArgb()
                    }
                    val xPos = x + (barSize / 2f) - (rect.width() / 2f) - rect.left
                    canvas.nativeCanvas.drawText(text,
                        xPos,
                        y - (textTopSpacing ?: 0f),
                        textPaint,
                    )
                }
            }
        }
    }
}

private fun updateBarData(
    data: List<ZDBarData>,
    savedData: MutableState<Holder<ZDBarData>>,
    max: MutableState<Float>,
    progressState: MutableState<Boolean>,
) {
    if(!isEqual(data, savedData.barData)) {
        progressState.value = true
        savedData.value = Holder(data)
        max.value = data.getMaxSum()
    }
}