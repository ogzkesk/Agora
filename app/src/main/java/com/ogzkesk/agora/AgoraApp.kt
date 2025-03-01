package com.ogzkesk.agora

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AgoraApp : Application()  {

    override fun onCreate() {
        super.onCreate()
    }
}