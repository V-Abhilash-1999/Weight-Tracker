package com.example.weighttracker.ui.util

import android.widget.Toast
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.weighttracker.ui.screens.cardShape
import com.example.weighttracker.ui.screens.colorList
import com.example.weighttracker.ui.screens.mainColor
import kotlin.math.sin


val screenPaddingValues = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 76.dp)

@Composable
fun MakeToast(
    message: String,
    length: Int = Toast.LENGTH_SHORT
) {
    LocalContext.current.makeToast(
        message = message,
        length = length
    )
}

fun Modifier.wavyBackground(
    waveColor: Color,
    background: Color
): Modifier {
    return this.drawBehind {
        val canvasHeight = size.height
        val canvasWidth = size.width
        drawRect(color = background)
        val path = Path()
        path.moveTo(canvasWidth, 0f)
        path.lineTo(0f, 0f)
        val waveHeight = canvasHeight * 0.4f
        path.lineTo(0f, waveHeight)
        (0..canvasWidth.toInt()).forEach {
            val x = sin(it.toFloat())
            path.lineTo(x, waveHeight)
        }

        drawPath(
            path = path,
            color = waveColor
        )
    }
}

fun Modifier.cardShadow(
    elevation: Dp = 8.dp
): Modifier {
    return this
        .shadow(
            elevation = elevation,
            shape = cardShape,
            clip = true,
            ambientColor = mainColor,
            spotColor = mainColor
        )
}

fun Modifier.screenBackground(
    scrollState: ScrollState,
    scrollEnabled: Boolean = true
): Modifier {
    return if(scrollEnabled) {
        this
            .fillMaxSize()
            .background(
                brush = Brush
                    .verticalGradient(
                        colors = colorList
                    )
            )
            .verticalScroll(state = scrollState)
            .padding(screenPaddingValues)
    } else {
        this
            .fillMaxSize()
            .background(
                brush = Brush
                    .verticalGradient(
                        colors = colorList
                    )
            )
            .padding(8.dp)
    }
}

@Composable
fun WTSnackBar(
    modifier: Modifier,
    text: String
) {
    val snackBarState = rememberSnackBarHostState()
    SnackbarHost(
        modifier = modifier.padding(horizontal = 8.dp),
        hostState = snackBarState.value,
    ) {
        Snackbar(
            snackbarData = WTSnackBarData(text){

            },
            backgroundColor = Color.Black,
            elevation = 32.dp,
            shape = RoundedCornerShape(16.dp),
        )
    }
    LaunchedEffect(key1 = Unit) {
        snackBarState.value.showSnackbar(text, null)
    }
}

@Composable
fun rememberSnackBarHostState() = remember {
    mutableStateOf(SnackbarHostState())
}

class WTSnackBarData(
    private val snackBarMessage: String,
    private val onClick: () -> Unit
): SnackbarData {
    override val actionLabel: String? = null
    override val duration: SnackbarDuration
        get() = SnackbarDuration.Short
    override val message: String
        get() = snackBarMessage

    override fun dismiss() {
    }

    override fun performAction() {
        onClick()
    }
}
