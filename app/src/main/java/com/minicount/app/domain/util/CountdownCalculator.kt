package com.minicount.app.domain.util

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs

data class CountdownData(
    val years: Long = 0,
    val months: Long = 0,
    val days: Long = 0,
    val hours: Long = 0,
    val minutes: Long = 0,
    val seconds: Long = 0,
    val isPast: Boolean = false,
    val totalDays: Long = 0
)

object CountdownCalculator {

    fun calculate(targetDate: LocalDateTime): CountdownData {
        val now = LocalDateTime.now()
        val isPast = now.isAfter(targetDate)

        val startDate = if (isPast) targetDate else now
        val endDate = if (isPast) now else targetDate

        var years = ChronoUnit.YEARS.between(startDate, endDate)
        var tempDate = startDate.plusYears(years)

        var months = ChronoUnit.MONTHS.between(tempDate, endDate)
        tempDate = tempDate.plusMonths(months)

        var days = ChronoUnit.DAYS.between(tempDate, endDate)
        tempDate = tempDate.plusDays(days)

        val hours = ChronoUnit.HOURS.between(tempDate, endDate)
        tempDate = tempDate.plusHours(hours)

        val minutes = ChronoUnit.MINUTES.between(tempDate, endDate)
        tempDate = tempDate.plusMinutes(minutes)

        val seconds = ChronoUnit.SECONDS.between(tempDate, endDate)

        val totalDays = abs(ChronoUnit.DAYS.between(now, targetDate))

        return CountdownData(
            years = years,
            months = months,
            days = days,
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            isPast = isPast,
            totalDays = totalDays
        )
    }

    fun formatCountdown(data: CountdownData, showSeconds: Boolean = false): String {
        return buildString {
            if (data.years > 0) append("${data.years}y ")
            if (data.months > 0) append("${data.months}mo ")
            if (data.days > 0) append("${data.days}d ")
            if (data.hours > 0 || data.years == 0L) append("${data.hours}h ")
            if (data.minutes > 0 || (data.years == 0L && data.months == 0L)) {
                append("${data.minutes}m")
            }
            if (showSeconds && data.years == 0L && data.months == 0L && data.days == 0L) {
                append(" ${data.seconds}s")
            }
        }.trim()
    }

    fun formatShort(data: CountdownData): String {
        return when {
            data.years > 0 -> "${data.years}y ${data.months}mo"
            data.months > 0 -> "${data.months}mo ${data.days}d"
            data.days > 0 -> "${data.days}d ${data.hours}h"
            data.hours > 0 -> "${data.hours}h ${data.minutes}m"
            else -> "${data.minutes}m ${data.seconds}s"
        }
    }

    fun getNextOccurrence(baseDate: LocalDateTime, interval: com.minicount.app.data.local.entity.RepeatInterval): LocalDateTime {
        val now = LocalDateTime.now()
        var nextDate = baseDate

        while (nextDate.isBefore(now)) {
            nextDate = when (interval) {
                com.minicount.app.data.local.entity.RepeatInterval.DAILY -> nextDate.plusDays(1)
                com.minicount.app.data.local.entity.RepeatInterval.WEEKLY -> nextDate.plusWeeks(1)
                com.minicount.app.data.local.entity.RepeatInterval.MONTHLY -> nextDate.plusMonths(1)
                com.minicount.app.data.local.entity.RepeatInterval.YEARLY -> nextDate.plusYears(1)
                com.minicount.app.data.local.entity.RepeatInterval.NONE -> return baseDate
            }
        }

        return nextDate
    }
}
