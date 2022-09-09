@file:OptIn(ExperimentalPermissionsApi::class)
package com.example.weighttracker.ui.screens

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.example.weighttracker.ui.layout.wtlayout.*
import com.example.weighttracker.ui.util.*
import com.example.weighttracker.viewmodel.WTViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.ramcosta.composedestinations.annotation.Destination


@Destination
@Composable
fun WTProfileScreen(
    viewModel: WTViewModel,
    activity: Activity
) {
    val focusManager = LocalFocusManager.current
    WTBackgroundScreen(
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures {
                focusManager.clearFocus(true)
            }
        }
    ) {
        AboutYouCard(activity, viewModel)
        EmailCard(viewModel)
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        contentPadding = PaddingValues(16.dp)
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

            NameTextField(viewModel)
        }
    }
}

@Composable
fun NameTextField(viewModel: WTViewModel) {
    val (name, setName) = remember { mutableStateOf(viewModel.user?.displayName ?: "") }
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged {
                if (!it.isFocused) {
                    if (name.isNotEmpty()) {
                        viewModel.updateUserName(name)
                    }
                }
            },
        value = name,
        onValueChange = setName,
        isError = name.isEmpty(),
        placeholder = {
            Text(text = "Enter Your Name Here")
        },
        keyboardActions = KeyboardActions(
            onDone = {
                viewModel.updateUserName(name)
            }
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        colors = WTProfileInputColors()
    )
}


@Composable
fun EmailCard(viewModel: WTViewModel) {
    WTCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.sizeInDp(12),
                imageVector = Icons.Default.Email,
                contentDescription = "Email"
            )

            EmailTextField(viewModel = viewModel)
        }
    }
}

@Composable
fun EmailTextField(viewModel: WTViewModel) {
    val (email, setEmail) = remember { mutableStateOf(viewModel.user?.email ?: "") }
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged {
                if (!it.isFocused) {
                    if (email.isNotEmpty()) {
                        viewModel.updateEmailId(email)
                    }
                }
            },
        value = email,
        onValueChange = setEmail,
        isError = email.isEmpty(),
        placeholder = {
            Text(text = "Enter Your Email Here")
        },
        keyboardActions = KeyboardActions(
            onDone = {
                viewModel.updateUserName(email)
            }
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        colors = WTProfileInputColors()
    )
}

@Composable
fun rememberAsyncProfilePic(
    profilePic: MutableState<Any>,
    showLoader: MutableState<Boolean>
) = rememberAsyncImagePainter(
    model = profilePic.value,
    onState = { painterState ->
        painterState.hideLoaderIf(profilePic.isNotDefaultProfilePic()) {
            showLoader.value = false
        }
    }
)


@Composable
fun LoadImageFromCloud(
    profilePic: MutableState<Any>,
    showLoader: MutableState<Boolean>,
    viewModel: WTViewModel,
    context: Context
) {
    LaunchedEffect(key1 = Unit) {
        val imageUri = viewModel.getImageFromCloud()
        if(imageUri != null) {
            profilePic.value = context.buildImageFromUri(imageUri)
        } else {
            showLoader.value = false
        }
    }
}

fun MutableState<Any>.isDefaultProfilePic() = value == WTConstant.DEFAULT_PROFILE_PIC

fun MutableState<Any>.isNotDefaultProfilePic() = value != WTConstant.DEFAULT_PROFILE_PIC

fun AsyncImagePainter.State.hideLoaderIf(condition: Boolean, block: () -> Unit) {
    if(painter != null && condition) {
        block()
    }
}

fun Uri.imagePickerResult(
    profilePic: MutableState<Any>,
    viewModel: WTViewModel,
    context: Context
) {
    profilePic.value = context.buildImageFromUri(this)
    viewModel.updateUserImage(this)
}

fun checkAndOpenImagePicker(
    permissionState: PermissionState,
    onPermissionEnabled: () -> Unit
) {
    if (permissionState.hasPermission) {
        onPermissionEnabled()
    } else {
        permissionState.launchPermissionRequest()
    }
}

fun PermissionState.setOnPermissionRequested(
    imagePickerLauncher: ManagedActivityResultLauncher<String, Uri?>,
    activity: Activity
) {
    if(permissionRequested) {
        if(hasPermission) {
            imagePickerLauncher.launch(WTConstant.IMAGE_TYPE_INTENT)
        } else {
            activity.makeToast("Kindly provide permission")
        }
    }
}