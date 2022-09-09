package com.example.weighttracker.viewmodel

import android.content.SharedPreferences
import android.graphics.drawable.Icon
import android.net.Uri
import androidx.annotation.IntRange
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import androidx.core.graphics.drawable.toIcon
import androidx.core.net.toFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.weighttracker.repository.WTRoomRepository
import com.example.weighttracker.repository.database.WTDataValue
import com.example.weighttracker.repository.util.WTDateConverter
import com.example.weighttracker.ui.util.WTConfiguration
import com.example.weighttracker.ui.util.WTConstant
import com.example.weighttracker.ui.util.WTSignInOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
class WTViewModel @Inject constructor(
    private val repository: WTRoomRepository,
    private val firebaseDB: FirebaseDatabase,
    private val sharedPref: SharedPreferences,
    private val auth: FirebaseAuth
): ViewModel() {
    var signInMode: WTSignInOption? = null
    var phoneNumber: String = ""
    var isSignedIn = mutableStateOf(false)

    var smsCode = mutableStateOf("")
    var verificationCode = mutableStateOf("")
    var verificationId = ""

    var user: FirebaseUser? = null
        private set

    val storageRef = FirebaseStorage.getInstance()

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

    fun getData(): LiveData<List<WTDataValue>> = repository.getWeight()

    fun checkSignIn() {
        val signInMode = sharedPref.getString(WTConstant.SIGN_IN, "") ?: ""
        if(signInMode != "") {
            this.signInMode = WTSignInOption.values().find { it.const == signInMode }
        }
        isSignedIn.value = signInMode != ""
        user = auth.currentUser
    }

    fun setSignedInMethod(signInMethod: WTSignInOption) {
        isSignedIn.value = true
        sharedPref.edit {
            putString(WTConstant.SIGN_IN, signInMethod.const)
            signInMode = signInMethod
        }
        val credential = PhoneAuthProvider.getCredential(verificationId, verificationCode.value)
        auth.signInWithCredential(credential).addOnSuccessListener {
            user = it.user
        }
    }

    suspend fun getImageFromCloud(): Uri? {
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

    fun updateEmailId(email: String) {
        user?.updateEmail(email)
    }

    fun updatePhoneNumber(number: String) {
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

    fun isUserEmailAvailable() = user?.email != null

    fun isUserPhoneNoAvailable() = user?.phoneNumber != null
}

