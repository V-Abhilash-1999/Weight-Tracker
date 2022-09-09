package com.example.weighttracker.ui.util

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.request.ImageRequest
import com.example.weighttracker.repository.database.WTDataValue
import com.example.weighttracker.repository.util.WTDateConverter
import com.abhilash.weighttracker.chart.chart.data.ZDDataValue
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

const val UNSPECIFIED = "UNSPECIFIED"
const val ERROR_STATE = -1
const val YEAR = 2022
val DatapointSize: Size
    @Composable get() = Size(4.dp.dpToPx(), 4.dp.dpToPx())

fun Color.getGradients() : List<Color> = (1..5).map { copy(alpha = it/100f) }

fun List<WTDataValue>.toLineDataValues() = map { dataValue ->
    val date = WTDateConverter.toDate(dataValue.date)
    val dateFormat = SimpleDateFormat.getDateInstance(DateFormat.MONTH_FIELD)
    val dateString = date?.let { dateFormat.format(date) } ?: UNSPECIFIED
    ZDDataValue(
        value = dataValue.weight,
        xLabel = dateString.convertDate(),
        hintLabel = null,
        isAnomaly = dataValue.skipped
    )
}

fun String.convertDate(): String {
    if(this == UNSPECIFIED) {
        return UNSPECIFIED
    }
    val splitValues = split('-')
    return if(splitValues.size < 2) this else splitValues[1] + " " + splitValues.first()
}

fun Context.makeToast(
    message: String,
    length: Int = Toast.LENGTH_SHORT
) {
    Toast.makeText(this, message, length).show()
}

fun String.getDay(): Int {
    return try {
        val day = this.split("-")[1].trim().toInt()

        if(day in 1..31) day else ERROR_STATE

    } catch (ex: Exception) {
        ERROR_STATE
    }
}

fun String.getMonth(): Int {
    return try {
        val day = this.split("-")[0].trim().lowercase().toMonth()

        if(day in 1..31) day else ERROR_STATE

    } catch (ex: Exception) {
        ERROR_STATE
    }
}

private fun String.toMonth(): Int {
    return when(this) {
        "jan" -> 1
        "feb" -> 2
        "mar" -> 3
        "apr" -> 4
        "may" -> 5
        "jun" -> 6
        "jul" -> 7
        "aug" -> 8
        "sep" -> 9
        "oct" -> 10
        "nov" -> 11
        "dec" -> 12
        else -> ERROR_STATE
    }
}

@Composable
internal fun Dp.dpToPx() = with(LocalDensity.current) { toPx() }

fun Modifier.sizeInDp(size: Int) = this.size(size.dp)

fun Context.buildImageFromUri(imageUri: Uri?) = ImageRequest
    .Builder(this)
    .error(WTConstant.DEFAULT_PROFILE_PIC)
    .data(imageUri)
    .listener(
        onError = { request, result ->
            WTConfiguration.checkAndLog("${result.throwable.message}")
        }
    ) { request, result ->
        WTConfiguration.checkAndLog("${result.diskCacheKey}")
    }
    .build()
