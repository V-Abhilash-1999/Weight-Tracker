package com.abhilash.weighttracker.chart.chart.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abhilash.weighttracker.chart.chart.data.*
import com.abhilash.weighttracker.chart.chart.data.ZDVerticalShiftProperty

internal val STAR_SIZE = 16.dp

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

internal fun List<ZDBandwidthData>.getWidth(spacing: Dp, removeBarWidth: Dp = Dp.Hairline): Dp {
    return if(this.isEmpty()){
        Dp.Hairline
    } else {
        when(this.first()) {
            is ZDLineChartData -> {
                val maxSize = maxOf {
                    if(it is ZDLineChartData) {
                        it.dataValues.size + 1
                    } else {
                        0
                    }
                }
                ((maxSize * spacing.value)).dp - spacing/2
            }

            is ZDBarData -> {
                (size * spacing.value).dp + (spacing / 2)
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

@Composable
internal fun Float.pxToDp() = with(LocalDensity.current) { (this@pxToDp / this.density).dp }

