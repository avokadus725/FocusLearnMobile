package com.example.focuslearnmobile.di

import android.content.Context
import com.example.focuslearnmobile.data.local.LanguageManager
import com.example.focuslearnmobile.data.local.TokenStorage
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
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideTokenStorage(@ApplicationContext context: Context): TokenStorage {
        return TokenStorage(context)
    }

    @Provides
    @Singleton
    fun provideLanguageManager(@ApplicationContext context: Context): LanguageManager {
        return LanguageManager(context)
    }
}