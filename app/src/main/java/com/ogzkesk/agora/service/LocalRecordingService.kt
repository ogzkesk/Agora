package com.ogzkesk.agora.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.ServiceInfo
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import com.ogzkesk.agora.ui.MainActivity

class LocalRecordingService : Service() {

    override fun onCreate() {
        super.onCreate()
        val notification = getDefaultNotification()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                this.startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                )
            } else {
                this.startForeground(NOTIFICATION_ID, notification)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun getDefaultNotification(): Notification {
        val appInfo: ApplicationInfo = this.applicationContext.applicationInfo
        val name: String = applicationContext.packageManager.getApplicationLabel(appInfo).toString()
        val icon = Icon.createWithResource(this, appInfo.icon)

        val mChannel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH)
        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.createNotificationChannel(mChannel)

        val intent = Intent(this, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, CHANNEL_ID)
            .addAction(
                Notification.Action
                    .Builder(icon, "Back to app", activityPendingIntent)
                    .build()
            )
            .setContentText("Agora Recording ...")
            .setOngoing(true)
            .setSmallIcon(icon)
            .setTicker(name)
            .setWhen(System.currentTimeMillis())
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 1234567800
        const val CHANNEL_ID = "audio_channel_id"

        fun start(context: Context) {
            val serviceRef = LocalRecordingService::class.java
            val isServiceRunning = context.isServiceRunning(serviceRef)
            if (!isServiceRunning) {
                val serviceIntent = Intent(context, serviceRef)
                context.startForegroundService(serviceIntent)
            }
        }

        fun stop(context: Context) {
            val serviceRef = LocalRecordingService::class.java
            val isServiceRunning = context.isServiceRunning(serviceRef)
            if (isServiceRunning) {
                val serviceIntent = Intent(context, LocalRecordingService::class.java)
                context.stopService(serviceIntent)
            }
        }
    }
}
