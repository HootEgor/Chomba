package com.egorhoot.chomba.di

import android.content.Context
import com.egorhoot.chomba.util.StringProvider // Import the actual StringProvider from shared.androidMain
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StringProviderModule {

    @Provides
    @Singleton
    fun provideStringProvider(@ApplicationContext context: Context): StringProvider {
        return StringProvider(context)
    }
}
