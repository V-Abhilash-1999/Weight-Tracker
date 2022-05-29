package com.example.weighttracker.di

import com.example.weighttracker.WTApplication
import com.example.weighttracker.di.modules.AppModule
import com.example.weighttracker.di.modules.RepositoryModule
import com.example.weighttracker.di.modules.ViewModelModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector


@Component(
    modules = [AndroidInjectionModule::class, AppModule::class, RepositoryModule::class, ViewModelModule::class]
)
interface AppComponent : AndroidInjector<WTApplication> {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: WTApplication): Builder

        fun build(): AppComponent
    }
    override fun inject(app: WTApplication)
}
