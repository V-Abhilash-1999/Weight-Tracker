package com.abhilash.weighttracker.chart.chart.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abhilash.weighttracker.chart.chart.data.*
import com.abhilash.weighttracker.chart.chart.ui.circular.ZDPieChartOnClickListener
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.sqrt

internal const val DEG_TO_RAD_CONST = 57.2958f
internal val STAR_SIZE = 16.dp
internal val BAR_TOP_SPACING = 8.dp

internal fun List<ZDBandwidthData>.getMaxLabelList() : List<String> {
    val data = this
    return if(data.isNotEmpty()) {
        when(val first = data.first()) {
            is ZDLineChartData -> {
                var max = first.dataValues.size
                var maxIndex = 0
                for (i in data.indices) {
                    val currData = data[i]
                    if(currData is ZDLineChartData) {
                        if (max < currData.dataValues.size) {
                            max = currData.dataValues.size
                            maxIndex = i
                        }
                    }
                }
                (data[maxIndex] as? ZDLineChartData)?.dataValues?.map { it.xLabel } ?: listOf()
            }

            is ZDBarData -> {
                data.map {
                    if(it is ZDBarData) {
                        it.xLabel
                    } else {
                        ""
                    }
                }
            }

            else -> {
                listOf()
            }
        }
    } else {
        listOf()
    }
}

internal fun ZDVerticalShiftProperty.getYAxisLabels(): List<String> {
    val list = mutableListOf<String>()
    for (i in 0 until numberOfShifts + 1) {
        val yAxisLabelValue = maxShiftValue - (maxShiftValue / numberOfShifts) * i
        val text = if (yAxisLabelValue % 1 == 0f || yAxisLabelValue == 0f) {
            yAxisLabelValue.toInt().toString()
        } else {
            "%.1f".format(yAxisLabelValue)
        }
        list.add(text)
    }
    return list
}

internal fun ZDVerticalShiftProperty.getYAxisLineLabels(): List<String> {
    val list = mutableListOf<String>()
    for (value in mainArr) {
        val textValue = ((1 - value) * (maxShiftValue - minShiftValue)) + minShiftValue
        val text = if (textValue % 1 == 0f || value == 0f) {
            textValue.toInt().toString()
        } else {
            val textVal = "%.1f".format(textValue).toFloat()
            if(textVal == textVal.toInt().toFloat()) {
                textVal.toInt().toString()
            } else {
                textVal.toString()
            }
        }
        list.add(text)
    }
    return list
}

internal fun <T> List<T>.getMaxSum():Float {
    return if(this.isEmpty()){
        0f
    } else {
        var max = Float.MIN_VALUE
        when(this.first()) {
            is ZDLineChartData -> {
                max = maxOf { lineData ->
                    if(lineData is ZDLineChartData) {
                        if(lineData.dataValues.isEmpty()) 0f
                        else lineData.dataValues.maxOf { it.value }
                    }
                    else {
                        0f
                    }
                }
            }

            is ZDBarData -> {
                forEach { barData ->
                    if(barData is ZDBarData){
                        barData.dataValues.forEach { stackedList ->
                            val stackSum = stackedList.sumOf { it.value.toDouble() }.toFloat()
                            if (stackSum > max)
                                max = stackSum
                        }
                    }
                }
            }

            is ZDCircularChartData -> {
                max = 0f
            }

            is ZDVerticalProgressData -> {
                max = 0f
            }
        }
        max
    }
}

internal fun List<ZDLineChartData>.getMinVal():Float {
    return if(this.isEmpty()) {
        0f
    } else {
        this.minOf { lineData ->
            if(lineData.dataValues.isEmpty()) 0f
            else lineData.dataValues.minOf { it.value }
        }
    }
}

internal fun <T> Map<T, MutableState<Float>>.toggleProgress() {
    this.values.forEach{
        if (it.value == ZDProgress.PROGRESS.PROGRESS_AT_START) {
            it.value = ZDProgress.PROGRESS.PROGRESS_COMPLETE
        } else {
            it.value = ZDProgress.PROGRESS.PROGRESS_AT_START
        }
    }
}

internal fun <T> Map<T, MutableState<Float>>.setProgress(progress: Float) {
    this.values.forEach{
        if(it.value != progress) {
            it.value = progress
        }
    }
}

internal fun List<ZDBandwidthData>.getWidth(spacing: Dp, removeBarWidth: Dp = Dp.Hairline): Dp {
    return if(this.isEmpty()){
        Dp.Hairline
    } else {
        when(this.first()) {
            is ZDLineChartData -> {
                val maxSize = maxOf {
                    if(it is ZDLineChartData) {
                        it.dataValues.size
                    } else {
                        0
                    }
                }
                ((maxSize * spacing.value) - removeBarWidth.value).dp
            }

            is ZDBarData -> {
                ((size - 1) * spacing.value).dp
            }

            is ZDCircularChartData -> {
                Dp.Hairline
            }

            is ZDVerticalProgressData -> {
                Dp.Hairline
            }
        }
    }
}

internal fun Float.toPositiveDegrees(): Float {
    return if (this < 0f) {
        360f + this
    } else {
        this
    }
}

internal val MutableState<Offset>.x
    get() = value.x

internal val MutableState<Offset>.y
    get() = value.y

@Composable
internal fun ObserveOnPress(
    source : MutableInteractionSource,
    offsetState : MutableState<Offset>
) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(key1 = 0) {
        scope.launch {
            source.interactions.collectLatest { interaction ->
                if (interaction is PressInteraction.Release) {
                    val pressedOffset = interaction.press.pressPosition
                    if (pressedOffset != offsetState.value) {
                        offsetState.value = pressedOffset
                    }
                }
            }
        }
    }
}

internal fun checkAndUpdateClickedState(
    offset: MutableState<Offset>,
    pressedState: MutableState<Int>,
    radius: Float,
    startAngles: List<Float>,
    sweepAngles: Array<Float>,
    center: Offset,
    lineWidth: Float,
    itemClickListener: ZDPieChartOnClickListener?
) {
    val x = center.x
    val y = center.y
    val clickedRadius = sqrt(
        (x - offset.x) * (center.x - offset.x) +
                (y - offset.y) * (center.y - offset.y)
    )
    if (clickedRadius > (radius - lineWidth/2) && clickedRadius < (radius + lineWidth/2)) {
        val clickedAngle = (atan2(offset.x - center.x, center.y - offset.y) * DEG_TO_RAD_CONST - 90f).toPositiveDegrees()

        for (index in sweepAngles.indices) {
            val currentSweepAngle = sweepAngles[index]
            val currentStartAngle =
                (startAngles[index].toPositiveDegrees()) % 360f
            val currentEndAngle =
                (startAngles[index].toPositiveDegrees() + currentSweepAngle).toPositiveDegrees() % 360f

            val clickedOnCurrentIndex =
                when {
                    currentStartAngle < currentEndAngle -> {
                        if(currentSweepAngle > 0f) {
                            (clickedAngle > currentStartAngle) && (clickedAngle < currentEndAngle)
                        } else {
                            (clickedAngle > currentEndAngle) || (clickedAngle < currentStartAngle)
                        }
                    }

                    currentStartAngle > currentEndAngle -> {
                        if(currentSweepAngle < 0f) {
                            (clickedAngle < currentStartAngle) && (clickedAngle > currentEndAngle)
                        } else {
                            (clickedAngle > 0f && clickedAngle < currentEndAngle) ||
                                    (clickedAngle > currentStartAngle && clickedAngle < 360f)
                        }
                    }

                    else -> {
                        false
                    }
                }

            if (clickedOnCurrentIndex) {
                if (pressedState.value != index) {
                    pressedState.value = index
                    itemClickListener?.onItemClicked(index)
                } else {
                    pressedState.value = -1
                    itemClickListener?.onItemClicked(-1)
                }
                break
            }

            if(index == sweepAngles.lastIndex && !clickedOnCurrentIndex) {
                pressedState.value = -1
                itemClickListener?.onItemClicked(-1)
            }
        }
    }
    else {
        if(pressedState.value != -1) {
            itemClickListener?.onItemClicked(-1)
        }
        pressedState.value = -1
    }
    offset.value = Offset.Zero
}

internal fun Modifier.observeClickable(
    source: MutableInteractionSource,
    clickable: Boolean
) = this.clickable(
    enabled = clickable,
    interactionSource = source,
    indication = null,
    onClick = {}
)

internal fun lineChartAnimationCallBack(
    animationMap: MutableMap<ZDLineChartData, Boolean?>,
    bandwidthData: ZDBandwidthData,
    progressMap: MutableMap<ZDLineChartData, MutableState<Float>>,
    data: MutableState<Holder<ZDLineChartData>>,
    chartData: List<ZDLineChartData>,
    index: Int,
    emptyBandwidthData: ZDLineChartData,
    minMaxValue: MutableState<Pair<Float, Float>>?,
    maxState: MutableState<Float>?
) {
    animationMap[bandwidthData as ZDLineChartData] = false
    if (progressMap[bandwidthData]?.value == 0f) {
        animationMap.remove(bandwidthData)
        if (chartData.isNotEmpty()) {
            val list = data.value.bandwidthDataList.toMutableList()

            /**
             * Adding a dummy data to swap it with current data
             * and removing it to maintain animation
             */
            list.add(emptyBandwidthData)

            val lastIndex = list.lastIndex
            if (index != lastIndex) {
                list[index] = list[lastIndex]
                list[lastIndex] = bandwidthData
            }
            list.removeAt(lastIndex)
            data.value = Holder(list)

            val maxValue = data.lineChartData.getMaxSum()
            val minValue = data.lineChartData.getMinVal()
            if(maxState?.value != maxValue) {
                maxState?.value = data.lineChartData.getMaxSum()
            }
            if(minMaxValue?.value != Pair(minValue, maxValue)) {
                minMaxValue?.value = Pair(minValue, maxValue)
            }
        }
        if (animationMap.isEmpty()) {
            data.value = Holder(listOf())
        }
    }
}

internal fun addLineData(
    from: List<ZDLineChartData>,
    to: MutableList<ZDLineChartData>,
    animationMap: MutableMap<ZDLineChartData, Boolean?>,
    maxState: MutableState<Float>?,
    minMaxValue: MutableState<Pair<Float, Float>>?
):Boolean {
    var added = false
    var max = maxState?.value ?: minMaxValue?.value?.second ?: Float.NEGATIVE_INFINITY
    var min = minMaxValue?.value?.first ?: Float.POSITIVE_INFINITY
    from.forEach { data ->
        if(data !in to) {
            animationMap[data] = null
            to.add(data)
            val currentMax = data.dataValues.maxOf { it.value }
            val currentMin = data.dataValues.minOf { it.value }
            if(max < currentMax) {
                max = currentMax
            }
            if(min > currentMin) {
                min = currentMin
            }
            added = true
        }
    }
    maxState?.let {
        if (max > maxState.value) {
            maxState.value = max
        }
    }
    minMaxValue?.let {
        if(max > minMaxValue.value.second || min < minMaxValue.value.first) {
            minMaxValue.value = Pair(min, max)
        }
    }

    return added
}

internal fun ZDDataValue.getValue(verticalShiftProperty: ZDVerticalShiftProperty):Float {
    val minMaxRange = verticalShiftProperty.maxShiftValue - verticalShiftProperty.minShiftValue
    return ((value - verticalShiftProperty.minShiftValue) / minMaxRange) * verticalShiftProperty.maxShiftValue
}


fun isEqual(first: List<ZDBarData>, second: List<ZDBarData>): Boolean {
    if (first.size != second.size) {
        return false
    }
    return first.zip(second).all { (x, y) ->
        return if(x.dataValues.size != y.dataValues.size) {
            false
        } else {
            x.dataValues.zip(y.dataValues).all { (x1, y1) ->
                if(x1.size != y1.size) {
                    false
                } else {
                    x1.zip(y1).all { (x2, y2) ->
                        x2.value == y2.value
                    }
                }
            }
        }
    }
}