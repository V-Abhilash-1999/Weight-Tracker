package com.example.weighttracker

import android.app.Application
import com.example.weighttracker.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WTApplication: Application()