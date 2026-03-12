package com.foccus.app.di

import com.foccus.app.data.repository.BlockedAppRepositoryImpl
import com.foccus.app.data.repository.FocusSessionRepositoryImpl
import com.foccus.app.domain.repository.BlockedAppRepository
import com.foccus.app.domain.repository.FocusSessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindBlockedAppRepository(
        impl: BlockedAppRepositoryImpl
    ): BlockedAppRepository

    @Binds
    @Singleton
    abstract fun bindFocusSessionRepository(
        impl: FocusSessionRepositoryImpl
    ): FocusSessionRepository
}
