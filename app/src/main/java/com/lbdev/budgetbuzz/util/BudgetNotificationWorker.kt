package com.lbdev.budgetbuzz.util

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class BudgetNotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val balance = inputData.getInt("balance", 0)
        val budgetNotificationService = BudgetNotificationService(applicationContext)
        budgetNotificationService.showNotification(balance)
        return Result.success()
    }
}