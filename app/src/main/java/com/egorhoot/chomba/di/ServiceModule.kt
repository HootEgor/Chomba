package com.egorhoot.chomba.di

import com.egorhoot.chomba.repo.UserRepository
import com.egorhoot.chomba.repo.impl.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {
    @Binds
    abstract fun provideUserRepository(impl: UserRepositoryImpl): UserRepository
}