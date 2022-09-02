package com.example.weighttracker.ui.util

import android.widget.Toast
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.weighttracker.ui.screens.cardShape
import com.example.weighttracker.ui.screens.colorList
import com.example.weighttracker.ui.screens.mainColor
import kotlin.math.sin


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

@Composable
fun WTBackgroundScreen(
    modifier: Modifier = Modifier,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .screenBackground(
                scrollState = rememberScrollState(),
                scrollEnabled = scrollable
            )
    ) {
        content()
    }
}
@Composable
fun WTCard(
    modifier: Modifier,
    padding: PaddingValues = PaddingValues(0.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .cardShadow()
            .background(Color.White)
            .padding(padding)
    ) {
        content()
    }
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

fun Modifier.cardShadow(): Modifier {
    return this
        .shadow(
            elevation = 8.dp,
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
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 76.dp)
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
