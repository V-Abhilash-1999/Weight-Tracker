package com.example.weighttracker.repository

import androidx.lifecycle.LiveData
import com.example.weighttracker.repository.database.WTDataValue
import com.example.weighttracker.repository.database.WTDatabaseTableSchema
import javax.inject.Inject

class WTRoomRepository @Inject constructor(wtDatabase: WTDatabaseTableSchema.WTDatabase): WTDatabaseTableSchema.WTDAO() {
    private val dao: WTDatabaseTableSchema.WTDAO = wtDatabase.getDAO()

    override suspend fun insert(data: WTDataValue) = dao.insert(data)

    override suspend fun updateWeight(weight: Float, date: String) = dao.updateWeight(weight, date)

    override suspend fun delete(data: WTDataValue) = dao.delete(data)

    override fun getWeightOn(date: Long): LiveData<WTDataValue> = dao.getWeightOn(date)

    override fun getWeight(): LiveData<List<WTDataValue>> = dao.getWeight()

}