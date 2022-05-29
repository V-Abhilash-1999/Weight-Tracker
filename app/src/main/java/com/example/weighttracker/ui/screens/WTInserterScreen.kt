package com.example.weighttracker.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weighttracker.R
import com.example.weighttracker.ui.layout.FormLayout
import com.example.weighttracker.ui.util.*
import com.example.weighttracker.viewmodel.WTViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.spec.DestinationStyle
import kotlinx.coroutines.launch
import java.lang.Exception

@Destination(
    style = DestinationStyle.BottomSheet::class,
)
@Composable
fun WTWeightInserter(viewModel: WTViewModel) {
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = Modifier
            .height(500.dp)
            .clip(shape)
            .fillMaxWidth(),
        color = Color.White,
        elevation = 8.dp,
        shape = shape
    ) {
        val (dateTextState, setDateState) = remember { mutableStateOf("") }
        val (weightTextState, setWeightState) = remember { mutableStateOf("") }
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            FormLayout(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
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
                        Text(
                            modifier = Modifier
                                .height(50.dp)
                                .weight(20f)
                                .padding(top = 16.dp, start = 16.dp),
                            text = "Date",
                            style = LocalTextStyle.current,
                        )
                    },
                    formItem = {
                        TextField(
                            modifier = Modifier
                                .weight(80f)
                                .padding(top = 16.dp, start = 16.dp),
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
                        Text(
                            modifier = Modifier
                                .height(50.dp)
                                .weight(20f)
                                .padding(top = 16.dp, start = 16.dp),
                            text = "Weight",
                            style = LocalTextStyle.current,
                        )
                    },
                    formItem = {
                        TextField(
                            modifier = Modifier
                                .weight(80f)
                                .padding(top = 16.dp, start = 16.dp),
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
                                    painter = painterResource(id = R.drawable.ic_wt_calendar),
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
                onClick = {
                    val day = dateTextState.getDay()
                    val month = dateTextState.getMonth()
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
                        }
                    }
                }
            ) {
                Text(text =  "Insert Data")
            }
        }
    }
}
