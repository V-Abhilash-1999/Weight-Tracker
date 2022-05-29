package com.example.weighttracker.repository.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import dagger.android.ContributesAndroidInjector

class WTDatabaseTableSchema {
    @Database(
        entities =  [WTDataValue::class],
        version = 1
    )
    abstract class WTDatabase: RoomDatabase() {
        object Instance {
            private var instance: WTDatabase? = null
            private var databaseName = "WTDatabase"

            @Synchronized
            fun getInstance(context: Context): WTDatabase {
                if (instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        WTDatabase::class.java,
                        databaseName
                    ).fallbackToDestructiveMigration()
                        .build()
                }
                return instance!!
            }
        }

        abstract fun getDAO(): WTDAO
    }


    @Dao
    abstract class WTDAO {
        @Insert
        abstract suspend fun insert(data: WTDataValue)

        @Query("UPDATE WeightTrackerDataTable SET weight = :weight WHERE date = :date")
        abstract suspend fun updateWeight(weight:Float, date:String)

        @Delete
        abstract suspend fun delete(data: WTDataValue)

        @Query("SELECT * FROM WeightTrackerDataTable where date = :date")
        abstract fun getWeightOn(date: Long): LiveData<WTDataValue>

        @Query("SELECT * FROM WeightTrackerDataTable")
        abstract fun getWeight() : LiveData<List<WTDataValue>>
    }
}