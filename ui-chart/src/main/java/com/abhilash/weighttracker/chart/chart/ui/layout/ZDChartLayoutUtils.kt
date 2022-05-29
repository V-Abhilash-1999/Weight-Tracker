package com.abhilash.weighttracker.chart.chart.ui.layout

import androidx.compose.ui.layout.Placeable
import com.abhilash.weighttracker.chart.chart.data.ZDXLabelOrientation
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin

internal enum class ChartLayout {
    X_AXIS,
    Y_AXIS,
    CHART,
    X_AXIS_LENGTH,
    BACKGROUND_CANVAS,
    CHART_WITH_LABELS,
    TOP_LABELS,
    BOTTOM_LABELS
}

internal fun calculateSpace(placeableList: List<Placeable>, layoutSpace: Int): Int {
    val totalSpace = placeableList.sumOf { it.height }
    val remainingSpace = layoutSpace - totalSpace
    val noOfLabels = if(placeableList.size > 1) {
        placeableList.size - 1
    } else {
        1
    }
    return remainingSpace / noOfLabels
}

internal fun calculateBottomSpacing(
    width: Int,
    height: Int,
    xLabelOrientation: ZDXLabelOrientation
) = ((width * sin(xLabelOrientation.angle.absoluteValue)) + (height * cos(xLabelOrientation.angle.absoluteValue))).toInt()

internal fun <T> List<T>.getZeroOnEmpty(function : List<T>.() -> Int) = if(isNotEmpty()) function() else 0

