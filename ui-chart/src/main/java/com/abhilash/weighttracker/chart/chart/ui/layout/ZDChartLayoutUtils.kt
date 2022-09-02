package com.abhilash.weighttracker.chart.chart.ui.layout

import androidx.compose.animation.core.*
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.gestures.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.Placeable
import com.abhilash.weighttracker.chart.chart.data.ZDXLabelOrientation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin

internal enum class ChartLayout {
    X_AXIS,
    Y_AXIS,
    CHART,
    X_AXIS_LENGTH,
    BACKGROUND_CANVAS,
    CHART_WITH_LABELS,
    TOP_LABELS,
    BOTTOM_LABELS
}

internal fun calculateSpace(placeableList: List<Placeable>, layoutSpace: Int): Int {
    val totalSpace = placeableList.sumOf { it.height }
    val remainingSpace = layoutSpace - totalSpace
    val noOfLabels = if(placeableList.size > 1) {
        placeableList.size - 1
    } else {
        1
    }
    return remainingSpace / noOfLabels
}

internal fun calculateBottomSpacing(
    width: Int,
    height: Int,
    xLabelOrientation: ZDXLabelOrientation
) = ((width * sin(xLabelOrientation.angle.absoluteValue)) + (height * cos(xLabelOrientation.angle.absoluteValue))).toInt()

internal fun <T> List<T>.getZeroOnEmpty(function : List<T>.() -> Int) = if(isNotEmpty()) function() else 0

internal fun Modifier.enableSwipe(
    scrollStateAnimatable: Animatable<Float, AnimationVector1D>,
    pointerOffset: MutableFloatState,
    chartWidth: Float
) = composed {
    /**
     * Saving this State in remember instead of rememberSaveable because
     * we want to set it again when composable is removed from memory and added again
     * like lazy Column scrolling and orientation changes
     */
    val isScrollStateRestored = remember { mutableStateOf(false) }

    ScrollStateInitiater(scrollStateAnimatable, pointerOffset, isScrollStateRestored)

    if(isScrollStateRestored.value) {
        setData(
            pointerOffset = pointerOffset,
            currentScrollState = scrollStateAnimatable
        )
    }

    var resize by remember { mutableStateOf(0) }
    pointerInput(resize, chartWidth) {
        if(size.width == 0) {
            resize++
            return@pointerInput
        }
        val lowerBound = chartWidth - size.width
        if(lowerBound > 0 && size.width > 0) {
            scrollStateAnimatable.updateBounds(lowerBound = -lowerBound, upperBound = 0f)
            val decay = splineBasedDecay<Float>(this)
            coroutineScope {
                val velocityTracker = VelocityTracker()

                detectHorizontalDragGestures { change, _ ->
                    handleHorizontalDrag(
                        scrollStateAnimatable = scrollStateAnimatable,
                        change = change,
                        velocityTracker = velocityTracker,
                        decay = decay
                    )
                }
            }
        }
    }
}

fun CoroutineScope.handleHorizontalDrag(
    scrollStateAnimatable: Animatable<Float, AnimationVector1D>,
    change: PointerInputChange,
    velocityTracker: VelocityTracker,
    decay: DecayAnimationSpec<Float>
) {
    launch {
        scrollStateAnimatable.snapTo(
            scrollStateAnimatable.value + change.positionChange().x,
        )
    }

    val position = change.position.copy(y = 0f)
    velocityTracker.addPosition(
        timeMillis = change.uptimeMillis,
        position = position
    )

    val velocity = velocityTracker.calculateVelocity().x
    launch {
        scrollStateAnimatable.animateDecay(velocity, decay)
    }
}

fun setData(
    pointerOffset: MutableFloatState,
    currentScrollState: Animatable<Float, AnimationVector1D>
) {
    pointerOffset.value = currentScrollState.value
}

@Composable
fun ScrollStateInitiater(
    scrollStateAnimatable: Animatable<Float, AnimationVector1D>,
    scrollOffset: MutableFloatState,
    isScrollStateRestored: MutableState<Boolean>
) {
    LaunchedEffect(key1 = Unit) {
        scrollStateAnimatable.snapTo(scrollOffset.value)
        isScrollStateRestored.value = true
    }
}