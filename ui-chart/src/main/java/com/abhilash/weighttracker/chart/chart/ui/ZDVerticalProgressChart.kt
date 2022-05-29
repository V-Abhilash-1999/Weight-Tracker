package com.abhilash.weighttracker.chart.chart.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abhilash.weighttracker.chart.chart.data.ZDLabelTextStyles
import com.abhilash.weighttracker.chart.chart.data.ZDProgress
import com.abhilash.weighttracker.chart.chart.data.ZDVerticalBarSize
import com.abhilash.weighttracker.chart.chart.data.ZDVerticalProgressData
import com.abhilash.weighttracker.chart.chart.utils.Holder
import com.abhilash.weighttracker.chart.chart.utils.fillChartData
import com.abhilash.weighttracker.chart.chart.utils.getSaver
import com.abhilash.weighttracker.chart.chart.ui.layout.ZDFillChartLayout

/**
 * Fill Chart is the used to represent the vertical progress bars.
 *
 * @param [verticalBarSize] Size attributes for bar such as
 *                bar width,
 *                top and bottom padding for each bar
 * @param [labelTextStyles] Text Styles for both top and bottom labels
 * @param [animationTimeMillis] Duration of animation
 * @param [animationDelayMillis] Initial delay of animation
 * @param [dataSpacing] Spacing between each bar
 */

@Composable
fun ZDVerticalProgressChart(
    modifier: Modifier = Modifier,
    data:List<ZDVerticalProgressData>,
    verticalBarSize: ZDVerticalBarSize = ZDVerticalBarSize(),
    labelTextStyles: ZDLabelTextStyles = ZDLabelTextStyles(),
    animationTimeMillis: Int = 1000,
    animationDelayMillis: Int = 0,
    dataSpacing: Dp = 32.dp,
) {
    ZDFillChartLayout(
        modifier = modifier,
        chartWidth = (data.size * dataSpacing.value).dp,
        dataSpacing = dataSpacing,
        topLabels = {
            if(labelTextStyles.showDataLabel) {
                data.forEach { fillData ->
                    val text = if (fillData.showValueOnTop) {
                        "${fillData.fillPercent}%"
                    } else {
                        fillData.topLabel
                    }
                    text?.let {
                        Text(
                            text = it,
                            style = labelTextStyles.dataLabelTextStyle
                        )
                    }
                }
            }
        },
        bottomLabels = {
            if(labelTextStyles.showBottomLabel) {
                data.forEach { fillData ->
                    Text(
                        text = fillData.bottomLabel,
                        style = labelTextStyles.bottomLabelTextStyle
                    )
                }
            }
        }
    ) {
        ZDFillChartSegment(
            data = data,
            verticalBarSize = verticalBarSize,
            dataSpacing = dataSpacing,
            animationDelay = animationDelayMillis,
            animationDuration = animationTimeMillis
        )
    }
}

@Composable
fun ZDFillChartSegment(
    data: List<ZDVerticalProgressData>,
    verticalBarSize: ZDVerticalBarSize,
    dataSpacing: Dp,
    animationDelay: Int,
    animationDuration: Int
) {
    val saver = getSaver<ZDVerticalProgressData> {
        Holder(
            if(data.isNotEmpty()) {
                it
            } else {
                listOf()
            }
        )
    }
    val savedData = rememberSaveable(stateSaver = saver) {
        mutableStateOf(Holder(data))
    }

    val progressMap = data.associateWith {
        rememberSaveable {
            mutableStateOf(ZDProgress.PROGRESS.PROGRESS_AT_START)
        }
    }
    val animationMap = data.associateWith {
        animateFloatAsState(
            targetValue = (progressMap[it]?.value ?: 1f) * it.fillPercent,
            animationSpec = TweenSpec(
                durationMillis = animationDuration,
                delay = animationDelay,
                easing = LinearEasing
            )
        )
    }

    updateData(
        data = data,
        savedData = savedData
    )

    ZDFillChartCanvas(
        data = savedData.fillChartData,
        verticalBarSize = verticalBarSize,
        dataSpacing = dataSpacing,
        progressMap = progressMap,
        animationMap = animationMap
    )
}

private fun updateData(
    data: List<ZDVerticalProgressData>,
    savedData: MutableState<Holder<ZDVerticalProgressData>>
) {
    if(data != savedData.fillChartData) {
        savedData.value = Holder(data)
    }
}

@Composable
private fun ZDFillChartCanvas(
    data: List<ZDVerticalProgressData>,
    verticalBarSize: ZDVerticalBarSize,
    dataSpacing: Dp,
    progressMap: Map<ZDVerticalProgressData, MutableState<Float>>,
    animationMap: Map<ZDVerticalProgressData, State<Float>>
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = verticalBarSize.barTopSpacing, bottom = verticalBarSize.barBottomSpacing)
    ) {
        val canvasHeight = size.height
        val barWidth = verticalBarSize.barWidth.toPx()
        val spacing = dataSpacing.toPx()

        data.forEachIndexed { index, fillData ->
            drawVerticalProgress(
                progressValue = progressMap[fillData],
                animationValue = animationMap[fillData],
                index = index,
                fillData = fillData,
                canvasHeight = canvasHeight,
                barWidth = barWidth,
                spacing = spacing
            )
        }
    }
}

private fun DrawScope.drawVerticalProgress(
    progressValue: MutableState<Float>?,
    animationValue: State<Float>?,
    index: Int,
    spacing: Float,
    fillData: ZDVerticalProgressData,
    canvasHeight: Float,
    barWidth: Float
) {
    progressValue?.value = ZDProgress.PROGRESS.PROGRESS_COMPLETE
    val x = (index * spacing) + spacing/2
    drawLine(
        color = fillData.bgColor,
        start = Offset(x, canvasHeight - barWidth/2),
        end = Offset(x, 0f + barWidth/2),
        strokeWidth = barWidth,
        cap = StrokeCap.Round
    )

    val fillHeight = (canvasHeight - barWidth) * (animationValue?.value ?: 100f) / 100
    drawLine(
        color = fillData.color,
        start = Offset(x, canvasHeight - barWidth/2),
        end = Offset(x, canvasHeight + barWidth/2 - fillHeight),
        strokeWidth = barWidth,
        cap = StrokeCap.Round
    )
}