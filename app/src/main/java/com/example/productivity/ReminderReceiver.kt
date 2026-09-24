package com.example.productivity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.R

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val title = intent?.getStringExtra("TITLE") ?: "TITONOX Reminder"
        val reminderId = intent?.getLongExtra("REMINDER_ID", System.currentTimeMillis()) ?: System.currentTimeMillis()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "titonox_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "TITONOX Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders and Alerts from TITONOX AI Assistant"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("TITONOX Alert")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .build()

        notificationManager.notify(reminderId.toInt(), notification)
    }
}
