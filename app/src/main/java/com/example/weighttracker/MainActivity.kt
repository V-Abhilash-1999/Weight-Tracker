package com.example.weighttracker

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.weighttracker.destinations.*
import com.example.weighttracker.ui.navigation.WTNavGraph
import com.example.weighttracker.ui.screens.CustomDialog
import com.example.weighttracker.ui.screens.WTScreen
import com.example.weighttracker.ui.theme.WeightTrackerTheme
import com.example.weighttracker.ui.util.MakeToast
import com.example.weighttracker.ui.util.WTSignInOption
import com.example.weighttracker.ui.util.makeToast
import com.example.weighttracker.viewmodel.WTViewModel
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultRecipient
import com.ramcosta.composedestinations.spec.NavHostEngine
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import com.ramcosta.composedestinations.scope.*

typealias callback = ((result: ActivityCallback) -> ActivityResultLauncher<IntentSenderRequest>)
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    @Inject
    lateinit var viewModel: WTViewModel
    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerForActivityResult(
            FirebaseAuthUIActivityResultContract()
        ) { res ->
            Log.e(">>>>","res: $res")
        }

        var callback: ActivityCallback? = null
        val launcher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            callback?.onResult(it)
        }

        setContent {
            WeightTrackerTheme {
                viewModel.checkSignIn()
                RenderScreen(viewModel, auth) { callbackResult ->
                    callback = callbackResult
                    launcher
                }
            }
        }
    }
}

interface ActivityCallback {
    fun onResult(result: ActivityResult)
}

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterialNavigationApi::class
)
@Composable
fun Activity.RenderScreen(
    viewModel: WTViewModel,
    auth: FirebaseAuth,
    activityCallback: callback
) {
    val navHostEngine = rememberAnimatedNavHostEngine()
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navController = navHostEngine.rememberNavController(bottomSheetNavigator)

    val isSignedIn = viewModel.isSignedIn
    val mainNavController = rememberNavController()

    DestinationsNavHost(
        navGraph = WTNavGraph.initialScreens,
        startRoute = WTLoggedOutScreenDestination,
        navController = mainNavController,
        dependenciesContainerBuilder = {
            dependency(viewModel)
            dependency(navController)
            dependency(navHostEngine)
            dependency(bottomSheetNavigator)
            dependency(auth)
            dependency(activityCallback)
            dependency(this@RenderScreen)
        }
    )

    if(isSignedIn.value && mainNavController.currentDestination?.route != WTLoggedInScreenDestination.route) {
        mainNavController.navigate(WTLoggedInScreenDestination.route) {
            popUpTo(WTLoggedOutScreenDestination.route) {
                inclusive = true
            }
        }
    }
}

@OptIn(ExperimentalMaterialNavigationApi::class)
@Destination
@Composable
fun WTLoggedInScreen(
    bottomSheetNavigator: BottomSheetNavigator,
    navHostEngine: NavHostEngine,
    viewModel: WTViewModel,
    activity: Activity
) {
    ModalBottomSheetLayout(
        bottomSheetNavigator = bottomSheetNavigator,
        sheetBackgroundColor = Color.Transparent
    ) {
        WTScreen(
            navHostEngine = navHostEngine,
            navController = navHostEngine.rememberNavController(bottomSheetNavigator),
            viewModel = viewModel,
            activity = activity
        )
    }
}


@Destination
@Composable
fun WTLoggedOutScreen(
    activity: Activity,
    viewModel: WTViewModel,
    activityCallback: callback,
    navHostEngine: NavHostEngine,
    auth: FirebaseAuth
) {
    var mobileNumber = ""
    val navController = navHostEngine.rememberNavController()
    val callback = activity.getPhoneAuthCallback(navController, viewModel, auth)
    val activityResultCallback = object : ActivityCallback {
        override fun onResult(result: ActivityResult) {
            mobileNumber = result.getPhoneNumber()
            navController.navigate(WTMobileSignInDestination.route)
        }
    }
    val launcher = activityCallback(activityResultCallback)

    var currentSignInOption: WTSignInOption? = null

    val setPhoneNumber: (String) -> Unit = { phoneNumber: String ->
        try {
            val options = PhoneAuthOptions
                .newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callback)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch(ex:Exception) {
            activity.baseContext.makeToast("Phone No Exception thrown")
        }
    }

    val setSignInOption: (WTSignInOption) -> Unit = { signInOption: WTSignInOption ->
        currentSignInOption = signInOption
        viewModel.signInMode = signInOption
        activity.signInUsing(signInOption, launcher)
    }

    DestinationsNavHost(
        navGraph = WTNavGraph.logInScreen,
        engine = navHostEngine,
        navController = navController,
        dependenciesContainerBuilder = {
            dependency(viewModel)
            dependency {
                mobileNumber
            }
            currentSignInOption?.let {
                dependency(it)
            }
            when(currentSignInOption) {
                null -> {
                    dependency(setSignInOption)
                }
                WTSignInOption.ANONYMOUS -> {

                }
                WTSignInOption.GOOGLE -> {

                }
                WTSignInOption.FACEBOOK -> {

                }
                WTSignInOption.MOBILE -> {
                    dependency(setPhoneNumber)
                }
            }
        }
    )
}

@Destination(
    style = CustomDialog::class
)
@Composable
fun WTResultScreen(
    viewModel: WTViewModel,
    navigator: DestinationsNavigator,
    getResult: () -> Boolean
) {
    if(getResult()) {
        when(viewModel.signInMode) {
            WTSignInOption.ANONYMOUS -> {

            }
            WTSignInOption.GOOGLE -> {

            }
            WTSignInOption.FACEBOOK -> {

            }
            WTSignInOption.MOBILE -> {
                navigator.navigate(WTMobileSignInDestination)
            }
            null -> {

            }
        }
    }
}

fun ActivityResult.getPhoneNumber(): String {
    val result = this
    val data = result.data
    return if (data != null) {
        val credential: Credential? = data.getParcelableExtra(Credential.EXTRA_KEY)
        credential?.id?.removeCountryCode() ?: ""
    } else {
        ""
    }
}

fun String.removeCountryCode(): String = if(this.startsWith("+")) {
    removePrefix("+91").apply {
        removePrefix("+1")
    }
} else {
    ""
    }

private fun Activity.signInUsing(
    signInMethod: WTSignInOption,
    launcher: ActivityResultLauncher<IntentSenderRequest>
) {
    when(signInMethod) {
        WTSignInOption.ANONYMOUS -> {

        }
        WTSignInOption.GOOGLE -> {
            /*val hintRequest = HintRequest.Builder()
                .setHintPickerConfig(
                    CredentialPickerConfig.Builder()
                        .setShowCancelButton(true)
                        .build()
                )
                .setEmailAddressIdentifierSupported(true)
                .setAccountTypes(IdentityProviders.GOOGLE)
                .build()

            val intent: PendingIntent = mCredentialsClient.getHintPickerIntent(hintRequest)
            try {
                ActivityCompat.startIntentSenderForResult(
                    intent.getIntentSender(),
                    RC_HINT,
                    null,
                    0,
                    0,
                    0
                )
            } catch (e: IntentSender.SendIntentException) {
                baseContext.makeToast("Could not start hint picker Intent")
            }*/
        }
        WTSignInOption.FACEBOOK -> {

        }
        WTSignInOption.MOBILE -> {
            requestHint(launcher)
        }
    }
}

private fun Context.requestHint(launcher: ActivityResultLauncher<IntentSenderRequest>) {
    val hintRequest = HintRequest.Builder()
        .setPhoneNumberIdentifierSupported(true)
        .build()

    val intent = Credentials.getClient(this).getHintPickerIntent(hintRequest)
    val intentSenderRequest = IntentSenderRequest.Builder(intent.intentSender)

    launcher.launch(intentSenderRequest.build())
}

fun Activity.getPhoneAuthCallback(
    navController: NavController,
    viewModel: WTViewModel,
    auth: FirebaseAuth
) =
    object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
        credential.smsCode?.let {
            viewModel.verificationCode.value = it
            viewModel.smsCode.value = it
        }
    }

    override fun onVerificationFailed(e: FirebaseException) {
        makeToast("Unable to verify code")
    }

    override fun onCodeSent(
        verificationId: String,
        token: PhoneAuthProvider.ForceResendingToken
    ) {
        viewModel.verificationId = verificationId
        navController.navigate(WTMobileVerificationCodeScreenDestination.route)
        makeToast("Code has been sent")
    }
}