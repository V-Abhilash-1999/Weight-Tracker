package com.example.weighttracker.ui.screens

import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import coil.compose.rememberAsyncImagePainter
import com.example.weighttracker.R
import com.example.weighttracker.ui.layout.form.FormLayout
import com.example.weighttracker.ui.util.*
import com.example.weighttracker.viewmodel.WTViewModel
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.HintRequest
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator
import java.util.*


@Destination
@Composable
fun WTSignInScreen(
    navController: DestinationsNavigator,
    onSignIn: (WTSignInOption) -> Unit
) {
    WTBackgroundScreen(
        scrollable = false
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            WTCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                padding = PaddingValues(16.dp)
            ) {
                Column {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        text = "Sign In Using",
                        textAlign = TextAlign.Center,
                        color = mainColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        modifier = Modifier
                            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                            .align(Alignment.CenterHorizontally)
                            .cardShadow(),
                        onClick = {

                        },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, mainColor3),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.White
                        )
                    ) {
                        Text(text = "SIGN IN ANONYMOUSLY")
                    }

                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        text = "OR",
                        textAlign = TextAlign.Center,
                        color = mainColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic
                    )

                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        WTSignInOption.values().forEach { signInOption ->
                            if(signInOption != WTSignInOption.ANONYMOUS) {
                                Button(
                                    modifier = Modifier
                                        .size(64.dp),
                                    onClick = {
                                        onSignIn(signInOption)
                                    },
                                    border = BorderStroke(1.dp, Color.Black),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = Color.White
                                    )
                                ) {
                                    val icon = rememberAsyncImagePainter(signInOption.icon)
                                    Icon(
                                        painter = icon,
                                        contentDescription = signInOption.const,
                                        tint = Color.Unspecified
                                    )
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}

@Destination(
    style = CustomDialog::class,
)
@Composable
fun WTMobileSignIn(
    navController: DestinationsNavigator,
    getPhoneNumber: () -> String,
    viewModel: WTViewModel,
    setPhoneNumber: (String) -> Unit
) {
    val (mobileNoTextState, setMobileNo) = remember { mutableStateOf(getPhoneNumber()) }
    val (expanded, setExpanded) = remember { mutableStateOf(false) }

    val currentCountry = Locale.getDefault().country
    val countryCodeMap = WTConstant.countryCodes.toMutableMap()
    var selectedCountry by remember { mutableStateOf(currentCountry) }
    val selectedCountryCode by remember(selectedCountry) {
        mutableStateOf("+${countryCodeMap[selectedCountry]}")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wavyBackground(
                waveColor = mainColor,
                background = Color.White
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FormLayout(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            FormRow(
                modifier = Modifier
                    .header(
                        text = "Kindly Enter Mobile Number",
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
                    Row(
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .clickable {
                                setExpanded(true)
                            }
                    ) {
                        Text("$selectedCountry - $selectedCountryCode")
                        Icon(
                            modifier = Modifier.padding(start = 8.dp),
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { setExpanded(false) },
                        modifier = Modifier
                            .height(300.dp)
                            .fillMaxWidth(0.5f)
                    ) {
                        countryCodeMap.forEach { countryCodeMapItem ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedCountry = countryCodeMapItem.key
                                    setExpanded(false)
                                }
                            ) {
                                Text(
                                    text = countryCodeMapItem.key,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )
                            }
                        }
                    }
                },
                formItem = {
                    TextField(
                        modifier = Modifier
                            .weight(80f),
                        value = mobileNoTextState,
                        onValueChange = setMobileNo,
                        placeholder = {
                            Text(
                                text = "Enter Mobile No.",
                                color = Color.Gray
                            )
                        },
                        leadingIcon = {
                            Image(
                                modifier = Modifier.size(12.dp),
                                painter = painterResource(id = R.drawable.ic_wt_call),
                                contentDescription = "Call Icon",
                                colorFilter = ColorFilter.tint(mainColor)
                            )
                        },
                        keyboardActions = KeyboardActions(
                            onDone = {
                                defaultKeyboardAction(ImeAction.Done)

                                viewModel.phoneNumber = "$selectedCountryCode$mobileNoTextState"
                                setPhoneNumber(viewModel.phoneNumber)
                                navController.navigateUp()
                            }
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        visualTransformation = VisualTransformation.None,
                        singleLine = true
                    )
                }
            )
        }

        Button(
            modifier = Modifier.padding(32.dp),
            onClick = {
                viewModel.phoneNumber = "$selectedCountryCode$mobileNoTextState"
                setPhoneNumber(viewModel.phoneNumber)
                navController.navigateUp()
            }
        ) {
            Text(text =  "Set Phone Number")
        }
    }
}


fun getCountryFlag(countryCode: String) {
    val flagOffset = 0x1F1E6
    val asciiOffset = 0x41

    val firstChar = Character.codePointAt(countryCode, 0) - asciiOffset + flagOffset
    val secondChar = Character.codePointAt(countryCode, 1) - asciiOffset + flagOffset

    val flag = (String(Character.toChars(firstChar))
            + String(Character.toChars(secondChar)))
}

@Destination(
    style = CustomDialog::class,
)
@Composable
fun WTMobileVerificationCodeScreen(
    navigator: DestinationsNavigator,
    viewModel: WTViewModel
) {
    val (verificationCode, setCode) = remember { viewModel.smsCode }
    val authVerificationCode = viewModel.verificationCode.value
    if(verificationCode.length >= 6 && verificationCode == authVerificationCode) {
        viewModel.setSignedInMethod(WTSignInOption.MOBILE)
        navigator.navigateUp()
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wavyBackground(
                waveColor = mainColor,
                background = Color.White
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FormLayout(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            FormRow(
                modifier = Modifier
                    .header(
                        text = "Kindly Enter Verification Code",
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
                labelItem = {},
                formItem = {
                    TextField(
                        modifier = Modifier
                            .weight(80f),
                        value = verificationCode,
                        onValueChange = setCode,
                        placeholder = {
                            Text(
                                text = "Enter Code",
                                color = Color.Gray
                            )
                        },
                        leadingIcon = {
                            Image(
                                modifier = Modifier.size(12.dp),
                                painter = painterResource(id = R.drawable.ic_wt_call),
                                contentDescription = "Call Icon",
                                colorFilter = ColorFilter.tint(mainColor)
                            )
                        },
                        keyboardActions = KeyboardActions(
                            onDone = {
                                defaultKeyboardAction(ImeAction.Done)
                                if(verificationCode == authVerificationCode) {
                                    viewModel.setSignedInMethod(WTSignInOption.MOBILE)
                                }
                                navigator.navigateUp()
                            }
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        visualTransformation = VisualTransformation.None,
                        singleLine = true
                    )
                }
            )
        }

        Button(
            modifier = Modifier.padding(32.dp),
            onClick = {
                if(verificationCode == authVerificationCode) {
                    viewModel.setSignedInMethod(WTSignInOption.MOBILE)
                }
                navigator.navigateUp()
            }
        ) {
            Text(text =  "Enter Verification Code")
        }
    }
}