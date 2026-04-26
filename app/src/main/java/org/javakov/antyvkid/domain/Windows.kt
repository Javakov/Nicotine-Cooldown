package org.javakov.antyvkid.domain

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

enum class WindowSlot(val openHour: Int, val label: String) {
    MORNING(9, "Утро"),
    EVENING(21, "Вечер");

    fun openTime(): LocalTime = LocalTime.of(openHour, 0)
    fun closeTime(): LocalTime = LocalTime.of(openHour, 59, 59, 999_000_000)
}

data class WindowInfo(
    val slot: WindowSlot,
    val opensAt: ZonedDateTime,
    val closesAt: ZonedDateTime
) {
    fun contains(moment: ZonedDateTime): Boolean =
        !moment.isBefore(opensAt) && !moment.isAfter(closesAt)
}

object WindowCalculator {

    fun zone(): ZoneId = ZoneId.systemDefault()

    fun now(): ZonedDateTime = ZonedDateTime.now(zone())

    fun nowMillis(): Long = System.currentTimeMillis()

    fun currentWindow(moment: ZonedDateTime = now()): WindowInfo? =
        WindowSlot.entries
            .map { windowFor(it, moment.toLocalDate()) }
            .firstOrNull { it.contains(moment) }

    fun nextWindow(moment: ZonedDateTime = now()): WindowInfo {
        val today = moment.toLocalDate()
        val candidates = WindowSlot.entries.map { windowFor(it, today) }
        val upcoming = candidates.firstOrNull { moment.isBefore(it.opensAt) }
        if (upcoming != null) return upcoming
        return windowFor(WindowSlot.MORNING, today.plusDays(1))
    }

    private fun windowFor(slot: WindowSlot, date: java.time.LocalDate): WindowInfo {
        val zone = zone()
        val opens = LocalDateTime.of(date, slot.openTime()).atZone(zone)
        val closes = LocalDateTime.of(date, slot.closeTime()).atZone(zone)
        return WindowInfo(slot, opens, closes)
    }
}
