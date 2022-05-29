package com.abhilash.weighttracker.chart.chart.utils

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.graphics.Color
import com.abhilash.weighttracker.chart.chart.data.ZDBarData
import com.abhilash.weighttracker.chart.chart.data.ZDCircularChartData
import com.abhilash.weighttracker.chart.chart.data.ZDVerticalProgressData
import com.abhilash.weighttracker.chart.chart.data.ZDLineChartData


data class Holder<T> (var bandwidthDataList: List<T>)

fun <T> getSaver(
    onSave : (Holder<T>) -> List<T> = { it.bandwidthDataList },
    onRestore : (List<T>) -> Holder<T>,
): Saver<Holder<T>, List<T>> {
    return Saver(
        save = { onSave(it) },
        restore = { onRestore(it) }
    )
}


val MutableState<Holder<ZDLineChartData>>.lineChartData
    get() = this.value.bandwidthDataList
        .filterNot {
            it == ZDLineChartData(
                color = Color.Transparent,
                dataValues = listOf()
            )
        }

val MutableState<Holder<ZDCircularChartData>>.circularChartData
    get() = this.value.bandwidthDataList

val MutableState<Holder<ZDBarData>>.barData
    get() = this.value.bandwidthDataList

val MutableState<Holder<ZDVerticalProgressData>>.fillChartData
    get() = this.value.bandwidthDataList
