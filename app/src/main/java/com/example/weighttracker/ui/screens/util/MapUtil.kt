package com.example.weighttracker.ui.screens.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarData
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.example.weighttracker.ui.util.WTConfiguration
import com.example.weighttracker.ui.util.WTSnackBar
import com.example.weighttracker.ui.util.isPermanentlyDisabled
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationUpdater(
    modifier: Modifier = Modifier,
    onLocation: (Location) -> Unit
) {
    val context = LocalContext.current
    val permission = Manifest.permission.ACCESS_FINE_LOCATION
    val permissionState = rememberPermissionState(permission = permission)
    LaunchedEffect(key1 = permissionState.hasPermission) {
        permissionState.launchPermissionRequest()
        if(permissionState.hasPermission) {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                    ?: return@LaunchedEffect
            try {
                locationManager
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?.let { lastLocation ->
                        onLocation(lastLocation)
                    }
            } catch (ex: SecurityException) {
                WTConfiguration.checkAndLog(ex.message)
            } catch (ex: Exception) {
                WTConfiguration.checkAndLog(ex.message)
            }
        }
    }

    when {
        permissionState.permissionRequested && permissionState.hasPermission -> {
            WTSnackBar(modifier = modifier, text = "Permission Enabled")
        }
        permissionState.permissionRequested && permissionState.shouldShowRationale -> {
            WTSnackBar(modifier = modifier, text = "Permission Disabled. Kindly provide permission to use this service.")
        }
        permissionState.isPermanentlyDisabled() -> {
            WTSnackBar(modifier = modifier, text = "Permission Disabled Permanently. Kindly provide permission in app settings to use this service.")
        }
    }
}