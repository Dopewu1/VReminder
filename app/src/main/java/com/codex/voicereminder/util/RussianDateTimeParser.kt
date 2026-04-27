package com.codex.voicereminder.util

import java.time.Clock
import java.time.DateTimeException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ParsedReminder(
    val taskText: String,
    val dateTime: LocalDateTime?
)

class RussianDateTimeParser(
    private val clock: Clock = Clock.systemDefaultZone()
) {
    private val zoneId: ZoneId = clock.zone
    private val explicitDateTimeFormatter = DateTimeFormatter.ofPattern("d.M.yyyy H:mm", Locale("ru"))

    fun parse(input: String): ParsedReminder {
        val original = input.trim()
        val normalized = original.lowercase(Locale("ru"))
            .replace(",", " ")
            .replace(Regex("\\s+"), " ")

        var working = normalized
        val now = LocalDateTime.now(clock).withSecond(0).withNano(0)
        var parsedDateTime: LocalDateTime? = null

        Regex("через\\s+(\\d+)\\s+(минут[уы]?|минут|час|часа|часов|день|дня|дней)")
            .find(working)
            ?.let { match ->
                val amount = match.groupValues[1].toLong()
                parsedDateTime = when {
                    match.groupValues[2].startsWith("мин") -> now.plusMinutes(amount)
                    match.groupValues[2].startsWith("час") -> now.plusHours(amount)
                    else -> now.plusDays(amount)
                }
                working = working.replace(match.value, " ")
            }

        if (parsedDateTime == null) {
            Regex("(сегодня|завтра|послезавтра)(?:\\s+в\\s+(\\d{1,2}):(\\d{2}))?")
                .find(working)
                ?.let { match ->
                    val baseDate = when (match.groupValues[1]) {
                        "сегодня" -> LocalDate.now(clock)
                        "завтра" -> LocalDate.now(clock).plusDays(1)
                        else -> LocalDate.now(clock).plusDays(2)
                    }
                    val hour = match.groupValues[2].toIntOrNull()
                    val minute = match.groupValues[3].toIntOrNull()
                    parsedDateTime = if (hour != null && minute != null) {
                        LocalDateTime.of(baseDate, LocalTime.of(hour, minute))
                    } else {
                        null
                    }
                    working = working.replace(match.value, " ")
                }
        }

        if (parsedDateTime == null) {
            Regex("(сегодня|завтра|послезавтра)\\s+(утром|днем|вечером|ночью)")
                .find(working)
                ?.let { match ->
                    val baseDate = when (match.groupValues[1]) {
                        "сегодня" -> LocalDate.now(clock)
                        "завтра" -> LocalDate.now(clock).plusDays(1)
                        else -> LocalDate.now(clock).plusDays(2)
                    }
                    val time = when (match.groupValues[2]) {
                        "утром" -> LocalTime.of(9, 0)
                        "днем" -> LocalTime.of(14, 0)
                        "вечером" -> LocalTime.of(19, 0)
                        else -> LocalTime.of(22, 0)
                    }
                    parsedDateTime = LocalDateTime.of(baseDate, time)
                    working = working.replace(match.value, " ")
                }
        }

        if (parsedDateTime == null) {
            Regex("(\\d{1,2})\\.(\\d{1,2})(?:\\.(\\d{2,4}))?(?:\\s*(?:в)?\\s*(\\d{1,2}):(\\d{2}))?")
                .find(working)
                ?.let { match ->
                    val day = match.groupValues[1].toInt()
                    val month = match.groupValues[2].toInt()
                    val year = match.groupValues[3]
                        .takeIf { it.isNotBlank() }
                        ?.toInt()
                        ?.let { if (it < 100) 2000 + it else it }
                        ?: LocalDate.now(clock).year
                    val hour = match.groupValues[4].toIntOrNull() ?: 9
                    val minute = match.groupValues[5].toIntOrNull() ?: 0
                    parsedDateTime = try {
                        LocalDateTime.parse(
                            "$day.$month.$year $hour:$minute",
                            explicitDateTimeFormatter
                        )
                    } catch (_: DateTimeException) {
                        null
                    }
                    working = working.replace(match.value, " ")
                }
        }

        if (parsedDateTime == null) {
            Regex("\\bв\\s+(\\d{1,2}):(\\d{2})\\b")
                .find(working)
                ?.let { match ->
                    val hour = match.groupValues[1].toInt()
                    val minute = match.groupValues[2].toInt()
                    val candidate = LocalDateTime.of(LocalDate.now(clock), LocalTime.of(hour, minute))
                    parsedDateTime = if (candidate.isAfter(now)) candidate else candidate.plusDays(1)
                    working = working.replace(match.value, " ")
                }
        }

        val taskText = working
            .replace(Regex("\\b(напомни|напомнить|напоминание|мне|пожалуйста)\\b"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("ru")) else it.toString() }

        return ParsedReminder(
            taskText = taskText,
            dateTime = parsedDateTime
        )
    }

    fun toEpochMillis(dateTime: LocalDateTime): Long {
        return dateTime.atZone(zoneId).toInstant().toEpochMilli()
    }

    fun fromEpochMillis(value: Long): LocalDateTime {
        return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(value), zoneId)
    }
}
