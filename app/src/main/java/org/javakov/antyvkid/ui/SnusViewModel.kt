package org.javakov.antyvkid.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.javakov.antyvkid.data.SnusRepository
import org.javakov.antyvkid.data.SnusState
import org.javakov.antyvkid.domain.WindowCalculator
import org.javakov.antyvkid.domain.WindowInfo
import org.javakov.antyvkid.domain.WindowSlot
import java.time.Instant
import java.time.ZonedDateTime

enum class SnusStatus { CanSubmit, AlreadyUsed, Waiting, Blocked }

data class SnusUiState(
    val status: SnusStatus = SnusStatus.Waiting,
    val countdownSeconds: Long = 0L,
    val countdownLabel: String = "00:00:00",
    val currentWindow: WindowSlot? = null,
    val nextWindow: WindowSlot = WindowSlot.MORNING,
    val nextWindowAtLabel: String = "09:00",
    val progress: Float = 0f,
    val justSubmitted: Boolean = false,
    val morningDone: Boolean = false,
    val eveningDone: Boolean = false
)

class SnusViewModel(private val repo: SnusRepository) : ViewModel() {

    private val _state = MutableStateFlow(SnusUiState())
    val state: StateFlow<SnusUiState> = _state.asStateFlow()

    /** Кэш из DataStore: обновляется ~раз в секунду, прогресс кольца — каждый тик. */
    private var cachedPersisted: SnusState = SnusState.EMPTY

    init {
        viewModelScope.launch {
            var tick = 0
            while (true) {
                if (tick % 4 == 0) {
                    refreshPersistedAndPush()
                } else {
                    pushFromClockOnly()
                }
                delay(1000L)
                tick++
            }
        }
    }

    fun submit() {
        viewModelScope.launch {
            val persisted = repo.state.first()
            val nowMillis = WindowCalculator.nowMillis()
            if (nowMillis < persisted.lastKnownSystemTime) return@launch
            val now = ZonedDateTime.now(WindowCalculator.zone())
            val window = WindowCalculator.currentWindow(now) ?: return@launch
            if (alreadyUsedInWindow(persisted, window)) return@launch
            _state.update { it.copy(justSubmitted = true) }
            repo.recordSubmit(nowMillis, window.slot)
            refreshPersistedAndPush()
            delay(900L)
            _state.update { it.copy(justSubmitted = false) }
        }
    }

    private suspend fun refreshPersistedAndPush() {
        cachedPersisted = repo.state.first()
        val nowMillis = WindowCalculator.nowMillis()
        val now = ZonedDateTime.now(WindowCalculator.zone())
        val rollback = cachedPersisted.lastKnownSystemTime > 0L && nowMillis < cachedPersisted.lastKnownSystemTime
        if (!rollback) repo.touchSystemTime(nowMillis)
        pushUi(cachedPersisted, now, rollback)
    }

    private fun pushFromClockOnly() {
        val nowMillis = WindowCalculator.nowMillis()
        val now = ZonedDateTime.now(WindowCalculator.zone())
        val rollback = cachedPersisted.lastKnownSystemTime > 0L && nowMillis < cachedPersisted.lastKnownSystemTime
        pushUi(cachedPersisted, now, rollback)
    }

    private fun pushUi(persisted: SnusState, now: ZonedDateTime, rollback: Boolean) {
        val current = WindowCalculator.currentWindow(now)
        val next = WindowCalculator.nextWindow(now)

        val (realStatus, target) = when {
            rollback -> SnusStatus.Blocked to next.opensAt
            current != null && alreadyUsedInWindow(persisted, current) ->
                SnusStatus.AlreadyUsed to next.opensAt
            current != null -> SnusStatus.CanSubmit to current.closesAt
            else -> SnusStatus.Waiting to next.opensAt
        }

        val remainingMs = target.toInstant().toEpochMilli() - now.toInstant().toEpochMilli()
        val seconds = (remainingMs / 1000L).coerceAtLeast(0L)
        val progress = computeProgress(realStatus, now, current, next)

        _state.update {
            it.copy(
                status = realStatus,
                countdownSeconds = seconds,
                countdownLabel = formatHms(seconds),
                currentWindow = current?.slot,
                nextWindow = next.slot,
                nextWindowAtLabel = "%02d:00".format(next.slot.openHour),
                progress = progress,
                morningDone = isWindowDoneToday(persisted, WindowSlot.MORNING, now),
                eveningDone = isWindowDoneToday(persisted, WindowSlot.EVENING, now)
            )
        }
    }

    private fun alreadyUsedInWindow(persisted: SnusState, window: WindowInfo): Boolean {
        val ts = when (window.slot) {
            WindowSlot.MORNING -> persisted.lastMorningSubmitTimestamp
            WindowSlot.EVENING -> persisted.lastEveningSubmitTimestamp
        }
        if (ts <= 0L) return false
        val zoned = Instant.ofEpochMilli(ts).atZone(WindowCalculator.zone())
        return window.contains(zoned)
    }

    private fun isWindowDoneToday(state: SnusState, slot: WindowSlot, now: ZonedDateTime): Boolean {
        val ts = when (slot) {
            WindowSlot.MORNING -> state.lastMorningSubmitTimestamp
            WindowSlot.EVENING -> state.lastEveningSubmitTimestamp
        }
        if (ts <= 0L) return false
        val zoned = Instant.ofEpochMilli(ts).atZone(WindowCalculator.zone())
        return zoned.toLocalDate() == now.toLocalDate()
    }

    private fun computeProgress(
        status: SnusStatus,
        now: ZonedDateTime,
        current: WindowInfo?,
        next: WindowInfo
    ): Float {
        // Вычитание делаем через Long, чтобы избежать потери точности Float (~131 072 мс на
        // значениях ~1.746×10¹²). В Float конвертируем только маленькие разности.
        val nowMs = now.toInstant().toEpochMilli()
        return when (status) {
            SnusStatus.CanSubmit -> {
                val openMs = current!!.opensAt.toInstant().toEpochMilli()
                val closeMs = current.closesAt.toInstant().toEpochMilli()
                val total = closeMs - openMs
                if (total <= 0L) 0f
                else ((nowMs - openMs).toFloat() / total.toFloat()).coerceIn(0f, 1f)
            }
            SnusStatus.AlreadyUsed -> {
                if (current != null) {
                    val anchorMs = current.opensAt.toInstant().toEpochMilli()
                    val endMs = next.opensAt.toInstant().toEpochMilli()
                    val total = maxOf(endMs - anchorMs, 1L)
                    val passed = (nowMs - anchorMs).coerceIn(0L, total)
                    (passed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                } else {
                    progressTowardNextWindow(nowMs, next)
                }
            }
            else -> progressTowardNextWindow(nowMs, next)
        }
    }

    private fun opensAtBeforeNext(next: WindowInfo): ZonedDateTime {
        val zone = WindowCalculator.zone()
        val nextDay = next.opensAt.toLocalDate()
        return when (next.slot) {
            WindowSlot.EVENING ->
                java.time.LocalDateTime.of(nextDay, WindowSlot.MORNING.openTime()).atZone(zone)
            WindowSlot.MORNING -> {
                val prevDay = nextDay.minusDays(1)
                java.time.LocalDateTime.of(prevDay, WindowSlot.EVENING.openTime()).atZone(zone)
            }
        }
    }

    private fun progressTowardNextWindow(nowMs: Long, next: WindowInfo): Float {
        val anchor = opensAtBeforeNext(next)
        val endMs = next.opensAt.toInstant().toEpochMilli()
        val startMs = anchor.toInstant().toEpochMilli()
        val total = maxOf(endMs - startMs, 1L)
        val passed = (nowMs - startMs).coerceIn(0L, total)
        return (passed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    }

    private fun formatHms(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return "%02d:%02d:%02d".format(h, m, s)
    }

    private inline fun <T> MutableStateFlow<T>.update(transform: (T) -> T) {
        value = transform(value)
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SnusViewModel(SnusRepository(context.applicationContext)) as T
        }
    }
}
