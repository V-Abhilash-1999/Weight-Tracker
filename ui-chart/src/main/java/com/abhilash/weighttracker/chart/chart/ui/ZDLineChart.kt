package com.abhilash.weighttracker.chart.chart.ui

import androidx.annotation.IntRange
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abhilash.weighttracker.chart.chart.data.*
import com.abhilash.weighttracker.chart.chart.data.Outline
import com.abhilash.weighttracker.chart.chart.ui.layout.MutableFloatState
import com.abhilash.weighttracker.chart.chart.ui.layout.ZDChartScreenLayout
import com.abhilash.weighttracker.chart.chart.utils.*

/**
 * Line Chart is the used to represent the flow of data in a line.
 *
 * @param [lineChartStyle] Styles applied to the line like
 *          line width (will be taken if data has 0 as line width),
 *          gradient,
 *          points in the line and
 *          whether the line should br curved or not (will be taken if data has null as line style)
 * @param [backgroundStyle] Styles on background like
 *          background color,
 *          background line color and
 *          background line path effect
 * @param [xLabelOrientation] Angle at which the xLabels to be placed
 * @param [animationTimeMillis] Duration of animation
 * @param [animationDelayMillis] Initial delay of animation
 * @param [dataSpacing] Spacing between each data points in line
 * @param [labelTextStyles] Text Styles for both x and y labels
 * @param [chartPaddingValues] Padding to be applied to chart from x and y labels
 * @param [yLabelWidth] Width of y labels
 */

@Composable
fun ZDLineChart(
    modifier: Modifier = Modifier,
    chartData: List<ZDLineChartData>,
    scrollStateAnimatable: ZDChartScrollState,
    lineChartStyle: ZDLineChartStyle = ZDLineChartStyle(),
    backgroundStyle: ZDBackgroundStyle = ZDBackgroundStyle(),
    xLabelOrientation: ZDXLabelOrientation = ZDXLabelOrientation.STRAIGHT,
    animationTimeMillis: Int = 0,
    animationDelayMillis: Int = 0,
    dataSpacing: Dp = 100.dp,
    labelTextStyles: ZDLabelTextStyles = ZDLabelTextStyles(bottomLabelTextStyle = LocalTextStyle.current, dataLabelTextStyle = LocalTextStyle.current),
    chartPaddingValues: ZDChartPaddingValues = ZDChartPaddingValues(
        chartPaddingYAxis = 8.dp
    ),
    yLabelWidth:Dp = 0.dp,
    calculateMaxValue: (Float) -> Float = { 0f }
) {
    if(chartData.isNotEmpty()) {
        val maxChartValue = chartData.getMaxSum()
        val minMaxValue = rememberSaveable { mutableStateOf(Pair(chartData.getMinVal(), maxChartValue)) }
        val maxValue = calculateMaxValue(maxChartValue)
        val verticalShiftProperty = ZDVerticalShiftProperty(
            mYMin = minMaxValue.value.first,
            mYMax = maxValue.takeIf { it > 0 } ?: minMaxValue.value.second,
            calculateMaxShiftValues = maxValue == 0f
        )
        val chartWidth = with(LocalDensity.current) { (chartData.getWidth(dataSpacing)).toPx() }

        ZDChartScreenLayout(
            xLabelOrientation = xLabelOrientation,
            modifier = modifier,
            scrollState = scrollStateAnimatable,
            xAxisLabels = {
                if(labelTextStyles.showBottomLabel) {
                    val list = chartData.getMaxLabelList()
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
                    val list = verticalShiftProperty.getYAxisLineLabels()
                    YLabelsList(
                        list = list,
                        yAxisWidth = yLabelWidth,
                        chartPaddingYAxis = chartPaddingValues.chartPaddingYAxis,
                        yLabelTextStyle = labelTextStyles.dataLabelTextStyle
                    )
                }
            },
            chart = { horizontalOffsetState ->
                LineChartSegment(
                    chartData = chartData,
                    animationTimeMillis = animationTimeMillis,
                    animationDelayMillis = animationDelayMillis,
                    minMaxValue = minMaxValue,
                    verticalShiftProperty = verticalShiftProperty,
                    lineChartStyle = lineChartStyle,
                    dataSpacing = dataSpacing,
                    backgroundColor = backgroundStyle.backgroundColor,
                    horizontalOffsetState = horizontalOffsetState
                )
            },
            chartTopPadding = lineChartStyle.lineWidth + lineChartStyle.dataPointStyle.height,
            chartBottomPadding = chartPaddingValues.chartPaddingXAxis,
            dataSpacing = dataSpacing,
            verticalShiftProperty = verticalShiftProperty,
            backgroundStyle = backgroundStyle,
            chartWidth = chartWidth
        )
    }
}

@Composable
private fun LineChartSegment(
    chartData: List<ZDLineChartData>,
    animationTimeMillis: Int,
    animationDelayMillis: Int,
    minMaxValue: MutableState<Pair<Float, Float>>,
    verticalShiftProperty: ZDVerticalShiftProperty,
    lineChartStyle: ZDLineChartStyle,
    dataSpacing: Dp,
    backgroundColor: Color,
    horizontalOffsetState: MutableFloatState
) {
    val holderSaver = getSaver<ZDLineChartData> {
        Holder(
            if(chartData.isNotEmpty()) {
                it
            } else {
                listOf()
            }
        ) }
    val data = rememberSaveable(stateSaver = holderSaver) {
        mutableStateOf(
            value = Holder(chartData),
            policy = referentialEqualityPolicy()
        )
    }

    val animationMap = remember {
        data.value.bandwidthDataList.associateWith { null }
            .toMutableMap<ZDLineChartData, Boolean?>()
    }

    updateLineData(
        data = data,
        chartData = chartData,
        animationMap = animationMap,
        minMaxValue = minMaxValue,
        maxState = null
    )

    val progressMap = data.value.bandwidthDataList.associateWith {
        rememberSaveable{ mutableStateOf(0f) }
    }.toMutableMap()

    val emptyBandwidthData = remember { ZDLineChartData(color = Color.Transparent, dataValues = listOf(), lineWidth = Dp.Hairline) }

    val animatedValueMap:Map<ZDLineChartData, State<Float>> = getLineAnimatedMap(
        progressedLineData = data,
        progressMap = progressMap,
        animationDuration = animationTimeMillis,
        animationDelay = animationDelayMillis,
        animationMap = animationMap,
        ZDLineData = chartData,
        emptyBandwidthData = emptyBandwidthData,
        maxState = null,
        minMaxValue = minMaxValue
    )

    LineChartCanvas(
        chartData = chartData,
        data = data.value.bandwidthDataList,
        verticalShiftProperty = verticalShiftProperty,
        lineStyle = lineChartStyle.lineStyle,
        lineWidth = lineChartStyle.lineWidth,
        progressMap = progressMap,
        animatedValueMap = animatedValueMap,
        xAxisSpacing = dataSpacing,
        animationMap = animationMap,
        dataPointStyle = lineChartStyle.dataPointStyle,
        backgroundColor = backgroundColor,
        gradient = lineChartStyle.gradientStyle,
        horizontalOffsetState = horizontalOffsetState
    )
}

internal fun updateLineData(
    data: MutableState<Holder<ZDLineChartData>>,
    chartData: List<ZDLineChartData>,
    animationMap: MutableMap<ZDLineChartData, Boolean?>,
    maxState:MutableState<Float>?,
    minMaxValue: MutableState<Pair<Float, Float>>?
) {
    if(data.value.bandwidthDataList.isEmpty() && chartData.isNotEmpty()) {
        data.value = Holder(chartData)
    } else if(data.value.bandwidthDataList != data && chartData.isNotEmpty()) {
        val list = data.value.bandwidthDataList.toMutableList()
        val added = addLineData(from = chartData, to = list, animationMap = animationMap,maxState = maxState, minMaxValue = minMaxValue)
        if(added) {
            data.value = Holder(list)
        }
    }

    if(chartData.isEmpty() && animationMap.isEmpty() && data.value.bandwidthDataList.isNotEmpty()) {
        data.value = Holder(listOf())
    }
}

@Composable
private fun LineChartCanvas(
    chartData:List<ZDLineChartData>,
    data: List<ZDLineChartData>,
    verticalShiftProperty: ZDVerticalShiftProperty,
    lineStyle: ZDLineStyle,
    lineWidth: Dp,
    gradient: ZDGradientStyle?,
    progressMap:  MutableMap<ZDLineChartData, MutableState<Float>>,
    animatedValueMap:  Map<ZDLineChartData, State<Float>>,
    xAxisSpacing: Dp,
    animationMap: MutableMap<ZDLineChartData, Boolean?>,
    dataPointStyle: ZDDataPointStyle?,
    backgroundColor: Color,
    horizontalOffsetState: MutableFloatState
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
    ) {
        data.forEach { lineChartData ->
            if (lineChartData.dataValues.isNotEmpty() || animationMap[lineChartData] == true) {
                drawLineChart(
                    lineChartData = lineChartData,
                    chartData = chartData,
                    verticalShiftProperty = verticalShiftProperty,
                    lineStyle = lineStyle,
                    lineWidth = lineWidth,
                    gradient = gradient,
                    progressMap = progressMap,
                    animatedValueMap = animatedValueMap,
                    xAxisSpacing = xAxisSpacing,
                    animationMap = animationMap,
                    dataPointStyle = dataPointStyle,
                    backgroundColor = backgroundColor,
                    horizontalOffset = horizontalOffsetState.value
                )
            }
        }
    }
}

internal fun DrawScope.drawLineChart(
    lineChartData: ZDLineChartData,
    chartData: List<ZDLineChartData>,
    verticalShiftProperty: ZDVerticalShiftProperty,
    lineStyle: ZDLineStyle,
    lineWidth: Dp,
    gradient: ZDGradientStyle?,
    progressMap: MutableMap<ZDLineChartData, MutableState<Float>>,
    animatedValueMap: Map<ZDLineChartData, State<Float>>,
    xAxisSpacing: Dp,
    animationMap: MutableMap<ZDLineChartData, Boolean?>,
    dataPointStyle: ZDDataPointStyle?,
    backgroundColor: Color,
    startOffset: Float = 0f,
    horizontalOffset: Float
) {
    val dataList = lineChartData.dataValues
    val lineColor = lineChartData.color
    val dataLineStyle = lineChartData.lineStyle ?: lineStyle
    val lineWidthPx = if(lineChartData.lineWidth == Dp.Hairline) {
        lineWidth.toPx()
    } else {
        lineChartData.lineWidth.toPx()
    }
    val canvasWidth = size.width
    val animatedValue = animatedValueMap[lineChartData]?.value ?: 1f
    val alphaVal = getAlphaValue(animatedValue = animatedValue)

    val canvasHeight = size.height
    val spacing = xAxisSpacing.toPx()
    val heightSpacing = canvasHeight / verticalShiftProperty.maxShiftValue

    val isBandwidthInData = lineChartData in chartData

    updateProgress(
        isBandwidthInData = isBandwidthInData,
        progressMap = progressMap,
        animationMap = animationMap,
        ZDLineChartData = lineChartData
    )
    val path = Path()

    var point = Offset(0f, canvasHeight - ((dataList.first().getValue(verticalShiftProperty)) * heightSpacing))

    val conPoints = ArrayList<ZDBezierPoints>()

    dataList.forEachDataInRange(
        spacing = spacing,
        horizontalOffset = horizontalOffset,
        canvasWidth = canvasWidth,
    ) { index, previousIndex, dataValue, isFirstIndex ->
        val currData = dataValue.getValue(verticalShiftProperty)
        val x = (index * spacing) + startOffset + horizontalOffset
        val currentDatapoint = Offset(
            x = x,
            y = canvasHeight - currData * heightSpacing
        )

        if(!isFirstIndex) {
            drawLine(
                conPoints = conPoints,
                previousIndex = previousIndex,
                path = path,
                lineStyle = dataLineStyle,
                currentDatapoint = currentDatapoint,
                point = point
            )
        } else {
            path.moveTo(currentDatapoint.x, currentDatapoint.y)
        }
        point = currentDatapoint
    }

    val pathMeasure = PathMeasure().apply {
        setPath(path, false)
    }
    val pathLength = pathMeasure.length

    var strokeCap = StrokeCap.Butt
    val outerPathEffect = when(lineChartData.linePathType) {
        ZDLinePathType.DOT -> {
            strokeCap = StrokeCap.Round
            PathEffect.dashPathEffect(
                floatArrayOf(
                    0.1.dp.toPx(),
                    lineWidthPx * 2
                )
            )
        }
        ZDLinePathType.GRID -> {
            PathEffect.dashPathEffect(
                floatArrayOf(
                    lineWidthPx * 2,
                    lineWidthPx
                )
            )
        }
        ZDLinePathType.LINE -> {
            PathEffect.dashPathEffect(
                floatArrayOf(pathLength, 0f)
            )
        }
    }

    val pathEffect = PathEffect.chainPathEffect(
        outer =  outerPathEffect,
        inner = PathEffect.dashPathEffect(
            floatArrayOf(
                pathLength,
                pathLength
            ),
            phase = (1 - animatedValue) * pathLength
        ),

        )

    val stroke = Stroke(
        width = lineWidthPx,
        pathEffect = pathEffect,
        cap = strokeCap
    )

    drawPath(
        path,
        lineColor,
        style = stroke
    )

    gradient?.let { gradientStyle ->
        val maxVal = dataList.maxOf { it.value }
        val maxY = canvasHeight - (maxVal * heightSpacing)
        drawGradient(
            path = path,
            canvasHeight = canvasHeight,
            alphaVal = alphaVal,
            gradient = gradientStyle,
            lineColor = lineColor,
            lastPoint = point,
            maxY = maxY,
            horizontalOffset = horizontalOffset
        )
    }

    if(dataPointStyle != None) {
        drawDataPoint(
            animatedValue = animatedValue,
            dataList =  dataList,
            dataPointStyle = dataPointStyle ?: lineChartData.dataPointStyle,
            spacing = spacing,
            canvasHeight = canvasHeight,
            heightSpacing = heightSpacing,
            backgroundColor = backgroundColor,
            lineColor = lineColor,
            verticalShiftProperty = verticalShiftProperty,
            startOffset = startOffset,
            horizontalOffset = horizontalOffset
        )
    }
    path.close()
}

private fun drawLine(
    lineStyle: ZDLineStyle,
    previousIndex: Int,
    path:Path,
    conPoints: ArrayList<ZDBezierPoints>,
    point: Offset,
    currentDatapoint: Offset
) {
    if(lineStyle == ZDLineStyle.CURVED) {
        conPoints.add(
            ZDBezierPoints(
                point.x,
                point.y,
                currentDatapoint.x,
                currentDatapoint.y
            )
        )
        path.cubicTo(
            conPoints[previousIndex].x1,
            conPoints[previousIndex].y1,
            conPoints[previousIndex].x2,
            conPoints[previousIndex].y2,
            currentDatapoint.x,
            currentDatapoint.y
        )
    }
    else {
        path.lineTo(currentDatapoint.x,currentDatapoint.y)
    }
}

/**
 * @param gradient
 * Will apply the gradient colors if provided else a list of gradually fading colors will be applied
 */
private fun DrawScope.drawGradient(
    path: Path,
    canvasHeight: Float,
    lastPoint: Offset,
    maxY: Float,
    alphaVal: Float,
    lineColor: Color,
    gradient: ZDGradientStyle,
    horizontalOffset: Float
) {
    path.lineTo(
        lastPoint.x,
        canvasHeight
    )
    path.lineTo(
        horizontalOffset.toFloat(),
        canvasHeight
    )
    if (alphaVal > 0f) {
        val brushColors = gradient.colors.ifEmpty {
            listOf(
                lineColor.copy(alpha = 0.5f * alphaVal),
                lineColor.copy(alpha = 0.25f * alphaVal),
                lineColor.copy(alpha = 0.125f * alphaVal),
                lineColor.copy(alpha = 0.0625f * alphaVal),
                Color.Transparent,
            )
        }
        val brush = when(gradient.direction){
            ZDGradientDirection.HORIZONTAL -> {
                Brush.horizontalGradient(brushColors)
            }
            ZDGradientDirection.VERTICAL -> {
                Brush.verticalGradient(
                    colors = brushColors,
                    startY = maxY
                )
            }
        }
        drawPath(
            path = path,
            brush = brush,
        )
    }
}

private fun DrawScope.drawDataPoint(
    animatedValue: Float,
    dataList: List<ZDDataValue>,
    dataPointStyle: ZDDataPointStyle,
    spacing: Float,
    canvasHeight: Float,
    heightSpacing: Float,
    backgroundColor: Color,
    lineColor: Color,
    verticalShiftProperty: ZDVerticalShiftProperty,
    startOffset: Float,
    horizontalOffset: Float
) {
    val dataSize = dataList.size
    val currIndex = (animatedValue * (dataSize - 1)).toInt()

    val spacingCircle:Size
    val pointSize:Size
    val strokeWidth: Float

    var dataColor = lineColor
    val drawStyle = when (dataPointStyle) {
        None -> {
            spacingCircle = Size(0f,0f)
            pointSize = Size(0f, 0f)
            Stroke()
        }
        is Filled -> {
            dataPointStyle.color?.let {
                dataColor = Color(it)
            }
            pointSize = dataPointStyle.size
            spacingCircle = Size(
                width = pointSize.width + dataPointStyle.spacing.toPx(),
                height = pointSize.height + dataPointStyle.spacing.toPx()
            )
            Fill
        }
        is Outline -> {
            dataPointStyle.color?.let {
                dataColor = Color(it)
            }
            strokeWidth = dataPointStyle.width.toPx()
            pointSize = dataPointStyle.size
            spacingCircle = Size(
                width = pointSize.width + dataPointStyle.spacing.toPx() + strokeWidth,
                height = pointSize.height + dataPointStyle.spacing.toPx() + strokeWidth
            )
            Stroke(width = strokeWidth)
        }
    }
    val indexRange = (0..currIndex).toList()
    indexRange.forEachDataInRange(
        spacing = spacing,
        horizontalOffset = horizontalOffset,
        canvasWidth = size.width,
    ) { _, _, index, _ ->
        val dataValue = dataList[index]
        val currData = dataValue.getValue(verticalShiftProperty)
        val dataPoint = Offset(
            x = index * spacing + startOffset + horizontalOffset,
            y = canvasHeight - currData * heightSpacing
        )
        val pointCenter = Offset(
            dataPoint.x - (pointSize.width / 2),
            dataPoint.y - (pointSize.height / 2)
        )
        val spacingCenter = Offset(
            dataPoint.x - (spacingCircle.width / 2),
            dataPoint.y - (spacingCircle.height / 2)
        )
        if(dataValue.isAnomaly) {
            drawStar(
                posX = dataPoint.x,
                posY = dataPoint.y,
                backgroundColor = backgroundColor,
                size = STAR_SIZE.toPx()
            )
        } else {
            /**
             * Drawing inner circle with background color
             */
            drawOval(
                color = backgroundColor,
                topLeft = spacingCenter,
                size = spacingCircle
            )
            /**
             * Drawing outer circle with stroke
             * If dataPoint style is filled, the outer circle is drawn in fill style
             */
            drawOval(
                color = dataColor,
                topLeft = pointCenter,
                size = pointSize,
                style = drawStyle
            )
        }
    }
}

private fun updateProgress(
    isBandwidthInData: Boolean,
    progressMap: MutableMap<ZDLineChartData, MutableState<Float>>,
    animationMap: MutableMap<ZDLineChartData, Boolean?>,
    ZDLineChartData: ZDLineChartData
) {
    if (isBandwidthInData && (progressMap[ZDLineChartData]?.value ?: 0f) == ZDProgress.PROGRESS.PROGRESS_AT_START) {
        progressMap[ZDLineChartData]?.value = ZDProgress.PROGRESS.PROGRESS_COMPLETE
        animationMap[ZDLineChartData] = true
    } else if (!isBandwidthInData && (progressMap[ZDLineChartData]?.value ?: 1f) == ZDProgress.PROGRESS.PROGRESS_COMPLETE) {
        progressMap[ZDLineChartData]?.value = ZDProgress.PROGRESS.PROGRESS_AT_START
        animationMap[ZDLineChartData] = true
    }
}

private fun getAlphaValue(
    animatedValue: Float,
    @IntRange(from = 0L, to = 100L)
    completionPercent: Int = 75
):Float {
    val multiplier = 100f / (100 - completionPercent)
    return ((animatedValue - (completionPercent / 100f)) * multiplier).coerceIn(0f, 1f)
}