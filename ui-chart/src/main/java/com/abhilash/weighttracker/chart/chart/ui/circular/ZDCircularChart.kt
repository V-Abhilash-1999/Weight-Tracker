package com.abhilash.weighttracker.chart.chart.ui.circular

import android.graphics.Paint
import android.graphics.Rect
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.times
import com.abhilash.weighttracker.chart.chart.data.*
import com.abhilash.weighttracker.chart.chart.utils.*
import com.abhilash.weighttracker.chart.chart.utils.DEG_TO_RAD_CONST
import com.abhilash.weighttracker.chart.chart.utils.ObserveOnPress
import com.abhilash.weighttracker.chart.chart.utils.checkAndUpdateClickedState
import com.abhilash.weighttracker.chart.chart.utils.observeClickable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * Circular Chart is the combination of both pie chart and radial progress chart.
 *
 * Single data in the list is considered as Radial Progress Chart.
 * If the size is more than 1, it is considered as pie chart.
 *
 * @param [startAngle] Angle at which the chart should start
 * @param [sweepAngle] Angle at which the chart should be drawn from the start angle
 * @param [spacingAngle] Angle of spacing in between each segment in case of pie / donut chart
 * @param [spacingWidth] Width of spacing in between each segment (Will only be considered if [spacingAngle] is 0)
 * @param [lineWidth] width of the line to draw for radial chart, In case of pie chart, this decides whether the chart is full pie chart or donut chart
 * @param [scalePercentOnClick] percentage of line width to be increased if user taps on it
 * @param [drawForMinSize] whether the radius show be equal to minimum of height and width
 * (Will Only be considered if absolute value of [sweepAngle] is 180 && absolute value of [startAngle] is 0, 90, 180, 270, 360)
 * @param [strokeCap] Stroke end Cap
 * @param [progressBackground] background color of progress Data
 * @param [animationTime] duration of animation
 * @param [animationDelay] initial delay to start the animation
 * @param [radialProgressStyle] Style such as leading pointer and needle style for progress and dial chart and progress of chart
 * @param [meterText] Text to display on chart
 * @param [itemClickListener] adapter to listen to click events in pie chart
 */

@Composable
internal fun zDCircularChart(
    modifier: Modifier = Modifier,
    chartData: List<ZDCircularChartData>,
    @FloatRange(from = 0.0, to = 360.0)
    startAngle: Float,
    @FloatRange(from = 10.0, to = 360.0)
    sweepAngle: Float,
    @FloatRange(from = 0.0, to = 20.0)
    spacingAngle: Float,
    spacingWidth: Dp,
    lineWidth: Dp,
    @IntRange(from = 0, to = 100)
    scalePercentOnClick: Int,
    drawForMinSize: Boolean,
    strokeCap: StrokeCap?,
    progressBackground: Color,
    radialProgressStyle: ZDRadialProgressStyle?,
    animationTime: Int,
    animationDelay: Int,
    meterText: ZDMeterText?,
    itemClickListener: ZDPieChartOnClickListener?
) : ZDPieChartSetAdapter? {

    val progressState = rememberSaveable { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    var initialValue by rememberSaveable {
        mutableStateOf(0f)
    }

    val animatable = remember {
        Animatable(
            initialValue = initialValue,
            visibilityThreshold = Spring.DampingRatioNoBouncy
        )
    }

    DisposableEffect(key1 = chartData, key2 = progressState.value) {
        progressState.value = radialProgressStyle?.progress ?: 100

        scope.launch {
            animatable.animateTo(
                targetValue = ((progressState.value / 100f) * sweepAngle),
                animationSpec = tween(
                    durationMillis = animationTime,
                    delayMillis = animationDelay,
                    easing = LinearEasing
                )
            ) {
                if (value == (progressState.value / 100f) * sweepAngle) {
                    progressState.value = radialProgressStyle?.progress ?: 100
                }
            }
        }

        onDispose {
            initialValue = (radialProgressStyle?.progress ?: 100) / 100f * sweepAngle
        }
    }

    val holder = getSaver<ZDCircularChartData> {
        Holder(it)
    }

    val data = rememberSaveable(stateSaver = holder) {
        mutableStateOf(
            value = Holder(chartData),
            policy = referentialEqualityPolicy()
        )
    }

    val enableClickable = chartData.size > 1

    val source = remember { MutableInteractionSource() }
    val offset = remember { mutableStateOf(Offset.Zero) }

    val pressedState = rememberSaveable { mutableStateOf(-1) }

    val setAdapter = remember {
        object : ZDPieChartSetAdapter {
            override fun setClickedItem(index: Int) {
                pressedState.value = index
            }
        }
    }

    ObserveOnPress(source = source, offsetState = offset)

    updateCircularChartData(
        chartType = if (radialProgressStyle == null) {
            ZDCircularChartType.PIE_CHART
        } else {
            ZDCircularChartType.RADIAL_PROGRESS
        },
        data = data,
        chartData = chartData,
        progressState = progressState,
        radialProgressStyle = radialProgressStyle,
        animationDuration = animationTime,
        animatable = animatable,
        scope = scope
    )

    Canvas(
        modifier = modifier
            .observeClickable(
                clickable = enableClickable && scalePercentOnClick > 0,
                source = source
            )
    ) {
        val canvasHeight = size.height
        val canvasWidth = size.width

        var lineWidthPx = lineWidth.toPx()
        var center = center
        val diameter = if(drawForMinSize && abs(sweepAngle) == 180f && abs(startAngle) in listOf(0f, 90f, 180f, 270f, 360f)) {
            val isSweepAnglePositive = sweepAngle > 0
            center = if(isSweepAnglePositive) {
                if (canvasHeight > canvasWidth) {
                    Offset(canvasWidth, canvasHeight / 2)
                } else {
                    Offset(canvasWidth / 2, canvasHeight)
                }
            } else {
                if (canvasHeight > canvasWidth) {
                    Offset(0f, canvasHeight / 2)
                } else {
                    Offset(canvasWidth / 2, 0f)
                }
            }

            when {
                canvasHeight < canvasWidth/2 -> {
                    if(canvasHeight < canvasWidth) canvasHeight * 2 else canvasWidth
                }
                canvasWidth < canvasHeight/2 -> {
                    if(canvasWidth < canvasHeight) canvasWidth * 2 else canvasHeight
                }
                else -> {
                    max(canvasHeight, canvasWidth)
                }
            }
        } else {
            min(canvasHeight, canvasWidth)
        }

        val adjLineWidth = if(lineWidthPx <= 0) diameter/2 else lineWidthPx
        val pressedWidth = adjLineWidth * scalePercentOnClick/100f

        if(lineWidthPx<=0)
            lineWidthPx = diameter/2

        val radius = diameter/2 - lineWidthPx/2 - pressedWidth/2

        val localSpacing = if(spacingAngle == 0f) {
            spacingWidth.toPx() / radius * DEG_TO_RAD_CONST
        } else {
            spacingAngle
        }

        val adjustedSpacing = if(sweepAngle < 0f) -localSpacing else localSpacing

        val arcSize = Size(diameter - adjLineWidth - pressedWidth, diameter - adjLineWidth - pressedWidth)

        val topLeft = Offset(
            x = center.x - radius,
            y = center.y - radius
        )

        val fillColor = if(data.circularChartData.isNotEmpty()) {
            data.circularChartData.first().dataColor
        } else {
            Color.Transparent
        }

        var localStartAngle = startAngle
        val startAngles = mutableListOf<Float>()

        val totalValue = data.circularChartData.map { it.data }.sum()
        val totalSpacing = adjustedSpacing * data.circularChartData.size

        val sweepAngles = Array(data.circularChartData.size) { index ->
            startAngles.add(localStartAngle)
            val calcSweepAngle = (data.circularChartData[index].data / totalValue) * (sweepAngle - totalSpacing)
            localStartAngle += calcSweepAngle + adjustedSpacing
            calcSweepAngle
        }

        if(enableClickable) {
            if (offset.value != Offset.Zero)
                checkAndUpdateClickedState(
                    offset = offset,
                    center = center,
                    startAngles = startAngles,
                    sweepAngles = sweepAngles,
                    radius = radius,
                    pressedState = pressedState,
                    lineWidth = lineWidthPx,
                    itemClickListener = itemClickListener
                )
        } else {
            if(data.circularChartData.isNotEmpty()) {
                sweepAngles[0] = (progressState.value / 100f) * sweepAngle
                val topLeftPoint = if(!enableClickable && sweepAngle == 180f) {
                    topLeft.copy(y = topLeft.y - lineWidthPx/2)
                } else {
                    topLeft
                }
                drawArc(
                    color = progressBackground,
                    topLeft = topLeftPoint,
                    size = arcSize,
                    style = Stroke(
                        width = lineWidthPx,
                        cap = strokeCap ?: StrokeCap.Round
                    ),
                    useCenter = false,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle
                )
            }
        }

        val animationAdjusters =
            if(progressState.value != 0) {
                (animatable.value / ((progressState.value / 100f) * sweepAngle))
            } else {
                (animatable.value / sweepAngle)
            }

        data.circularChartData.forEachIndexed { index, value ->
            val animatedSweepAngle = if (enableClickable) {
                animationAdjusters * sweepAngles[index]
            } else {
                animatable.value
            }

            drawPieArc(
                startAngle = startAngles[index],
                sweepAngle = animatedSweepAngle,
                isHalfSweepAngle = sweepAngle == 180f,
                strokeCap = strokeCap,
                color = value.dataColor,
                arcSize = arcSize,
                enableClickable = enableClickable,
                index = index,
                scalePercentOnClick = scalePercentOnClick,
                lineWidth = lineWidthPx,
                pressedIndex = pressedState.value,
                topLeft = topLeft
            )
        }

        if(!enableClickable) {
            if(radialProgressStyle?.pointerStyle != null || radialProgressStyle?.leadingIndicator != null) {
                val pointerCenter = center.copy(y = center.y - lineWidthPx/2)

                val path = radialProgressStyle.pointerStyle?.let { needleStyle ->
                   drawNeedlePointer(needleStyle = needleStyle, fillColor = fillColor, radius = radius, center = pointerCenter)
                }

                withTransform(
                    transformBlock = {
                        rotate(
                            degrees = (startAngle + animatable.value) + 180,
                            pivot = pointerCenter
                        )
                    },

                    drawBlock = {
                        path?.let {
                            drawPath(
                                path = path,
                                color = fillColor,
                            )
                            path.close()
                        }

                        radialProgressStyle.leadingIndicator?.let { leadingIndicator ->
                            drawLeadingIndicator(leadingIndicator = leadingIndicator, diameter = diameter, lineWidth = lineWidthPx, center = pointerCenter)
                        }

                    }
                )

            }

            meterText?.let { meterText ->
                if(meterText.count > 0) {
                    drawRadialMeterText(
                        meterText = meterText,
                        radius = radius,
                        lineWidth = lineWidthPx,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        center = center
                    )
                } else {
                    drawPieText(
                        meterText = meterText,
                        lineWidth = lineWidthPx,
                        radius = radius,
                        data = data.circularChartData,
                        startAngles = startAngles,
                        sweepAngles = sweepAngles,
                        center = center
                    )
                }
            }
        } else if(sweepAngle != 0f) {
            meterText?.let { meterText ->
                drawPieText(
                    meterText = meterText,
                    lineWidth = lineWidthPx,
                    radius = radius,
                    data = data.circularChartData,
                    startAngles = startAngles,
                    sweepAngles = sweepAngles,
                    center = center
                )
            }
        }
    }
    return if(radialProgressStyle == null) setAdapter else null
}

private fun DrawScope.drawRadialMeterText(
    meterText: ZDMeterText,
    lineWidth: Float,
    startAngle: Float,
    sweepAngle: Float,
    radius: Float,
    center: Offset
) {
    val meterTextSpacing = meterText.meterSpacing.toPx()
    val angleSpacing = sweepAngle / (meterText.count - 1)
    val paint = Paint().apply {
        color = meterText.textStyle.color.toArgb()
        this.textSize = meterText.textStyle.fontSize.toPx()
    }
    val meterRadius = radius - lineWidth/2 - meterTextSpacing
    var value = meterText.startValue.toFloat()
    val valueInc = (meterText.endValue - meterText.startValue).toFloat() / (meterText.count - 1)
    var angle = 0f
    val multiplier = if(sweepAngle < 0) {
        -1
    } else {
        0
    }
    val textBounds = Rect()
    while (value <= meterText.endValue) {
        val text = "${value.toInt()}"
        val calcAngle = (startAngle + (angle * multiplier)) * Math.PI.toFloat() / 180

        drawMeterText(
            radius = meterRadius,
            text = text,
            paint = paint,
            angle = calcAngle,
            textBounds = textBounds,
            center = center
        )

        value += valueInc

        if (sweepAngle != 0f) {
            angle += angleSpacing * multiplier
        } else {
            break
        }
    }
}

private fun DrawScope.drawPieText(
    meterText: ZDMeterText,
    lineWidth: Float,
    radius: Float,
    data:List<ZDCircularChartData>,
    startAngles:List<Float>,
    sweepAngles: Array<Float>,
    center: Offset
) {
    val meterTextSpacing = meterText.meterSpacing.toPx()
    val paint = Paint().apply {
        color = meterText.textStyle.color.toArgb()
        this.textSize = meterText.textStyle.fontSize.toPx()
    }
    val meterRadius = radius - lineWidth/2 - meterTextSpacing
    val textBounds = Rect()
    for (index in data.indices) {
        val currAngle = (startAngles[index] + (sweepAngles[index] / 2)) * Math.PI.toFloat() / 180
        val text = "${data[index].data.toInt()}"

        drawMeterText(
            radius = meterRadius,
            text = text,
            angle = currAngle,
            paint = paint,
            textBounds = textBounds,
            center = center
        )
    }
}

private fun DrawScope.drawPieArc(
    startAngle: Float,
    sweepAngle: Float,
    color:Color,
    arcSize: Size,
    topLeft: Offset,
    lineWidth : Float,
    index: Int,
    pressedIndex: Int,
    enableClickable:Boolean,
    scalePercentOnClick: Int,
    strokeCap: StrokeCap?,
    isHalfSweepAngle: Boolean
) {
    val pressedWidth = if(index == pressedIndex) {
        lineWidth * (scalePercentOnClick/100f)
    } else {
        0f
    }
    val topLeftPoint = if(!enableClickable && isHalfSweepAngle) {
        topLeft.copy(y = topLeft.y - lineWidth/2)
    } else {
        topLeft
    }
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = false,
        size = arcSize,
        topLeft = topLeftPoint,
        style = Stroke(
            width = lineWidth + pressedWidth,
            cap = strokeCap ?: run {
                if (enableClickable) {
                    StrokeCap.Butt
                } else {
                    StrokeCap.Round
                }
            }
        )
    )
}

private fun DrawScope.drawMeterText(
    radius:Float,
    text: String,
    angle: Float,
    textBounds: Rect,
    paint:Paint,
    center:Offset
) {
    paint.getTextBounds(text, 0, text.length, textBounds)

    val x = center.x + (radius * cos(angle)) - textBounds.width() / 2
    val y = center.y + (radius * sin(angle)) + textBounds.height() / 2

    drawIntoCanvas {
        it.nativeCanvas.drawText(text, x, y, paint)
    }

    textBounds.times(0)
}

private fun updateCircularChartData(
    chartType: ZDCircularChartType,
    data: MutableState<Holder<ZDCircularChartData>>,
    chartData: List<ZDCircularChartData>,
    progressState: MutableState<Int>,
    animationDuration: Int,
    radialProgressStyle: ZDRadialProgressStyle?,
    animatable: Animatable<Float, AnimationVector1D>,
    scope: CoroutineScope
) {
    if(animationDuration == 0 || animatable.isRunning || animatable.value != 0f) {
        progressState.value = radialProgressStyle?.progress ?: 100
    }
    if(data.circularChartData != chartData) {
        if(chartType == ZDCircularChartType.PIE_CHART) {
            progressState.value = 0
            scope.launch {
                animatable.snapTo(targetValue = 0f)
            }
        } else {
            progressState.value = radialProgressStyle?.progress ?: 100
        }
        data.value = Holder(chartData)
    }
}

private fun DrawScope.drawNeedlePointer(
    needleStyle: ZDNeedlePointerStyle,
    fillColor: Color,
    radius: Float,
    center:Offset
):Path {
    val path = Path()
    val pointerWidth = needleStyle.needlePointWidth.toPx()
    val pointerLength = if(needleStyle.needlePointLength == Dp.Hairline) {
        radius/2
    } else {
        needleStyle.needlePointLength.toPx()
    }

    path.apply {
        moveTo(center.x, center.y - pointerWidth)
        lineTo(center.x - pointerLength, center.y)
        lineTo(center.x, center.y + pointerWidth)
    }

    val pointRadius = needleStyle.needleBaseRadius.toPx()
    drawCircle(
        fillColor,
        center = center,
        radius = pointRadius
    )
    return path
}

private fun DrawScope.drawLeadingIndicator(
    leadingIndicator: ZDLeadingIndicator,
    diameter:Float,
    lineWidth: Float,
    center:Offset
) {
    drawCircle(
        color = leadingIndicator.pointerColor,
        radius = leadingIndicator.pointerRadius.toPx(),
        center = Offset(
            x = center.x - diameter / 2 + lineWidth / 2,
            y = center.y
        )
    )
}