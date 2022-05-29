package com.example.weighttracker.repository.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "WeightTrackerDataTable")
data class WTDataValue(
    @PrimaryKey
    var date: Long,

    @ColumnInfo
    var weight: Float, //Weight in Kilograms

    @ColumnInfo
    var skipped: Boolean = false
)