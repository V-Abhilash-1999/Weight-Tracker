package com.example.weighttracker.ui.layout.form

import androidx.compose.foundation.clickable
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
                labelItem: @Composable () -> Unit,
                formItem: @Composable () -> Unit
            ) {
                var headerText: String? = null
                var textStyle: TextStyle = TextStyle.Default

                var formType: FormType? = null
                var isHandled = true

                var rowModifier: Modifier = Modifier
                var headerModifier: Modifier = Modifier
                var formModifier: Modifier = Modifier
                /**
                 * Running through all modifiers nested in main modifier
                 */
                modifier.any { mod ->
                    headerText?.let {
                        headerModifier = headerModifier.then(mod)
                    } ?: formType?.let {
                        formModifier = formModifier.then(mod)
                    } ?: run {
                        rowModifier = rowModifier.then(mod)
                    }
                    when (mod) {
                        is HeaderModifier -> {
                            headerText = mod.text
                            textStyle = mod.textStyle
                        }
                        is FormTypeModifier -> {
                            formType = mod.formType
                            isHandled = mod.isHandled
                        }
                        else -> {

                        }
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
                val formRow = @Composable {
                    if (formType != null && !isHandled) {
                        Box(
                            modifier = Modifier
                                .clickable {
                                    //ABHI Implement Date/Time Picker for compose
                                    when (formType) {
                                        FormType.DATE -> {

                                        }
                                        FormType.TIME -> {

                                        }
                                        FormType.DATE_TIME -> {

                                        }
                                        else -> {

                                        }
                                    }
                                }
                        ) {
                            formItem()
                        }
                    } else {
                        formItem()
                    }
                }

                Row(
                    modifier = rowModifier,
                    verticalAlignment = verticalAlignment,
                    horizontalArrangement = horizontalArrangement
                ) {
                    labelItem()
                    formRow()
                }
            }

            override fun Modifier.header(
                text: String?,
                textStyle: TextStyle
            ): Modifier = HeaderModifier(otherModifier = this, text = text, textStyle = textStyle)

            override fun Modifier.formType(
                formType: FormType,
                isHandled: Boolean
            ): Modifier = FormTypeModifier(otherModifier = this, formType = formType, isHandled = isHandled)

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
        labelItem: @Composable () -> Unit,
        formItem: @Composable () -> Unit
    )

    fun Modifier.header(text: String?, textStyle: TextStyle): Modifier

    fun Modifier.formType(formType: FormType, isHandled: Boolean): Modifier
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
@Stable
class FormTypeModifier(
    private val otherModifier: Modifier,
    val formType: FormType,
    val isHandled: Boolean
):  Modifier.Element {
    override fun <R> foldIn(initial: R, operation: (R, Modifier.Element) -> R): R {
        return otherModifier.foldIn(otherModifier.foldIn(initial, operation), operation)
    }

    override fun <R> foldOut(initial: R, operation: (Modifier.Element, R) -> R): R {
        return otherModifier.foldOut(otherModifier.foldOut(initial, operation), operation)
    }

    override fun toString() = "FormTypeModifier(formType = $formType, isHandled = $isHandled)"
}

enum class FormType {
    DATE,
    TIME,
    DATE_TIME,
    STRING,
    NUMBER
}