package com.abhilash.weighttracker.chart.chart.ui.circular

import androidx.annotation.FloatRange
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abhilash.weighttracker.chart.chart.data.*

/**
 * Used to draw full pie chart and donut chart.
 * Depending on start and sweep angle, half pie can also be drawn
 * @param [startAngle] Angle at which the chart should start
 * @param [sweepAngle] Angle at which the chart should be drawn from the start angle
 * @param [spacingAngle] Angle of spacing in between each segment in case of pie / donut chart
 * @param [spacingWidth] Width of spacing in between each segment (Will only be considered if [spacingAngle] is 0)
 * @param [spacingWidth] Width of spacing (will be taken only if spacing angle is zero)
 * @param [scalePercentOnClick] percentage of line width to be increased if user taps on it
 * @param [lineWidth] width of the line to draw for radial chart, In case of pie chart, this decides whether the chart is full pie chart or donut chart
 * @param [meterText] Text to display on chart
 * @param [animationTime] duration of animation
 * @param [animationDelay] initial delay to start the animation
 * @param [itemClickListener] adapter to listen to click events in pie chart
 */

@Composable
fun zDPieChart(
    modifier: Modifier = Modifier,
    data:List<ZDCircularChartData>,
    @FloatRange(from = 0.0, to = 360.0)
    startAngle: Float = 180f,
    @FloatRange(from = 0.0, to = 360.0)
    sweepAngle: Float = 180f,
    @FloatRange(from = 0.0, to = 20.0)
    spacingAngle: Float = 0f,
    spacingWidth:Dp = Dp.Hairline,
    @androidx.annotation.IntRange(from = 0L, to = 100L)
    scalePercentOnClick: Int = 20,
    animationTime: Int = 0,
    animationDelay: Int = 0,
    meterText: ZDMeterText? = null,
    lineWidth:Dp = 2.dp,
    itemClickListener: ZDPieChartOnClickListener? = null
): ZDPieChartSetAdapter? {
    return zDCircularChart(
        modifier = modifier,
        chartData = data,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        spacingAngle = spacingAngle,
        spacingWidth = spacingWidth,
        animationTime = animationTime,
        animationDelay = animationDelay,
        strokeCap = StrokeCap.Butt,
        scalePercentOnClick = scalePercentOnClick,
        meterText = meterText,
        lineWidth = lineWidth,
        radialProgressStyle = null,
        progressBackground = Color.Transparent,
        drawForMinSize = true,
        itemClickListener = itemClickListener
    )
}

/**
 * Radial progress chart is used to display progress as full progress chart and dial chart.
 *
 * @param [startAngle] Angle at which the chart should start
 * @param [sweepAngle] Angle at which the chart should be drawn from the start angle
 * @param [lineWidth] width of the line to draw for radial chart, In case of pie chart, this decides whether the chart is full pie chart or donut chart
 * @param [strokeCap] Stroke end Cap
 * @param [radialProgressStyle] Style such as leading pointer and needle style for progress and dial chart and progress of chart
 * @param [drawForMinSize] whether the radius show be equal to minimum of height and width
 * (Will Only be considered if absolute value of [sweepAngle] is 180 && absolute value of [startAngle] is 0, 90, 180, 270, 360)
 * @param [meterText] Text to display on chart
 * @param [progressBackground] background color of progress Data
 * @param [animationTime] duration of animation
 * @param [animationDelay] initial delay to start the animation
 */

@Composable
fun ZDRadialProgressChart(
    modifier: Modifier = Modifier,
    data: ZDCircularChartData,
    @FloatRange(from = 0.0, to = 360.0)
    startAngle: Float = 180f,
    @FloatRange(from = 0.0, to = 360.0)
    sweepAngle: Float = 180f,
    animationTime: Int = 0,
    animationDelay: Int = 0,
    meterText: ZDMeterText? = null,
    lineWidth:Dp = 2.dp,
    drawForMinSize:Boolean = false,
    radialProgressStyle: ZDRadialProgressStyle = ZDRadialProgressStyle(),
    strokeCap: StrokeCap? = null,
    progressBackground: Color = data.dataColor.copy(alpha = 0.2f)
) {
    zDCircularChart(
        modifier = modifier,
        chartData = listOf(data),
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        spacingAngle = 0f,
        spacingWidth = Dp.Hairline,
        lineWidth = lineWidth,
        scalePercentOnClick = 0,
        drawForMinSize = drawForMinSize,
        strokeCap = strokeCap,
        progressBackground = progressBackground,
        radialProgressStyle = radialProgressStyle,
        animationTime = animationTime,
        animationDelay = animationDelay,
        meterText = meterText,
        itemClickListener = null
    )
}