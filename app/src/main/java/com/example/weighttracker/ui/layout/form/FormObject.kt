package com.example.weighttracker.ui.layout.form

import java.util.*

interface FormObject {
    fun getCalender(): Calendar
}


object MaterialDate: FormObject {
    override fun getCalender(): Calendar = Calendar.getInstance()
}

