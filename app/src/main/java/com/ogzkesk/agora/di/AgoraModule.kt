package com.ogzkesk.agora.di

import android.content.Context
import com.ogzkesk.agora.controller.AudioController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AgoraModule {

    @Provides
    @Singleton
    fun provideAudioController(
        @ApplicationContext context: Context
    ) = AudioController(context).apply { initialize() }
}
