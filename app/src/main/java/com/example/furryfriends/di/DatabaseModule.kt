package com.example.furryfriends.di

import android.content.Context
import androidx.room.Room
import com.example.furryfriends.data.local.AppDatabase
import com.example.furryfriends.data.local.dao.CachedSearchDao
import com.example.furryfriends.data.local.dao.FavoritePetDao
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "furry_friends_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideFavoritePetDao(database: AppDatabase): FavoritePetDao {
        return database.favoritePetDao()
    }

    @Provides
    fun provideCachedSearchDao(database: AppDatabase): CachedSearchDao {
        return database.cachedSearchDao()
    }
}
