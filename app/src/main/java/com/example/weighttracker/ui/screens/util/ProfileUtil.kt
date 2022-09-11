@file:OptIn(ExperimentalPermissionsApi::class)
package com.example.weighttracker.ui.screens.util

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.weighttracker.ui.util.WTConfiguration
import com.example.weighttracker.ui.util.WTConstant
import com.example.weighttracker.ui.util.WTUserInfo
import com.example.weighttracker.ui.util.makeToast
import com.example.weighttracker.viewmodel.WTViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState


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


@Composable
fun LoadImageFromCloud(
    profilePic: MutableState<Any>,
    showLoader: MutableState<Boolean>,
    viewModel: WTViewModel,
    context: Context
) {
    LaunchedEffect(key1 = Unit) {
        val imageUri = viewModel.getUserImageFromCloud()
        if(imageUri != null) {
            profilePic.value = context.buildImageFromUri(imageUri)
        } else {
            showLoader.value = false
        }
    }
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

fun Modifier.profileModifier() = fillMaxWidth().padding(vertical = 16.dp)