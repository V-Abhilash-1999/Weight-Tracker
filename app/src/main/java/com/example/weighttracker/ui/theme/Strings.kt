package com.example.weighttracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle as ComposeTextStyle

enum class Language {
    ENGLISH,
    TAMIL,
    MALAYALAM
}

enum class TextStyle {
    NORMAL,
    BOLD_STYLE,
    ITALIC_STYLE,
    MEDIUM_NORMAL,
    MEDIUM_BOLD_STYLE,
    MEDIUM_ITALIC_STYLE,
    LARGE_NORMAL,
    LARGE_BOLD_STYLE,
    LARGE_ITALIC_STYLE,
}

class LanguageStrings(
    val doneBy: String,
    val name: String,
    val weightTracker:String
)

fun localStrings(
    doneBy: String = "Done By",
    name: String = "Abhilash",
    weightTracker: String = "Weight Tracker"
): LanguageStrings = LanguageStrings(
    doneBy = doneBy,
    name = name,
    weightTracker = weightTracker
)

fun localTamilStrings(
    doneBy: String = "செய்தது",
    name: String = "அபிலாஷ்",
    weightTracker: String = "எடை கண்காணிப்பான்"
): LanguageStrings = LanguageStrings(
    doneBy = doneBy,
    name = name,
    weightTracker = weightTracker
)

fun localMalayalamStrings(
    doneBy: String = "ചെയ്തത്ത്",
    name: String = "അഭിലാഷ്",
    weightTracker: String = "ഭാര നീരീക്ഷണൻ"
): LanguageStrings = LanguageStrings(
    doneBy = doneBy,
    name = name,
    weightTracker = weightTracker
)


private val textColor
    @Composable get() = if(isSystemInDarkTheme()) Color.White else Color.Black

private val NORMAL_TEXT_SIZE = 12.sp
private val MEDIUM_TEXT_SIZE = 16.sp
private val LARGE_TEXT_SIZE = 24.sp

//region CompositionLocalProviders

val LocalLanguage = staticCompositionLocalOf {
    Language.ENGLISH
}

val LocalTextStyleProvider = staticCompositionLocalOf {
    TextStyle.NORMAL
}

object LocalLanguageStrings {
    val current: LanguageStrings
        @Composable get() {
            return when(LocalLanguage.current) {
                Language.ENGLISH -> localStrings()
                Language.TAMIL -> localTamilStrings()
                Language.MALAYALAM -> localMalayalamStrings()
            }
        }
}

object LocalTextStyle {
    val current: ComposeTextStyle
        @Composable get() {
            return when(LocalTextStyleProvider.current) {
                TextStyle.NORMAL -> {
                    ComposeTextStyle(
                        color = textColor,
                        fontFamily = FontFamily.Serif,
                        fontSize = NORMAL_TEXT_SIZE
                    )
                }
                TextStyle.BOLD_STYLE -> {
                    ComposeTextStyle(
                        color = textColor,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = NORMAL_TEXT_SIZE
                    )
                }
                TextStyle.ITALIC_STYLE -> {
                    ComposeTextStyle(
                        color = textColor,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = NORMAL_TEXT_SIZE
                    )
                }
                TextStyle.MEDIUM_NORMAL -> {
                    ComposeTextStyle(
                        color = textColor,
                        fontFamily = FontFamily.Serif,
                        fontSize = MEDIUM_TEXT_SIZE
                    )
                }
                TextStyle.MEDIUM_BOLD_STYLE -> {
                    ComposeTextStyle(
                        color = textColor,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = MEDIUM_TEXT_SIZE
                    )
                }
                TextStyle.MEDIUM_ITALIC_STYLE -> {
                    ComposeTextStyle(
                        color = textColor,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = MEDIUM_TEXT_SIZE
                    )
                }
                TextStyle.LARGE_NORMAL -> {
                    ComposeTextStyle(
                        color = textColor,
                        fontFamily = FontFamily.Serif,
                        fontSize = LARGE_TEXT_SIZE
                    )
                }
                TextStyle.LARGE_BOLD_STYLE -> {
                    ComposeTextStyle(
                        color = textColor,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = LARGE_TEXT_SIZE
                    )
                }
                TextStyle.LARGE_ITALIC_STYLE -> {
                    ComposeTextStyle(
                        color = textColor,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = LARGE_TEXT_SIZE
                    )
                }
            }
        }
}

//endregion