@file:OptIn(ExperimentalPermissionsApi::class)
package com.example.weighttracker.ui.screens

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.weighttracker.destinations.WTMobileVerificationCodeScreenDestination
import com.example.weighttracker.getPhoneAuthCallback
import com.example.weighttracker.ui.layout.wtlayout.*
import com.example.weighttracker.ui.screens.util.*
import com.example.weighttracker.ui.util.*
import com.example.weighttracker.viewmodel.WTViewModel
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.SizeMode
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Destination
@Composable
fun WTProfileScreen(
    navController: DestinationsNavigator,
    viewModel: WTViewModel,
    activity: Activity
) {
    val focusManager = LocalFocusManager.current
    LaunchedEffect(key1 = Unit) {
        showCard(viewModel)
    }
    WTBackgroundScreen(
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures {
                focusManager.clearFocus(true)
            }
        }
    ) {
        AboutYouCard(activity, viewModel)

        UserInfoSection(activity, navController, viewModel)
    }
}

suspend fun showCard(viewModel: WTViewModel) {
    WTUserInfo.PHONE.state.value = viewModel.isUserEmailAvailable
    WTUserInfo.EMAIL.state.value = viewModel.isUserEmailAvailable
    viewModel.getUserNotes {
        WTUserInfo.NOTES.state.value = it.isNotEmpty()
    }
}


@Composable
fun AboutYouCard(activity: Activity, viewModel: WTViewModel) {
    val profilePic = remember {
        mutableStateOf<Any>(WTConstant.DEFAULT_PROFILE_PIC)
    }
    val showLoader = remember { mutableStateOf(true) }

    LoadImageFromCloud(profilePic, showLoader, viewModel, activity)

    val imagePickerLauncher = WTActivityState.rememberImagePickerLauncherForResult { imageUri ->
        imageUri.imagePickerResult(profilePic, viewModel, activity)
    }
    val storagePermissionState = WTActivityState.rememberExternalStoragePermissionState().also {
        it.setOnPermissionRequested(imagePickerLauncher, activity)
    }

    WTCard(
        modifier = Modifier.profileModifier(),
        contentPadding = WTConstant.profilePaddingValues
    ) {
        Row {
            CircularImageWithLoader(
                modifier = Modifier
                    .clickable {
                        checkAndOpenImagePicker(storagePermissionState) {
                            imagePickerLauncher.launch(WTConstant.IMAGE_TYPE_INTENT)
                        }
                    },
                size = 64,
                painter = rememberAsyncProfilePic(profilePic, showLoader),
                contentDescription = "Profile Pic",
                showLoader = showLoader.value
            )

            ProfileTextField(
                text = viewModel.user?.displayName ?: "",
                placeHolderText = "Provide Name Here",
                keyboardType = KeyboardType.Text
            ) { name ->
                viewModel.updateUserName(name)
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun UserInfoSection(
    activity: Activity,
    navController: DestinationsNavigator,
    viewModel: WTViewModel
) {
    WTUserInfo.values().forEach { wtUserInfo ->
        val userState by remember { wtUserInfo.state }

        AnimatedContent(
            targetState = userState,
            transitionSpec = {
                val enterAnim = slideInHorizontally { -it } +  fadeIn(animationSpec = tween(200, delayMillis = 100))
                val exitAnim = slideOutHorizontally { it } + fadeOut(animationSpec = tween(200, delayMillis = 100))

                (enterAnim with exitAnim)
                    .using(
                        SizeTransform(
                            clip = false,
                            sizeAnimationSpec = { width, height ->
                                spring(
                                    stiffness = Spring.StiffnessMediumLow,
                                    visibilityThreshold = IntSize.VisibilityThreshold
                                )
                            }
                        )
                    )
            }
        ) {showInfo ->
            if(showInfo) {
                when(wtUserInfo) {
                    WTUserInfo.EMAIL -> {
                        if(viewModel.signInMode == WTSignInOption.GOOGLE) {
                            EmailCard(viewModel)
                        }
                    }
                    WTUserInfo.PHONE -> {
                        if(viewModel.signInMode == WTSignInOption.MOBILE) {
                            PhoneCard(activity, navController, viewModel)
                        }
                    }
                    WTUserInfo.NOTES -> {
                        NotesCard(viewModel = viewModel)
                    }
                }
            }
        }
    }

    UserInfoAdditionCard(viewModel = viewModel)
}


@Composable
fun EmailCard(viewModel: WTViewModel) {
    ProfileCard(
        icon = Icons.Filled.Email,
        placeHolderText = "Provide Email Here",
        keyboardType = KeyboardType.Email,
        text = viewModel.email,
    ) { emailId ->
    }
}

@Composable
fun PhoneCard(
    activity: Activity,
    navController: DestinationsNavigator,
    viewModel: WTViewModel
) {
    val phoneAuthCallback = LocalContext.current.getPhoneAuthCallback(
        onCodeSent = {  verificationId->
            viewModel.verificationId = verificationId
            navController.navigate(WTMobileVerificationCodeScreenDestination)
        }
    ) { credential ->
        credential.smsCode?.let { smsCode ->
            viewModel.verificationCode.value = smsCode
            viewModel.smsCode.value = smsCode
        }
        viewModel.user?.updatePhoneNumber(credential)
    }
    ProfileCard(
        icon = Icons.Filled.Phone,
        placeHolderText = "Provide Phone No Here",
        text = viewModel.phoneNumber,
        isEditable = viewModel.signInMode != WTSignInOption.MOBILE,
        onTextUpdate = { phoneNumber ->
            viewModel.updatePhoneNumber(phoneNumber, phoneAuthCallback, activity)
        }
    )
}
@Composable
fun NotesCard(viewModel: WTViewModel) {
    var showLoader by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }
    LaunchedEffect(key1 = Unit) {
        viewModel.getUserNotes { apiNotes ->
            notes = apiNotes
            showLoader = true
        }
    }
    ProfileCard(
        icon = Icons.Filled.DateRange,
        placeHolderText = "Provide Notes Here",
        text = notes,
        isEditable = !showLoader,
        showLoader = showLoader,
        onTextUpdate = { updatedNotes ->
            if(updatedNotes != notes) {
                CoroutineScope(Dispatchers.IO).launch {
                    showLoader = true
                    viewModel.setUserNotes(updatedNotes) {
                        showLoader = false
                    }
                }
            }
        }
    )
}

@Composable
fun ProfileCard(
    icon: ImageVector,
    placeHolderText: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    text: String,
    showLoader: Boolean = false,
    isEditable: Boolean = true,
    onTextUpdate: (String) -> Unit
) {
    WTCard(
        modifier = Modifier
            .profileModifier(),
        contentPadding = WTConstant.profilePaddingValues
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                modifier = Modifier
                    .sizeInDp(32),
                imageVector = icon,
                contentDescription = "Profile Icon"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                ProfileTextField(
                    text = text,
                    keyboardType = keyboardType,
                    isEditable = isEditable,
                    showLoader = showLoader,
                    placeHolderText = placeHolderText,
                    onTextUpdate = onTextUpdate
                )
            }

        }
    }
}

@Composable
fun RowScope.ProfileTextField(
    text: String,
    placeHolderText: String,
    showLoader: Boolean = false,
    isEditable: Boolean = true,
    keyboardType: KeyboardType,
    onTextUpdate: (String) -> Unit
) {
    val (inputText, setText) = remember(text) { mutableStateOf(text) }
    var hasFocus by remember { mutableStateOf(false) }

    TextField(
        modifier = Modifier
            .fillMaxWidth(0.80f)
            .weight(95f, true)
            .onFocusChanged {
                if (!it.isFocused && hasFocus) {
                    if (inputText.isNotEmpty()) {
                        onTextUpdate(inputText)
                    }
                }
                if (it.isFocused) {
                    hasFocus = true
                }
            },
        value = inputText,
        onValueChange = setText,
        isError = inputText.isEmpty(),
        placeholder = {
            Text(text = placeHolderText)
        },
        keyboardActions = KeyboardActions(
            onDone = {
                onTextUpdate(inputText)
            }
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Done
        ),
        enabled = isEditable,
        singleLine = true,
        colors = WTProfileInputColors()
    )

    WTCircularProgressBar(
        modifier = Modifier
            .weight(5f, true),
        showLoader = showLoader
    )
}

@Composable
fun UserInfoAdditionCard(viewModel: WTViewModel) {
    Crossfade(
        targetState = !viewModel.isUserEmailAvailable || !viewModel.isUserPhoneNoAvailable
    ) { showCard ->
        if(showCard) {
            WTCard(
                modifier = Modifier.profileModifier()
            ) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    mainAxisSize = SizeMode.Expand,
                    mainAxisAlignment = FlowMainAxisAlignment.SpaceEvenly
                ) {
                    for(userInfo in WTUserInfo.values()) {
                        if(
                            (userInfo != WTUserInfo.EMAIL || viewModel.isUserEmailAvailable) &&
                            (userInfo != WTUserInfo.PHONE || viewModel.isUserPhoneNoAvailable)
                        ) {
                            Icon(
                                modifier = Modifier
                                    .sizeInDp(64)
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = colorList
                                        )
                                    )
                                    .padding(16.dp)
                                    .clickable(
                                        role = Role.Image
                                    ) {
                                        userInfo.state.flipValue()
                                    },
                                imageVector = userInfo.icon,
                                contentDescription = userInfo.desc
                            )
                        }
                    }
                }
            }
        }
    }
}