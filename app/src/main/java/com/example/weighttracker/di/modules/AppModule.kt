package com.example.weighttracker.di.modules

import android.content.Context
import android.content.SharedPreferences
import com.example.weighttracker.repository.database.WTDatabaseTableSchema
import com.example.weighttracker.ui.util.WTConstant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@JvmSuppressWildcards
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideSharedPref(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(WTConstant.SHARED_PREF, Context.MODE_PRIVATE)
    }

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): WTDatabaseTableSchema.WTDatabase {
        return WTDatabaseTableSchema.WTDatabase.Instance.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return FirebaseDatabase.getInstance("https://weight-tracker-bd252-default-rtdb.europe-west1.firebasedatabase.app")
    }

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
}

