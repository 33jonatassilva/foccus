package com.foccus.app.di

import android.content.Context
import androidx.room.Room
import com.foccus.app.data.local.db.FoccusDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FoccusDatabase =
        Room.databaseBuilder(
            context,
            FoccusDatabase::class.java,
            FoccusDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideBlockedAppDao(db: FoccusDatabase) = db.blockedAppDao()

    @Provides
    fun provideFocusSessionDao(db: FoccusDatabase) = db.focusSessionDao()
}
