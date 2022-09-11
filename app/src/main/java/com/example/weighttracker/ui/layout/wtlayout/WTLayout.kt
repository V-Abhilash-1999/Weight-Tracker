package com.example.weighttracker.ui.layout.wtlayout

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.TextFieldColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.weighttracker.ui.screens.mainColor
import com.example.weighttracker.ui.util.cardShadow
import com.example.weighttracker.ui.util.screenBackground
import com.example.weighttracker.ui.util.sizeInDp


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
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .cardShadow()
            .background(Color.White)
            .padding(contentPadding)
    ) {
        content()
    }
}

@Composable
fun WTCircularProgressBar(
    showLoader: Boolean,
    modifier: Modifier = Modifier,
    color: Color = mainColor,
    size: Int = 16,
    strokeWidth: Dp = 2.dp
) {
    if(showLoader) {
        CircularProgressIndicator(
            modifier = modifier
                .sizeInDp(size),
            color = color,
            strokeWidth = strokeWidth
        )
    }
}

@Composable
fun CircularImage(
    modifier: Modifier = Modifier,
    size: Int,
    painter: Painter,
    contentDescription: String? = null
) {
    val imageShape = RoundedCornerShape((size * 2).dp)
    Image(
        modifier = modifier
            .sizeInDp(size)
            .clip(imageShape)
            .border(1.dp, mainColor, imageShape),
        painter = painter,
        contentScale = ContentScale.Crop,
        contentDescription = contentDescription
    )
}


@Composable
fun CircularImageWithLoader(
    modifier: Modifier,
    size: Int,
    painter: Painter,
    showLoader: Boolean,
    contentDescription: String? = null
) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        CircularImage(
            modifier = modifier,
            size = size,
            painter = painter,
            contentDescription = contentDescription
        )

        WTCircularProgressBar(showLoader)
    }
}

@Immutable
class WTProfileInputColors : TextFieldColors {
    @Composable
    override fun backgroundColor(enabled: Boolean): State<Color> = rememberUpdatedState(Color.Transparent)

    @Composable
    override fun cursorColor(isError: Boolean): State<Color>  = rememberUpdatedState(mainColor)

    @Composable
    override fun indicatorColor(
        enabled: Boolean,
        isError: Boolean,
        interactionSource: InteractionSource
    ): State<Color> = rememberUpdatedState(Color.Transparent)

    @Composable
    override fun labelColor(
        enabled: Boolean,
        error: Boolean,
        interactionSource: InteractionSource
    ): State<Color>  = rememberUpdatedState(Color.Transparent)

    @Composable
    override fun leadingIconColor(enabled: Boolean, isError: Boolean): State<Color>  = rememberUpdatedState(Color.Transparent)

    @Composable
    override fun placeholderColor(enabled: Boolean): State<Color>  = rememberUpdatedState(Color.LightGray)

    @Composable
    override fun textColor(enabled: Boolean): State<Color>  = rememberUpdatedState(Color.Black)

    @Composable
    override fun trailingIconColor(enabled: Boolean, isError: Boolean): State<Color>  = rememberUpdatedState(Color.Transparent)
}