package com.example.weighttracker.ui.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState

object WTActivityState {
    @Composable
    fun rememberImagePickerLauncherForResult(
        onResult: (Uri) -> Unit
    ) = rememberLauncherForActivityResult(
        contract = WTGetDocumentContent()
    ) {
        if(it != null) {
            onResult(it)
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun rememberExternalStoragePermissionState() = rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE)


    class WTGetDocumentContent: ActivityResultContract<String, Uri?>() {
        override fun createIntent(context: Context, input: String): Intent {
            return Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType(input)
        }

        override fun getSynchronousResult(
            context: Context,
            input: String
        ): SynchronousResult<Uri?>? = null

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return intent.takeIf { resultCode == Activity.RESULT_OK }?.data
        }
    }
}