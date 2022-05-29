package com.example.weighttracker.di.modules

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.weighttracker.repository.WTRoomRepository
import com.example.weighttracker.repository.database.WTDatabaseTableSchema
import com.example.weighttracker.viewmodel.WTViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.multibindings.IntoMap
import javax.inject.Named

@Module
@InstallIn(ViewModelComponent::class)
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(WTViewModel::class)
    @Named("wtViewModel")
    abstract fun provideWTViewModel(viewModel: WTViewModel): ViewModel
}