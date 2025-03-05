package com.ogzkesk.agora.di

import android.content.Context
import com.ogzkesk.agora.lib.CallCache
import com.ogzkesk.agora.lib.RtcEventListener
import com.ogzkesk.agora.lib.TokenUtils
import com.ogzkesk.agora.lib.controller.Controller
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AgoraModule {

    @Provides
    @Singleton
    fun provideCallCache() = CallCache()

    @Provides
    @Singleton
    fun provideEventListener(
        callCache: CallCache
    ) = RtcEventListener(callCache)

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
        config: RtcEngineConfig,
        eventListener: RtcEventListener
    ): RtcEngine = try {
        RtcEngine.create(config).apply {
            addHandler(eventListener)
        }
    } catch (e: Exception) {
        throw RuntimeException("Error initializing RTC engine: ${e.message}")
    }

    @Provides
    @Singleton
    fun provideController(
        rtcEngine: RtcEngine,
        callCache: CallCache,
    ) = Controller(rtcEngine, callCache)
}

