package com.minicount.app.di

import android.content.Context
import androidx.room.Room
import com.minicount.app.data.local.MiniCountDatabase
import com.minicount.app.data.local.dao.EventDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MiniCountDatabase {
        return Room.databaseBuilder(
            context,
            MiniCountDatabase::class.java,
            "minicount_db"
        ).build()
    }

    @Provides
    fun provideEventDao(database: MiniCountDatabase): EventDao {
        return database.eventDao()
    }
}
