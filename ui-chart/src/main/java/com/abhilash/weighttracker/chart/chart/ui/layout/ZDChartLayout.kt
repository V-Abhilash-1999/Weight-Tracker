package com.abhilash.weighttracker.chart.chart.ui.layout

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.abhilash.weighttracker.chart.chart.data.ZDBackgroundStyle
import com.abhilash.weighttracker.chart.chart.data.ZDChartScrollState
import com.abhilash.weighttracker.chart.chart.data.ZDVerticalShiftProperty
import com.abhilash.weighttracker.chart.chart.data.ZDXLabelOrientation
import kotlin.math.cos
import kotlin.math.roundToInt

internal typealias MutableFloatState = MutableState<Float>

@Composable
internal fun ZDChartScreenLayout(
    modifier : Modifier = Modifier,
    xLabelOrientation: ZDXLabelOrientation,
    backgroundStyle: ZDBackgroundStyle,
    verticalShiftProperty: ZDVerticalShiftProperty,
    scrollState: ZDChartScrollState,
    chartWidth: Float = 0f,
    barWidth: Dp = 0.dp,
    dataSpacing: Dp = 0.dp,
    chartTopPadding: Dp = 0.dp,
    chartBottomPadding: Dp = 0.dp,
    xAxisLabels: @Composable () -> Unit,
    yAxisLabels : @Composable () -> Unit,
    chart : @Composable (MutableFloatState) -> Unit,
) {
    val scrollOffset = rememberSaveable { mutableStateOf(0f) }
    val scrollStateAnimatable = remember { scrollState.animatable }

    val scrollTo by rememberSaveable { scrollState.scrollToPercent }
    SubcomposeLayout(
        modifier = modifier
            .background(backgroundStyle.backgroundColor)
            .enableSwipe(
                scrollStateAnimatable = scrollStateAnimatable,
                chartWidth = chartWidth,
                pointerOffset = scrollOffset
            )
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

            val adjustedChartWidth = (layoutWidth - yAxisWidth).toDp()

            val chartWithXAxis = @Composable {
                ZDXAxisChartLayout(
                    modifier = Modifier
                        .width(adjustedChartWidth)
                        .padding(top = chartTopPadding),
                    xLabelOrientation = xLabelOrientation,
                    bottomSpacing = bottomSpacing,
                    bottomPadding = bottomPadding,
                    dataSpacing = dataSpacing,
                    barWidth = barWidth,
                    yLabelHeight = yAxisPlaceable.getZeroOnEmpty { last().height },
                    xAxisLabels = xAxisLabels,
                    chart = chart,
                    offset = scrollOffset
                )
            }

            val xAxisChartPlaceable = subcompose(ChartLayout.CHART_WITH_LABELS, chartWithXAxis).map {
                it.measure(chartConstraints)
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

                val bgMaxHeight = looseConstraints.maxHeight - bottomSpacing - yLabelHeight

                val bgConstraints = looseConstraints.copy(maxHeight = bgMaxHeight)
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

//@Composable
//fun ScrollToSetter(
//    scrollPercent: Int,
//    scrollStateAnimatable: Animatable<Float, AnimationVector1D>,
//    chartWidth: Float
//) {
//    LaunchedEffect(key1 = scrollPercent) {
//        val scrollToValue = -(scrollPercent * chartWidth)/100
//        scrollStateAnimatable
//            .animateTo(
//                scrollToValue,
//                AnimationSpec
//            )
//    }
//}

@Composable
private fun ZDXAxisChartLayout(
    modifier: Modifier,
    xLabelOrientation: ZDXLabelOrientation,
    bottomSpacing: Int,
    dataSpacing: Dp,
    bottomPadding: Int,
    barWidth: Dp = 0.dp,
    yLabelHeight: Int,
    offset: MutableFloatState,
    xAxisLabels: @Composable () -> Unit,
    chart: @Composable (MutableFloatState) -> Unit
) {
    SubcomposeLayout(
        modifier = modifier
    ) { constraints ->
        val layoutWidth = constraints.maxWidth
        val layoutHeight = constraints.maxHeight
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        layout(layoutWidth, layoutHeight) {
            val xAxisConstraints = looseConstraints.copy(maxWidth = looseConstraints.maxWidth)

            val xAxisPlaceable = subcompose(ChartLayout.X_AXIS_LENGTH, xAxisLabels).map { it.measure(xAxisConstraints) }

            val firstLabelWidth = xAxisPlaceable.getZeroOnEmpty { first().width }
            val barWidthPx = barWidth.roundToPx()

            val chartMaxHeight = looseConstraints.maxHeight - bottomSpacing - yLabelHeight - bottomPadding
            val chartConstraints = looseConstraints
                .copy(
                    maxHeight = chartMaxHeight,
                    maxWidth = layoutWidth
                )
            val chartHolder = @Composable {
                chart(offset)
            }
            val chartPlaceable = subcompose(ChartLayout.CHART, chartHolder).map { it.measure(chartConstraints) }

            val chartHeight = chartPlaceable.maxOf { it.height }

            chartPlaceable.forEach { it.place ((firstLabelWidth - barWidthPx)/2, yLabelHeight/2) }

            val xMinWidth = xAxisPlaceable.getZeroOnEmpty { minOf { it.width } }

            val ySlantedPos =
                ((xMinWidth * cos(xLabelOrientation.angle)).toInt() / 2)
                    .takeIf { xLabelOrientation != ZDXLabelOrientation.STRAIGHT } ?: 0

            val yPos = chartHeight + yLabelHeight + bottomPadding + ySlantedPos

            val spacing = (barWidth.toPx() + dataSpacing.toPx()).roundToInt()

            xAxisPlaceable.forEachIndexed { index, placeable ->
                val xPos = index * spacing
                placeable.place(x = (xPos + offset.value).toInt(), y = yPos)
            }
        }
    }
}

private class ClipRect(val size: Size) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Rectangle(this.size.toRect())
    }
}