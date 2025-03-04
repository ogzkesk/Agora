package com.ogzkesk.agora.di

import android.content.Context
import com.ogzkesk.agora.audio.AudioController
import com.ogzkesk.agora.audio.TokenUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AgoraModule {

    @Provides
    @Singleton
    fun provideRtcEngineConfig(
        @ApplicationContext context: Context
    ) = RtcEngineConfig().apply {
        mContext = context
        mAppId = TokenUtils.APP_ID
    }

    @Provides
    @Singleton
    fun provideRtcEngine(
        config: RtcEngineConfig
    ): RtcEngine = try {
        RtcEngine.create(config)
    } catch (e: Exception) {
        throw RuntimeException("Error initializing RTC engine: ${e.message}")
    }

    @Provides
    @Singleton
    fun provideAudioController(
        rtcEngine: RtcEngine
    ) = AudioController(rtcEngine)
}

