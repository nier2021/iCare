package com.docter.icare.utils

import com.google.android.material.datepicker.*
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat.CLOCK_24H
import java.lang.System.currentTimeMillis
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields.SUNDAY_START
import android.content.Context
import android.os.Build
import android.text.format.DateFormat
import java.text.SimpleDateFormat

val DATE_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
val DATE_TIME_NO_SECOND_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
val DATE_TIME_NO_SECOND_SHOW_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
val DATE_SHOW_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
val YEAR_MONTH_SHOW_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM")
val MONTH_DAY_SHOW_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd")
val DAY_SHOW_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd")
val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
val HOUR_MINUTE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

class TimeUtils(
    context: Context
) {

    private val appContext = context.applicationContext

    val is24: Boolean get() = DateFormat.is24HourFormat(appContext)
}

fun Long.toDelayTime(): Long = (currentTimeMillis() - this).let {
    when {
        it < 3000L -> 3000L - it
        else -> 0L
    }
}

fun Long.toLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())



fun LocalDate.toDatePicker(
    isLimitDate: Boolean
) = MaterialDatePicker.Builder
    .datePicker()
    .setTitleText("")
    .setSelection(LocalDateTime.of(this, LocalTime.now()).toInstant(ZoneOffset.UTC).toEpochMilli())  //Set start date
    .also { if (isLimitDate) it.setCalendarConstraints(this.toCalendarConstraints(2018)) }  //Set available date
    .build()

fun LocalDate.toCalendarConstraints(
    startYear: Int
): CalendarConstraints = CalendarConstraints.Builder()
    .setOpenAt(LocalDateTime.of(this, LocalTime.now()).toInstant(ZoneOffset.UTC).toEpochMilli())
    .setValidator(
        CompositeDateValidator.allOf(listOf(
            DateValidatorPointForward.from(LocalDateTime.of(startYear, 1, 1, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli()),
            DateValidatorPointBackward.now()
        )))
    .build()

fun LocalDateTime.toShowDateTimeNoSecondTwoLine(): String = "${this.format(DATE_SHOW_FORMAT)}\n${this.format(HOUR_MINUTE_FORMAT)}"

fun LocalDate.isNowWeek(): Boolean = this.toFirstDateInWeek() == LocalDate.now().toFirstDateInWeek()

fun LocalDate.toFirstDateInWeek(): LocalDate = this.with(SUNDAY_START.dayOfWeek(), 1L)

fun LocalDate.toWeekDayStartString(): String = when (dayOfWeek) {
    DayOfWeek.SUNDAY -> "週日 "
    DayOfWeek.MONDAY -> "週一 "
    DayOfWeek.TUESDAY -> "週二 "
    DayOfWeek.WEDNESDAY -> "週三 "
    DayOfWeek.THURSDAY -> "週四 "
    DayOfWeek.FRIDAY -> "週五 "
    DayOfWeek.SATURDAY -> "週六 "
} + this.format(DATE_SHOW_FORMAT)

fun LocalDate.toWeekDayEndString(): String = this.format(DATE_SHOW_FORMAT) +
        when (dayOfWeek) {
            DayOfWeek.SUNDAY -> " 週日"
            DayOfWeek.MONDAY -> " 週一"
            DayOfWeek.TUESDAY -> " 週二"
            DayOfWeek.WEDNESDAY -> " 週三"
            DayOfWeek.THURSDAY -> " 週四"
            DayOfWeek.FRIDAY -> " 週五"
            DayOfWeek.SATURDAY -> " 週六"
        }

fun LocalDate.toWeekString(): String = this.with(SUNDAY_START.dayOfWeek(), 1L).format(DATE_SHOW_FORMAT) +
        "~" +
        this.with(SUNDAY_START.dayOfWeek(), 7L).format(DATE_SHOW_FORMAT)

fun LocalDate.toWeekDateList(): List<LocalDate> = (1L..7L).map { this.with(SUNDAY_START.dayOfWeek(), it) }

fun YearMonth.toMonthString(): String = this.format(YEAR_MONTH_SHOW_FORMAT) + "月"

fun LocalDate.toMonthDateList(): List<LocalDate> = (1..this.lengthOfMonth()).map { this.withDayOfMonth(it) }

fun LocalDate.toMonthList(): List<String> = mutableListOf<String>().apply {

    this@toMonthList.withDayOfMonth(1).let { firstDate ->

        add(firstDate.format(DATE_SHOW_FORMAT) +
                "~" +
                firstDate.with(SUNDAY_START.dayOfWeek(), 7L).format(MONTH_DAY_SHOW_FORMAT))

        var week = 1L

        do {

            firstDate.plusWeeks(week).let {

                add(it.with(SUNDAY_START.dayOfWeek(), 1L).format(DATE_SHOW_FORMAT) +
                        "~" +
                        when {
                            (week.toInt() + 1) == firstDate.toWeekOfMonthCount() -> firstDate.withDayOfMonth(firstDate.lengthOfMonth()).format(MONTH_DAY_SHOW_FORMAT)
                            else -> it.with(SUNDAY_START.dayOfWeek(), 7L).format(MONTH_DAY_SHOW_FORMAT)
                        }
                )
            }

            week++

        } while (week.toInt() != firstDate.toWeekOfMonthCount())
    }
}

fun LocalDate.toMonthListNoYear(): List<String> = mutableListOf<String>().apply {

    this@toMonthListNoYear.withDayOfMonth(1).let { firstDate ->

        add(firstDate.format(MONTH_DAY_SHOW_FORMAT) +
                "~" +
                firstDate.with(SUNDAY_START.dayOfWeek(), 7L).format(DAY_SHOW_FORMAT))

        var week = 1L

        do {

            firstDate.plusWeeks(week).let {

                add(it.with(SUNDAY_START.dayOfWeek(), 1L).format(MONTH_DAY_SHOW_FORMAT) +
                        "~" +
                        when {
                            (week.toInt() + 1) == firstDate.toWeekOfMonthCount() -> firstDate.withDayOfMonth(firstDate.lengthOfMonth()).format(DAY_SHOW_FORMAT)
                            else -> it.with(SUNDAY_START.dayOfWeek(), 7L).format(DAY_SHOW_FORMAT)
                        }
                )
            }

            week++

        } while (week.toInt() != firstDate.toWeekOfMonthCount())
    }
}

fun LocalDate.toWeekOfMonthCount(): Int = this.withDayOfMonth(this.lengthOfMonth()).get(SUNDAY_START.weekOfMonth())

fun LocalDate.isNowSeason(): Boolean = this.toFirstMonthInSeason() == LocalDate.now().toFirstMonthInSeason()

fun LocalDate.toFirstMonthInSeason(): Int = ((monthValue - 1) / 3) * 3 + 1

fun LocalDate.toSeasonDate(): LocalDate = this.withMonth(this.toFirstMonthInSeason()).withDayOfMonth(1)

fun LocalDate.toSeasonString(): String = YearMonth.from(this).toMonthString() +
        "~" +
        YearMonth.from(this).plusMonths(2L).toMonthString()

fun LocalDate.toSeasonDateList(): List<LocalDate> = mutableListOf<LocalDate>().apply {

    (0L..2L).map { this@toSeasonDateList.plusMonths(it) }.forEach { addAll(it.toMonthDateList()) }
}

fun LocalDate.toSeasonList(): List<String> = (0L..2L).map { this.plusMonths(it).format(YEAR_MONTH_SHOW_FORMAT) }

//fun LocalDate.toDateList(
//    dateInterval: String
//): List<LocalDate> = when (dateInterval) {
//    DATE_INTERVAL_WEEK -> this.toWeekDateList()
//    DATE_INTERVAL_MONTH -> this.toMonthDateList()
//    DATE_INTERVAL_SEASON -> this.toSeasonDateList()
//    else -> listOf(this)
//}

fun LocalDate.isHistory() = this.isBefore(LocalDate.now())

fun LocalTime.toTimePicker() = MaterialTimePicker.Builder()
    .setTimeFormat(CLOCK_24H)
    .setHour(hour)
    .setMinute(minute)
    .build()

fun Pair<String, String>.toLocalDateTime(): LocalDateTime = LocalDateTime.of(LocalDate.parse(first), LocalTime.parse(second, TIME_FORMAT))

fun String.toShowDate(): String = LocalDate.parse(this).format(DATE_SHOW_FORMAT)

fun String.toLocalDate(): LocalDate = LocalDate.parse(this)

fun String.toShowTimeNoSecond(): String = LocalTime.parse(this).format(HOUR_MINUTE_FORMAT)

fun String.toRadarShowDate(): String = "${LocalDateTime.parse(this, DATE_TIME_FORMAT).format(DATE_FORMAT)}\n${LocalDateTime.parse(this, DATE_TIME_FORMAT).format(TIME_FORMAT)}"