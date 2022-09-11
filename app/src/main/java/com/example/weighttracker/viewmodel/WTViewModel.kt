package com.example.weighttracker.viewmodel

import android.app.Activity
import android.content.SharedPreferences
import android.net.Uri
import androidx.annotation.IntRange
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.weighttracker.repository.WTRoomRepository
import com.example.weighttracker.repository.database.WTDataValue
import com.example.weighttracker.repository.util.WTDateConverter
import com.example.weighttracker.ui.util.WTConfiguration
import com.example.weighttracker.ui.util.WTConstant
import com.example.weighttracker.ui.util.WTSignInOption
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.tasks.await
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
class WTViewModel @Inject constructor(
    private val repository: WTRoomRepository,
    private val firebaseDB: FirebaseDatabase,
    private val sharedPref: SharedPreferences,
    private val auth: FirebaseAuth
): ViewModel() {
    var signInMode: WTSignInOption? = null
    var isSignedIn = mutableStateOf(false)
    var smsCode = mutableStateOf("")

    var verificationCode = mutableStateOf("")
    var verificationId = ""
    var user: FirebaseUser? = null
        private set

    val storageRef = FirebaseStorage.getInstance()

    val phoneNumber: String
        get() = user?.phoneNumber ?: ""
    val email: String
        get() = user?.email ?: ""

    /***
     * day indicates the day of the month
     * month indicates the month in date,
     *  month will be accessed as indexed
     *  so while setting value for it use a value lesser than it
     * ***/
    suspend fun insertData(
        @IntRange(from = 1, to = 31)
        day: Int,
        @IntRange(from = 1, to = 12)
        month: Int,
        year: Int = 2022,
        skipped: Boolean = false,
        weight: Float
    ) {
        val calendar = Calendar.getInstance().apply {
            set(year, month - 1, day)
        }

        WTDateConverter.fromDate(calendar.time)?.let { date ->
            val dataValue = WTDataValue(
                date = date,
                weight = weight,
                skipped = skipped
            )
            repository.insert(dataValue)

            coroutineScope {
                launch(Dispatchers.IO) {
                    firebaseDB.reference.setValue(dataValue)
                }
            }
        }
    }

    suspend fun getUserNotes(onFetched: (String) -> Unit) {
        try {
            val byteArray = storageRef
                .reference
                .child(WTConstant.NOTES_PATH + (user?.uid ?: ""))
                .getBytes(10000)
                .asDeferred()
                .await()
            onFetched(String(byteArray))
        } catch (ex: Exception) {
            onFetched("")
        }
    }

    suspend fun setUserNotes(notes: String, onSet: () -> Unit) {
        storageRef
            .reference
            .child(WTConstant.NOTES_PATH + (user?.uid ?: ""))
            .putBytes(notes.toByteArray())
            .asDeferred()
            .await()
        onSet()
    }

    fun getData(): LiveData<List<WTDataValue>> = repository.getWeight()

    fun checkSignIn() {
        val signInMode = sharedPref.getString(WTConstant.SIGN_IN, "") ?: ""
        if(signInMode != "") {
            this.signInMode = WTSignInOption.values().find { it.const == signInMode }
        }
        isSignedIn.value = signInMode != ""
        user = auth.currentUser
    }

    var credential: AuthCredential? = null

    fun setSignedInMethod(signInMethod: WTSignInOption) {
        isSignedIn.value = true
        sharedPref.edit {
            putString(WTConstant.SIGN_IN, signInMethod.const)
            signInMode = signInMethod
        }
        credential = PhoneAuthProvider.getCredential(verificationId, smsCode.value).also { credential ->
            auth.signInWithCredential(credential).addOnSuccessListener {
                user = it.user
            }
        }
    }

    suspend fun getUserImageFromCloud(): Uri? {
        user?.let { user ->
            try {
                return storageRef
                    .reference
                    .child(WTConstant.PROFILE_PIC_PATH + user.uid)
                    .downloadUrl
                    .await()
            } catch (ex: Exception) {
                WTConfiguration.checkAndLog(ex.message)
            }
        }
        return null
    }

    fun updateUserName(name: String) {
        val request = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        user?.updateProfile(request)
    }

    suspend fun updateEmailId(email: String) {
        val credential = EmailAuthProvider.getCredential(email, "password1234")
        user
            ?.reauthenticate(credential)
            ?.addOnSuccessListener {
                user
                    ?.updateEmail(email)
                    ?.addOnSuccessListener {
                        WTConfiguration.checkAndLog("Email updated Successfully")
                    }
                    ?.addOnFailureListener {
                        WTConfiguration.checkAndLog("Email Update Failed: ${it.message}")
                    }
            }
            ?.addOnFailureListener { e ->

            }
    }

    fun updatePhoneNumber(
        number: String,
        callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks,
        activity: Activity
    ) {
        val options = PhoneAuthOptions
            .newBuilder(auth)
            .setPhoneNumber(number)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callback)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)

    }

    fun updateUserImage(image: Uri) {
        val request = UserProfileChangeRequest.Builder()
            .setPhotoUri(image)
            .build()
        user?.updateProfile(request)
        user?.let { user ->
            storageRef
                .reference
                .child(WTConstant.PROFILE_PIC_PATH + user.uid)
                .putFile(image)
        }
    }

    val isUserEmailAvailable: Boolean
        get() = user?.email != null && user?.email?.isNotEmpty() == true

    val isUserPhoneNoAvailable: Boolean
        get() = user?.phoneNumber != null && user?.phoneNumber?.isNotEmpty() == true

}

