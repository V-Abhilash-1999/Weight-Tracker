package com.abhilash.weighttracker.chart.chart.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.abhilash.weighttracker.chart.chart.data.ZDLineChartData
import com.abhilash.weighttracker.chart.chart.data.ZDXLabelOrientation
import com.abhilash.weighttracker.chart.chart.utils.Holder
import com.abhilash.weighttracker.chart.chart.utils.lineChartAnimationCallBack

@Composable
internal fun getLineAnimatedMap(
    progressedLineData: MutableState<Holder<ZDLineChartData>>,
    progressMap: MutableMap<ZDLineChartData, MutableState<Float>>,
    animationDuration: Int,
    animationDelay: Int,
    animationMap: MutableMap<ZDLineChartData, Boolean?>,
    ZDLineData: List<ZDLineChartData>,
    emptyBandwidthData: ZDLineChartData,
    maxState: MutableState<Float>?,
    minMaxValue: MutableState<Pair<Float, Float>>?
) = progressedLineData.value.bandwidthDataList.associateWith { bandwidthData ->
    animateFloatAsState(
        targetValue = progressMap[bandwidthData]?.value ?: 1f,
        animationSpec = tween(
            durationMillis = animationDuration,
            delayMillis = animationDelay,
        )
    ) {
        lineChartAnimationCallBack(
            animationMap = animationMap,
            progressMap = progressMap,
            bandwidthData = bandwidthData,
            data = progressedLineData,
            chartData = ZDLineData,
            index = progressedLineData.value.bandwidthDataList.indexOf(bandwidthData),
            emptyBandwidthData = emptyBandwidthData,
            minMaxValue = minMaxValue,
            maxState = maxState
        )
    }
}

@Composable
internal fun YLabelsList(
    list: List<String>,
    yAxisWidth: Dp,
    chartPaddingYAxis: Dp,
    yLabelTextStyle: TextStyle
) {
    val yAxisModifier = Modifier.run {
        if(yAxisWidth != Dp.Hairline) {
            this.width(yAxisWidth)
        } else {
            this
        }
    }
    list.forEach {
        Box(
            modifier = Modifier
                .padding(end = chartPaddingYAxis),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = it,
                style = yLabelTextStyle,
                maxLines = 1,
                modifier = yAxisModifier,
            )
        }
    }
}

@Composable
internal fun XLabelsList(
    list: List<String>,
    xLabelTextStyle: TextStyle,
    maxLines:Int,
    xLabelOrientation: ZDXLabelOrientation,
    dataSpacing: Dp
) {
    list.forEach {
        val modifier = Modifier.run {
            if(xLabelOrientation == ZDXLabelOrientation.STRAIGHT) {
                width(dataSpacing)
            } else {
                width(dataSpacing).rotate(xLabelOrientation.angle)
            }
        }

        Text(
            text = it,
            style = xLabelTextStyle,
            textAlign = TextAlign.Center,
            modifier = modifier,
            overflow = TextOverflow.Ellipsis,
            maxLines = maxLines
        )
    }
}


internal fun DrawScope.drawStar(
    posX: Float,
    posY: Float,
    backgroundColor: Color,
    size: Float
) {
    val starSize = Size(size, size)
    val starPath = Path()

    val min = kotlin.math.min(starSize.width, starSize.height)
    val mid = starSize.width
    starPath.apply {
        moveTo(posX - mid + min * 0.50f,posY + min * 0.84f - starSize.width)
        lineTo(posX - mid + min * 1.50f,posY + min * 0.84f - starSize.width)
        lineTo(posX - mid + min * 0.68f,posY + min * 1.45f - starSize.width)
        lineTo(posX - mid + min * 1.00f,posY + min * 0.50f - starSize.width)
        lineTo(posX - mid + min * 1.32f,posY + min * 1.45f - starSize.width)
        lineTo(posX - mid + min * 0.50f,posY + min * 0.84f - starSize.width)
        close()
    }
    drawPath(path = starPath, color = Color.Red, style = Stroke(2f))
    drawPath(path = starPath, color = backgroundColor, style = Fill)
    starPath.close()
}

internal fun <T> List<T>.forEachDataInRange(
    lazyIndexPaddingStart: Int = 1,
    spacing: Float,
    horizontalOffset: Float,
    canvasWidth: Float,
    action: (index: Int, previousIndex: Int, dataValue: T, isFirstIndex: Boolean) -> Unit
) {
    val startIndex = ((-horizontalOffset.toInt() / spacing.toInt()) - lazyIndexPaddingStart).coerceIn(0, lastIndex)
    val finalIndex = (((-horizontalOffset + canvasWidth).toInt() / spacing.toInt()) + 1).coerceIn(0, lastIndex)

    for (index in startIndex..finalIndex) {
        val isFirstIndex = index == startIndex
        val previousIndex = index - startIndex - 1
        action(index,previousIndex, get(index), isFirstIndex)
    }
}