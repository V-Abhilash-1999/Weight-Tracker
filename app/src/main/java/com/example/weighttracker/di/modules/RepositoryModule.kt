package com.example.weighttracker.di.modules

import com.example.weighttracker.repository.WTRoomRepository
import com.example.weighttracker.repository.database.WTDatabaseTableSchema
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import javax.inject.Named

@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {
    @Provides
    @Named("repository")
    fun provideRepository(wtDatabase: WTDatabaseTableSchema.WTDatabase): WTRoomRepository {
        return WTRoomRepository(wtDatabase)
    }
}