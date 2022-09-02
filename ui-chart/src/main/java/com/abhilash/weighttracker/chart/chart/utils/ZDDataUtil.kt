package com.abhilash.weighttracker.chart.chart.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abhilash.weighttracker.chart.chart.data.*

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


val ZDDataPointStyle?.height: Dp
    @Composable get() = if(this == null) {
           0.dp
        } else {
            when(this) {
                None -> {
                    0.dp
                }
                is Filled -> {
                    size.height.pxToDp()
                }
                is Outline -> {
                    size.height.pxToDp()
                }
            }
        }
