package com.lbdev.budgetbuzz.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.lbdev.budgetbuzz.R
import com.lbdev.budgetbuzz.ui.view.HomeActivity

class BudgetNotificationService(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showNotification(balance: Int) {
        val activityIntent = Intent(context, HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 1, activityIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = if (balance < 0) {
            NotificationCompat.Builder(context, BUDGET_CHANNEL_ID)
                .setContentTitle("Budget Alert")
                .setContentText("You have exceeded your budget!")
                .setSmallIcon(R.drawable.ic_stat_budgetbuzz_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
        } else if (balance < 100) {
            NotificationCompat.Builder(context, BUDGET_CHANNEL_ID)
                .setContentTitle("Budget Alert")
                .setContentText("You are close to exceeding your budget!")
                .setSmallIcon(R.drawable.ic_stat_budgetbuzz_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
        } else {
            NotificationCompat.Builder(context, BUDGET_CHANNEL_ID)
                .setContentTitle("Your Remaining Budget is : $balance")
                .setContentText("Keep Saving Wisely!")
                .setSmallIcon(R.drawable.ic_stat_budgetbuzz_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
        }

        notificationManager.notify(1, notification)
    }

    companion object {
        const val BUDGET_CHANNEL_ID = "budget_channel"
    }
}