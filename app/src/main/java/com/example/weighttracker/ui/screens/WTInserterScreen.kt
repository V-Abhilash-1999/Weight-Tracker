package com.example.weighttracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.weighttracker.R
import com.example.weighttracker.ui.layout.form.FormLayout
import com.example.weighttracker.ui.util.*
import com.example.weighttracker.viewmodel.WTViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DestinationStyle
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.util.*

object CustomDialog: DestinationStyle.Dialog {
    @OptIn(ExperimentalComposeUiApi::class)
    override val properties: DialogProperties = DialogProperties(
        usePlatformDefaultWidth = true
    )

}

@Destination(
    style = CustomDialog::class,
)
@Composable
fun WTWeightInserter(
    navController: DestinationsNavigator,
    viewModel: WTViewModel
) {
    val shape = RoundedCornerShape(16.dp)
    Surface(
        color = Color.White,
        shape = shape,
        border = BorderStroke(1.dp, Color.Black)
    ) {
        val currentMonthName = SimpleDateFormat
            .getDateInstance()
            .format(Date())
            .split("-")
            .getOrNull(1)  ?: ""

        var isCurrentMonthDisplayed by remember { mutableStateOf(false) }

        val (dateTextState, setDateState) = remember { mutableStateOf(TextFieldValue("")) }
        val (weightTextState, setWeightState) = remember { mutableStateOf("") }
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FormLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                //ABHI Put formType in modifier as DATE
                FormRow(
                    modifier = Modifier
                        .header(
                            text = "Kindly Enter Date",
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        .align(Alignment.Start)
                        .padding(top = 16.dp, start = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    labelItem = {
                        /*
                            Text(
                                modifier = Modifier
                                    .height(50.dp)
                                    .weight(20f)
                                    .padding(top = 16.dp, start = 16.dp),
                                text = "Date",
                                style = LocalTextStyle.current,
                            )
                        */
                    },
                    formItem = {
                        //ABHI Disable Text Input Field
                        TextField(
                            modifier = Modifier
                                .weight(80f)
                                .onFocusChanged {
                                    if (it.isFocused && !isCurrentMonthDisplayed) {
                                        isCurrentMonthDisplayed = true
                                        setDateState(
                                            TextFieldValue(
                                                text = currentMonthName,
                                                selection = TextRange(currentMonthName.length)
                                            )
                                        )
                                    }
                                },
                            value = dateTextState,
                            onValueChange = setDateState,
                            placeholder = {
                                Text(
                                    text = "MMM-DD",
                                    color = Color.Gray
                                )
                            },
                            leadingIcon = {
                                Image(
                                    modifier = Modifier.size(32.dp),
                                    painter = painterResource(id = R.drawable.ic_wt_calendar),
                                    contentDescription = "Calender Icon",
                                    colorFilter = ColorFilter.tint(MaterialTheme.colors.background)
                                )
                            },
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    defaultKeyboardAction(ImeAction.Done)
                                }
                            ),
                            visualTransformation = VisualTransformation.None,
                            singleLine = true
                        )
                    }
                )

                FormRow(
                    modifier = Modifier
                        .header(
                            text = "Kindly Enter Weight",
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        .align(Alignment.Start)
                        .padding(top = 16.dp, start = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    labelItem = {
                        /*
                            Text(
                                modifier = Modifier
                                    .height(50.dp)
                                    .weight(20f)
                                    .padding(top = 16.dp, start = 16.dp),
                                text = "Weight",
                                style = LocalTextStyle.current,
                            )
                        */
                    },
                    formItem = {
                        TextField(
                            modifier = Modifier
                                .weight(80f),
                            value = weightTextState,
                            onValueChange = setWeightState,
                            placeholder = {
                                Text(
                                    text = "In Kgs",
                                    color = Color.Gray
                                )
                            },
                            leadingIcon = {
                                Image(
                                    modifier = Modifier.size(32.dp),
                                    painter = painterResource(id = R.drawable.ic_wt_weight_icon),
                                    contentDescription = "Weight Icon",
                                    colorFilter = ColorFilter.tint(MaterialTheme.colors.background)
                                )
                            },
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    defaultKeyboardAction(ImeAction.Done)
                                }
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            singleLine = true
                        )
                    }
                )
            }
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            Button(
                modifier = Modifier.padding(32.dp),
                onClick = {
                    val day = dateTextState.text.getDay()
                    val month = dateTextState.text.getMonth()
                    val weight = try {
                        weightTextState.toFloat()
                    } catch (ex:Exception) {
                        ERROR_STATE.toFloat()
                    }
                    if(day == ERROR_STATE || month == ERROR_STATE || weight == ERROR_STATE.toFloat()) {
                        context.makeToast("Some Error Occurred")
                    } else {
                        scope.launch {
                            viewModel.insertData(
                                day = day,
                                month = month,
                                year = YEAR,
                                skipped = false,
                                weight = weight
                            )
                            navController.navigateUp()
                        }
                    }
                }
            ) {
                Text(text =  "Insert Data")
            }
        }
    }
}
