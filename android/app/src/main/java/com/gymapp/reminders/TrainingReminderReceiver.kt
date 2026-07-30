package com.gymapp.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TrainingReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ReminderScheduler.ACTION_REMINDER) return
        ReminderScheduler.notification(context, intent.getStringExtra(ReminderScheduler.EXTRA_PLAN_NAME).orEmpty())
        ReminderScheduler.rescheduleStored(context)
    }
}
