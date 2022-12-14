package com.abhilash.weighttracker.chart.chart.data

import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.io.Serializable
import kotlin.math.*

internal class ZDBezierPoints(pX: Float, pY: Float, x: Float, y: Float) {
    var x1: Float = 0f
    var y1: Float = pY
    var x2: Float = 0f
    var y2: Float = 0f

    init {
        x1 = (x + pX) / 2
        x2 = (x + pX) / 2
        y1 = pY
        y2 = y
    }
}

enum class ZDGradientDirection {
    HORIZONTAL,
    VERTICAL
}

data class ZDGradientStyle(
    val direction: ZDGradientDirection,
    val colors: List<Color>
)

sealed class ZDDataPointStyle: Serializable

object None: ZDDataPointStyle()

/**
 * @param color defines the color for data pointer, if null line color will be applied
 */
data class Filled(
    val size: Size = Size(-1f, -1f),
    val spacing: Dp = 2.dp,
    @ColorInt
    val color: Int? = null
): ZDDataPointStyle()

/**
 * @param color defines the color for data pointer, if null line color will be applied
 */
data class Outline(
    val size: Size = Size(-1f, -1f),
    val width: Dp = 2.dp,
    val spacing: Dp = 2.dp,
    @ColorInt
    val color: Int? = null
): ZDDataPointStyle()

sealed class ZDBandwidthData : Serializable

/**
 * @param dataPointStyle overrides the point style defined for the specific line
 * @param lineWidth overrides the line width given in chart if greater than 0
 * @param lineStyle overrides the line style in chart if non null
 * @param linePathType will indicate how the path effect should be applied when drawing
 */
data class ZDLineChartData(
    val color: Color,
    val dataValues: List<ZDDataValue>,
    val dataPointStyle: ZDDataPointStyle = None,
    val lineWidth: Dp = Dp.Hairline,
    val lineStyle: ZDLineStyle? = null,
    val linePathType: ZDLinePathType = ZDLinePathType.LINE
) : ZDBandwidthData()

data class ZDCircularChartData(
    val data: Float,
    val dataColor: Color
) : ZDBandwidthData()

data class ZDVerticalProgressData(
    val color: Color,
    val bgColor: Color = color.copy(alpha = 0.2f),
    val bottomLabel: String,
    @IntRange(from = 0L, to = 100L)
    val fillPercent: Int,
    val showValueOnTop: Boolean = true,
    val topLabel: String? = null
) : ZDBandwidthData()

data class ZDBarDataValue(
    val value: Float,
    val color: Color
) : Serializable

/**
 * Outer List is used to indicate groups and inner list is used to indicate stacks in each group
 */
data class ZDBarData(
    val xLabel: String,
    val barTopLabel: String?,
    val dataValues: List<List<ZDBarDataValue>>
) : ZDBandwidthData()

data class ZDDataValue(
    var value:Float,
    val xLabel:String,
    val hintLabel:String?,
    val isAnomaly:Boolean = false
) : Serializable

@Stable
internal data class ZDVerticalShiftProperty(
    val mYMin: Float,
    val mYMax: Float,
    val calculateMaxShiftValues: Boolean = true
) {
    private val shiftIntervals: Float = 10.0.pow(floor(log10(abs(mYMax).toDouble()))).toFloat()
    val minShiftValue = (shiftIntervals * floor((mYMin / shiftIntervals).toDouble())).toFloat()
    val maxShiftValue = if(calculateMaxShiftValues) (shiftIntervals * ceil((mYMax / shiftIntervals).toDouble())).toFloat() else mYMax
    val numberOfShifts: Int
    val mainArr : List<Float>

    init {
        var mYgridNum = ((maxShiftValue - minShiftValue) / shiftIntervals).toInt()
        if (mYgridNum <= 1)
            mYgridNum *= 5
        else if (mYgridNum <= 4) mYgridNum *= 2
        this.numberOfShifts = mYgridNum
        val localList = mutableListOf<Float>()
        if(mYMin == 0f && mYMax == 0f) {
            localList.add(0f)
            localList.add(0f)
            localList.add(0f)
        }
        else {
            for (i in 0..mYgridNum) {
                localList.add(i/mYgridNum.toFloat())
            }
        }
        mainArr = localList
    }
}

@Stable
data class ZDBackgroundStyle(
    val backgroundColor:Color = Color.Transparent,
    val drawBgLines: Boolean = false,
    val bgLineColor: Color = Color.LightGray,
    val bgLinePathEffect: PathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 4f)),
    val strokeCap: StrokeCap = StrokeCap.Butt,
    val lineWidth: Dp = 1.dp
)

enum class ZDLineStyle {
    CURVED,
    STRAIGHT
}

enum class ZDLinePathType {
    DOT,
    GRID,
    LINE
}

internal enum class ZDProgress(
    val PROGRESS_AT_START: Float = 0f,
    val PROGRESS_COMPLETE: Float = 1f
) {
    PROGRESS
}

data class ZDLineChartStyle(
    val dataPointStyle: ZDDataPointStyle? = null,
    val gradientStyle: ZDGradientStyle? = null,
    val lineWidth: Dp = Dp.Hairline,
    val lineStyle: ZDLineStyle = ZDLineStyle.CURVED
)

@Stable
enum class ZDXLabelOrientation(val angle:Float) {
    STRAIGHT(0f),
    LEFT_SLANTED(-45F),
    RIGHT_SLANTED(45F)
}

data class ZDLabelTextStyles(
    val showBottomLabel: Boolean = true,
    val bottomLabelTextStyle:TextStyle = TextStyle.Default,
    val maxBottomLines: Int = Int.MAX_VALUE,
    val showDataLabel: Boolean = true,
    val dataLabelTextStyle:TextStyle = TextStyle.Default,
)

data class ZDChartPaddingValues(
    val chartPaddingXAxis: Dp = 0.dp,
    val chartPaddingYAxis: Dp = 0.dp,
)

data class ZDChartScrollState(
    val animatable: Animatable<Float, AnimationVector1D>,
    @IntRange(from = 0L, to = 100L)
    val scrollToPercent: MutableState<Int>,
)