package com.gymapp.reminders

enum class ReminderDestination { CATALOG, TODAY }

fun reminderDestination(openToday: Boolean) = if (openToday) ReminderDestination.TODAY else ReminderDestination.CATALOG
