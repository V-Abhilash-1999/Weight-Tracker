package com.abhilash.weighttracker.chart.chart.data

interface ZDChartDataAdapter<T> {
    fun setData(list:List<T>)

    fun addData(data:T):List<T>

    fun addDataAt(index : Int, data:T):List<T>

    fun removeDataAt(index:Int):List<T>

    fun updateData(index:Int, data: T): List<T> = listOf()

    fun removeData(data: T): List<T> = listOf()

    fun removeAll(data: List<T>): List<T> = listOf()

    fun clear(): List<T> = listOf()
}
