package com.abhilash.weighttracker.chart.chart.ui.layout

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.*
import com.abhilash.weighttracker.chart.chart.data.ZDBackgroundStyle
import com.abhilash.weighttracker.chart.chart.data.ZDVerticalShiftProperty
import com.abhilash.weighttracker.chart.chart.data.ZDXLabelOrientation
import kotlin.math.cos
import kotlin.math.roundToInt

@Composable
internal fun ZDChartScreenLayout(
    modifier : Modifier = Modifier,
    xLabelOrientation: ZDXLabelOrientation,
    backgroundStyle: ZDBackgroundStyle,
    verticalShiftProperty: ZDVerticalShiftProperty,
    scrollState: ScrollState = rememberScrollState(),
    chartWidth : Dp,
    barWidth:Dp = 0.dp,
    dataSpacing:Dp = 0.dp,
    chartTopPadding: Dp = 0.dp,
    chartBottomPadding: Dp  = 0.dp,
    xAxisLabels: @Composable () -> Unit,
    yAxisLabels : @Composable () -> Unit,
    chart : @Composable () -> Unit,
) {
    SubcomposeLayout(
        modifier = modifier
            .background(backgroundStyle.backgroundColor)
    ) { constraints ->
        val layoutWidth = constraints.maxWidth
        val layoutHeight = constraints.maxHeight

        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val xAxisConstraints = looseConstraints.copy(maxWidth = looseConstraints.maxWidth)

        val bottomPadding = chartBottomPadding.toPx().toInt()
        val xAxisMeasurable = subcompose(ChartLayout.X_AXIS_LENGTH) {
            xAxisLabels()
        }
        val xAxisPlaceable = xAxisMeasurable.map { it.measure(xAxisConstraints) }
        val bottomSpacing = if(xAxisPlaceable.isNotEmpty()) {
            calculateBottomSpacing(
                height = xAxisPlaceable.maxOf { it.height },
                width = xAxisPlaceable.maxOf { it.width },
                xLabelOrientation = xLabelOrientation
            )
        } else {
            0
        }
        layout(layoutWidth, layoutHeight) {
            val yAxisConstraints = looseConstraints.copy(
                maxHeight = layoutHeight - chartTopPadding.roundToPx() - chartBottomPadding.roundToPx()
            )
            val yAxisPlaceable = subcompose(ChartLayout.Y_AXIS, yAxisLabels).map { it.measure(yAxisConstraints) }
            val yAxisWidth = yAxisPlaceable.getZeroOnEmpty { maxOf { it.width } }

            val chartConstraints = looseConstraints.copy()

            val xLastPlaceableWidth = xAxisPlaceable.getZeroOnEmpty { last().width }
            val xFirstPlaceableWidth = xAxisPlaceable.getZeroOnEmpty { first().width }

            val xAxisLabelWidth = (xFirstPlaceableWidth/2).toDp() + (xLastPlaceableWidth/2).toDp()

            val chartWithXAxis = @Composable {
                ZDXAxisChartLayout(
                    xLabelOrientation = xLabelOrientation,
                    bottomSpacing = bottomSpacing,
                    bottomPadding = bottomPadding,
                    dataSpacing = dataSpacing,
                    barWidth = barWidth,
                    yLabelHeight = yAxisPlaceable.getZeroOnEmpty { last().height },
                    xAxisLabels = xAxisLabels,
                    chart = chart,
                    chartWidth = chartWidth,
                    minWidth = (layoutWidth - yAxisWidth).toDp(),
                    topPadding = chartTopPadding,
                    totalWidth = chartWidth + yAxisWidth.toDp() + xAxisLabelWidth,
                    scrollState = scrollState
                )
            }

            if(backgroundStyle.drawBgLines) {
                val yLabelHeight = yAxisPlaceable.getZeroOnEmpty { maxOf { it.height } }
                val bgMeasurable = subcompose(ChartLayout.BACKGROUND_CANVAS) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = chartTopPadding,
                                bottom = chartBottomPadding,
                            )
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        val heightSpacing = canvasHeight / verticalShiftProperty.numberOfShifts

                        var y = 0f

                        while (y <= canvasHeight) {
                            drawLine(
                                color = backgroundStyle.bgLineColor,
                                start = Offset(0f, y),
                                end = Offset(canvasWidth, y),
                                pathEffect = backgroundStyle.bgLinePathEffect,
                                strokeWidth = backgroundStyle.lineWidth.toPx(),
                                cap = backgroundStyle.strokeCap
                            )
                            y += heightSpacing
                        }

                        drawLine(
                            color = backgroundStyle.bgLineColor,
                            start = Offset(0f, canvasHeight),
                            end = Offset(canvasWidth, canvasHeight),
                            pathEffect = backgroundStyle.bgLinePathEffect,
                            strokeWidth = backgroundStyle.lineWidth.toPx(),
                            cap = backgroundStyle.strokeCap
                        )
                    }
                }

                val bgConstraints = looseConstraints.copy(
                    maxHeight = looseConstraints.maxHeight - bottomSpacing - yLabelHeight
                )
                val bgPlaceable = bgMeasurable.map { it.measure(bgConstraints) }

                bgPlaceable.forEach {
                    it.placeRelativeWithLayer (
                        x = yAxisWidth,
                        y = yLabelHeight/2,
                        layerBlock = {
                            clip = true
                            shape = ClipRect(Size((layoutWidth - yAxisWidth).toDp().toPx(),layoutHeight.toDp().toPx()))
                        }
                    )
                }
            }

            val xAxisChartPlaceable = subcompose(ChartLayout.CHART_WITH_LABELS, chartWithXAxis).map {
                it.measure(chartConstraints)
            }

            xAxisChartPlaceable.forEach {
                it.placeRelativeWithLayer(
                    x = yAxisWidth,
                    y = 0,
                    layerBlock = {
                        clip = true
                        shape = ClipRect(Size((layoutWidth - yAxisWidth).toDp().toPx(), layoutHeight.toDp().toPx()))
                    }
                )
            }

            val ySpace = calculateSpace(
                placeableList = yAxisPlaceable,
                layoutSpace = layoutHeight - bottomSpacing - chartTopPadding.roundToPx() - chartBottomPadding.roundToPx()
            )
            var yPos = chartTopPadding.roundToPx()

            yAxisPlaceable.forEach {
                it.place(yAxisWidth - it.width, yPos)
                yPos += it.height + ySpace
            }
        }
    }
}

@Composable
internal fun ZDXAxisChartLayout(
    xLabelOrientation: ZDXLabelOrientation,
    bottomSpacing: Int,
    bottomPadding: Int,
    barWidth: Dp = 0.dp,
    dataSpacing: Dp = 0.dp,
    yLabelHeight: Int,
    chartWidth: Dp,
    xAxisLabels: @Composable () -> Unit,
    chart: @Composable () -> Unit,
    topPadding: Dp,
    minWidth: Dp,
    totalWidth: Dp,
    scrollState: ScrollState
) {
    val scrollPos = remember { mutableStateOf(scrollState.value) }
    LaunchedEffect(key1 = chartWidth) {
        if(scrollState.value != scrollPos.value)
            scrollPos.value = scrollState.value
    }
    SubcomposeLayout(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .defaultMinSize(minWidth = minWidth)
            .width(totalWidth)
            .padding(top = topPadding)
    ) { constraints ->
        val layoutWidth = constraints.maxWidth
        val layoutHeight = constraints.maxHeight
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        layout(layoutWidth, layoutHeight) {
            val xAxisConstraints = looseConstraints.copy(maxWidth = layoutWidth)
            val xAxisLabelPlaceable = subcompose(ChartLayout.X_AXIS, xAxisLabels).map { it.measure(xAxisConstraints) }

            val firstLabelWidth = xAxisLabelPlaceable.getZeroOnEmpty { first().width }
            val barWidthPx = barWidth.roundToPx()

            val chartMaxHeight = looseConstraints.maxHeight - bottomSpacing - yLabelHeight - bottomPadding
            val chartConstraints = looseConstraints
                .copy(
                    maxHeight = chartMaxHeight,
                    maxWidth = layoutWidth
                )
            val chartPlaceable = subcompose(ChartLayout.CHART, chart).map { it.measure(chartConstraints) }

            val chartHeight = chartPlaceable.maxOf { it.height }

            chartPlaceable.forEach { it.place ((firstLabelWidth - barWidthPx)/2, yLabelHeight/2) }

            val xMinWidth = xAxisLabelPlaceable.getZeroOnEmpty { minOf { it.width } }

            val yPos = chartHeight + yLabelHeight + bottomPadding
            val ySlantedPos = if(xLabelOrientation != ZDXLabelOrientation.STRAIGHT) {
                (xMinWidth * cos(xLabelOrientation.angle)).toInt() / 2
            } else {
                0
            }

            xAxisLabelPlaceable.forEachIndexed { index, placeable ->
                val spacing = (barWidth.toPx() + dataSpacing.toPx()).roundToInt()
                val xPos = index * spacing
                placeable.place(
                    x = xPos,
                    y = yPos + ySlantedPos
                )
            }
        }
    }
}

private class ClipRect(val size:Size) :Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Rectangle(this.size.toRect())
    }
}

@Composable
internal fun ZDFillChartLayout(
    modifier: Modifier,
    dataSpacing: Dp,
    chartWidth: Dp,
    topLabels: @Composable () -> Unit,
    bottomLabels: @Composable () -> Unit,
    chart: @Composable () -> Unit
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val layoutHeight = constraints.maxHeight
        val layoutWidth = constraints.maxWidth
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        layout(width = layoutWidth, height = layoutHeight) {
            val chartWithLabels = @Composable {
                ChartWithLabels(
                    chartWidth = chartWidth,
                    dataSpacing = dataSpacing,
                    topLabels = topLabels,
                    bottomLabels = bottomLabels,
                    chart = chart
                )
            }
            val placeables = subcompose(ChartLayout.CHART_WITH_LABELS, chartWithLabels).map { it.measure(looseConstraints) }
            placeables.forEach { placeable ->
                placeable.place(x = 0, y = 0)
            }
        }
    }
}

@Composable
private fun ChartWithLabels(
    chartWidth: Dp,
    dataSpacing: Dp,
    topLabels: @Composable () -> Unit,
    bottomLabels: @Composable () -> Unit,
    chart: @Composable () -> Unit
) {
    val scrollState = rememberScrollState()
    SubcomposeLayout(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .fillMaxHeight()
            .width(chartWidth)
    ) { constraints ->
        val layoutHeight = constraints.maxHeight
        val layoutWidth = constraints.maxWidth
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        layout(width = layoutWidth, height = layoutHeight) {
            val topLabelPlaceables = subcompose(ChartLayout.TOP_LABELS, topLabels).map { it.measure(looseConstraints) }
            val bottomLabelPlaceables = subcompose(ChartLayout.BOTTOM_LABELS, bottomLabels).map { it.measure(looseConstraints) }

            val topLabelHeight  = topLabelPlaceables.getZeroOnEmpty { maxOf { it.height } }
            val bottomLabelHeight = bottomLabelPlaceables.getZeroOnEmpty { maxOf { it.height } }

            val chartConstraints = looseConstraints.copy(
                maxHeight = (looseConstraints.maxHeight - topLabelHeight - bottomLabelHeight),
                maxWidth = looseConstraints.maxWidth
            )

            val chartPlaceable = subcompose(ChartLayout.CHART, chart).map { it.measure(chartConstraints) }

            val spacing = dataSpacing.roundToPx()

            val chartHeight = chartPlaceable.maxOf { it.height }

            var x = spacing/2
            topLabelPlaceables.forEach { placeable ->
                placeable.place(x = (x - placeable.width / 2), y = 0)
                x += spacing
            }

            x = 0
            chartPlaceable.forEach { placeable ->
                placeable.place(x = x, y = topLabelHeight)
            }

            x = spacing/2
            bottomLabelPlaceables.forEach { placeable ->
                placeable.place(x = x - placeable.width / 2, y = (topLabelHeight + chartHeight))
                x += spacing
            }
        }
    }
}