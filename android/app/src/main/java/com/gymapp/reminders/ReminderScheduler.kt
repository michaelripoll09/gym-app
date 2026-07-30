package com.gymapp.reminders

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.gymapp.MainActivity
import com.gymapp.network.WorkoutPlanResponse

object ReminderScheduler {
    const val ACTION_REMINDER = "com.gymapp.REMINDER"
    const val EXTRA_PLAN_NAME = "plan_name"
    const val EXTRA_OPEN_TODAY = "open_today"
    private const val CHANNEL_ID = "training_reminders"

    fun reschedule(context: Context, plans: List<WorkoutPlanResponse>) {
        val store = ReminderStore(context)
        store.savePlans(plans)
        ReminderScheduleController(AndroidAlarmGateway(context, store)).apply(plans, store.readSettings())
    }

    fun rescheduleStored(context: Context) {
        val store = ReminderStore(context)
        ReminderScheduleController(AndroidAlarmGateway(context, store)).apply(store.readPlans(), store.readSettings())
    }

    fun notification(context: Context, planName: String) {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Recordatorios de entrenamiento", NotificationManager.IMPORTANCE_DEFAULT))
        val intent = Intent(context, MainActivity::class.java).putExtra(EXTRA_OPEN_TODAY, true).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = androidx.core.app.NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Entrenamiento programado")
            .setContentText("Hoy te toca $planName")
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()
        manager.notify(planName.hashCode(), notification)
    }

    private class AndroidAlarmGateway(private val context: Context, private val store: ReminderStore) : ReminderAlarmGateway {
        private val alarms = context.getSystemService(AlarmManager::class.java)
        override fun cancelAll() {
            store.readPlans().flatMap { plan -> plan.days.map { day -> (plan.id + day.name).hashCode() } }.distinct().forEach { requestCode ->
                alarms.cancel(pendingIntent(requestCode, ""))
            }
        }
        override fun schedule(reminder: ScheduledReminder) {
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.triggerAt.toEpochMilli(), pendingIntent(reminder.requestCode, reminder.planName))
        }
        private fun pendingIntent(requestCode: Int, planName: String): PendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, TrainingReminderReceiver::class.java).setAction(ACTION_REMINDER).putExtra(EXTRA_PLAN_NAME, planName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
