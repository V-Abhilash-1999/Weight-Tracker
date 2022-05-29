package com.example.weighttracker.ui.layout

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Measured
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.text.TextStyle


@Composable
internal fun FormLayout(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content :@Composable FormScope.() -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment
    ) {
        val form = object : FormScope {

            @Composable
            override fun FormRow(
                modifier: Modifier,
                verticalAlignment: Alignment.Vertical,
                horizontalArrangement: Arrangement.Horizontal,
                labelItem: @Composable RowScope.() -> Unit,
                formItem: @Composable RowScope.() -> Unit
            ) {
                var headerText: String? = null
                var textStyle: TextStyle = TextStyle.Default
                var headerAlignment: Alignment.Horizontal = Alignment.CenterHorizontally
                var rowModifier: Modifier = Modifier
                var headerModifier: Modifier = Modifier
                modifier.any { mod ->
                    headerText?.let {
                        headerModifier = headerModifier.then(mod)
                    } ?: run {
                        rowModifier = rowModifier.then(mod)
                    }
                    if (mod is HeaderModifier) {
                        headerText = mod.text
                        textStyle = mod.textStyle
                    }
                    false
                }

                headerText?.let {
                    Text(
                        text = it,
                        modifier = headerModifier,
                        style = textStyle
                    )
                }

                Row(
                    modifier = rowModifier,
                    verticalAlignment = verticalAlignment,
                    horizontalArrangement = horizontalArrangement
                ) {
                    labelItem()
                    formItem()
                }
            }

            override fun Modifier.header(
                text: String?,
                textStyle: TextStyle
            ): Modifier = HeaderModifier(otherModifier = this, text = text, textStyle = textStyle)

            override fun Modifier.align(alignment: Alignment.Horizontal): Modifier {
                this@Column.apply {
                    return modifier
                        .then(this@align)
                        .align(alignment)
                }
            }
            override fun Modifier.alignBy(alignmentLineBlock: (Measured) -> Int): Modifier {
                this@Column.apply {
                    return modifier
                        .alignBy(alignmentLineBlock)
                        .then(this@alignBy)
                }
            }
            override fun Modifier.alignBy(alignmentLine: VerticalAlignmentLine): Modifier {
                this@Column.apply {
                    return modifier
                        .alignBy(alignmentLine)
                        .then(this@alignBy)

                }
            }
            override fun Modifier.weight(weight: Float, fill: Boolean): Modifier {
                this@Column.apply {
                    return modifier
                        .weight(weight = weight, fill = fill)
                        .then(this@weight)
                }
            }

        }
        form.content()
    }
}

@Stable
internal interface FormScope: ColumnScope {
    @Composable
    fun FormRow(
        modifier: Modifier,
        verticalAlignment: Alignment.Vertical,
        horizontalArrangement: Arrangement.Horizontal,
        labelItem: @Composable RowScope.() -> Unit,
        formItem: @Composable RowScope.() -> Unit
    )

    fun Modifier.header(text: String?, textStyle: TextStyle): Modifier
}

@Stable
class HeaderModifier(
    private val otherModifier: Modifier,
    val text: String?,
    val textStyle: TextStyle
):  Modifier.Element {
    override fun <R> foldIn(initial: R, operation: (R, Modifier.Element) -> R): R {
        return otherModifier.foldIn(otherModifier.foldIn(initial, operation), operation)
    }

    override fun <R> foldOut(initial: R, operation: (Modifier.Element, R) -> R): R {
        return otherModifier.foldOut(otherModifier.foldOut(initial, operation), operation)
    }

    override fun toString() = "HeaderModifierData(text = $text)"
}