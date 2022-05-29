package com.example.weighttracker.viewmodel

import androidx.annotation.IntRange
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.weighttracker.repository.WTRoomRepository
import com.example.weighttracker.repository.database.WTDataValue
import com.example.weighttracker.repository.util.WTDateConverter
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class WTViewModel @Inject constructor(
    private val repository: WTRoomRepository,
    private val firebaseDB: FirebaseDatabase
): ViewModel() {

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
        weight: Float // Weight Hopefully will be less than 75 in a few weeks, so no int range
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
}