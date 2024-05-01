package com.lbdev.budgetbuzz.util

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createBudgetNotificationChannel()
    }

    private fun createBudgetNotificationChannel() {
        val channel = NotificationChannel(
            BudgetNotificationService.BUDGET_CHANNEL_ID,
            "Budget",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "Used for budget related notifications"

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}