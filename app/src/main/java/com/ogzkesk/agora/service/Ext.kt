package com.ogzkesk.agora.service

import android.app.ActivityManager
import android.app.Service
import android.content.Context

fun Context.isServiceRunning(serviceClass: Class<out Service>): Boolean {
    val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (process in manager.runningAppProcesses) {
        if (process.processName == serviceClass.name) {
            return true
        }
    }
    return false
}
