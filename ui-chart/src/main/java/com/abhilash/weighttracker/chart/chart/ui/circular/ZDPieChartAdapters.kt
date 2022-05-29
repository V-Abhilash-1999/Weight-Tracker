package com.abhilash.weighttracker.chart.chart.ui.circular

import androidx.compose.runtime.Stable

@FunctionalInterface
@Stable
interface ZDPieChartOnClickListener {
    fun onItemClicked(clickedItemIndex: Int)
}

@FunctionalInterface
@Stable
interface ZDPieChartSetAdapter {
    fun setClickedItem(index: Int)
}