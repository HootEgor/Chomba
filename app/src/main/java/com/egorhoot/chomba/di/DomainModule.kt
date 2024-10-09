package com.egorhoot.chomba.di

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.egorhoot.chomba.pages.PageState
import com.egorhoot.chomba.pages.onlinegame.OnLineGameUiState
import com.egorhoot.chomba.pages.user.ProfileScreenUiState
import com.egorhoot.chomba.utils.IdConverter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    @Provides
    fun provideIdConverter(@ApplicationContext context: Context): IdConverter {
        return IdConverter(context)
    }

    @Provides
    @Singleton
    fun provideProfileUiState(): ProfileScreenUiState {
        return ProfileScreenUiState()
    }

    @Provides
    @Singleton
    fun provideMutableProfileUiState(): MutableState<ProfileScreenUiState> {
        return mutableStateOf(ProfileScreenUiState())
    }

    @Provides
    @Singleton
    fun providePageState(): PageState {
        return PageState()
    }

    @Provides
    @Singleton
    fun provideMutablePageState(): MutableState<PageState> {
        return mutableStateOf(PageState())
    }

    @Provides
    @Singleton
    fun provideMutableOnLineGameUiState(): MutableState<OnLineGameUiState> {
        return mutableStateOf(OnLineGameUiState())
    }

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun providesFirebaseFirestore(): FirebaseFirestore {
        return Firebase.firestore
    }
}