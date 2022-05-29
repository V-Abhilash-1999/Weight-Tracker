package com.example.weighttracker.ui.util

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.weighttracker.repository.database.WTDataValue
import com.example.weighttracker.repository.util.WTDateConverter
import com.abhilash.weighttracker.chart.chart.data.ZDDataValue
import java.text.DateFormat
import java.text.SimpleDateFormat

const val UNSPECIFIED = "UNSPECIFIED"

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