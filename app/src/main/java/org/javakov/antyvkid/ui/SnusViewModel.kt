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
                if (tick % 32 == 0) {
                    refreshPersistedAndPush()
                } else {
                    pushFromClockOnly()
                }
                delay(32L)
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
            if (alreadyUsedInWindow(persisted.lastSubmitTimestamp, window)) return@launch
            repo.recordSubmit(nowMillis)
            _state.update { it.copy(justSubmitted = true) }
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
            current != null && alreadyUsedInWindow(persisted.lastSubmitTimestamp, current) ->
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

    private fun alreadyUsedInWindow(lastSubmitMillis: Long, window: WindowInfo): Boolean {
        if (lastSubmitMillis <= 0L) return false
        val zoned = Instant.ofEpochMilli(lastSubmitMillis).atZone(WindowCalculator.zone())
        return window.contains(zoned)
    }

    private fun isWindowDoneToday(state: SnusState, slot: WindowSlot, now: ZonedDateTime): Boolean {
        if (state.lastSubmitTimestamp <= 0L) return false
        val zoned = Instant.ofEpochMilli(state.lastSubmitTimestamp).atZone(WindowCalculator.zone())
        if (zoned.toLocalDate() != now.toLocalDate()) return false
        return zoned.hour == slot.openHour
    }

    private fun computeProgress(
        status: SnusStatus,
        now: ZonedDateTime,
        current: WindowInfo?,
        next: WindowInfo
    ): Float {
        val nowMs = now.toInstant().toEpochMilli().toFloat()
        return when (status) {
            SnusStatus.CanSubmit -> {
                val openMs = current!!.opensAt.toInstant().toEpochMilli().toFloat()
                val closeMs = current.closesAt.toInstant().toEpochMilli().toFloat()
                val total = closeMs - openMs
                // Как в «Ожидании»: дуга нарастает по часу от открытия к закрытию (одинаковое направление отметки -90°).
                if (total <= 0f) 0f
                else ((nowMs - openMs) / total).coerceIn(0f, 1f)
            }
            SnusStatus.AlreadyUsed -> {
                // Раньше якорь брался как «конец другого окна» и оказывался в будущем относительно now
                // внутри текущего часа — passed обнулялся, дуга не двигалась.
                if (current != null) {
                    val anchorMs = current.opensAt.toInstant().toEpochMilli().toFloat()
                    val endMs = next.opensAt.toInstant().toEpochMilli().toFloat()
                    val total = (endMs - anchorMs).coerceAtLeast(1f)
                    val passed = (nowMs - anchorMs).coerceIn(0f, total)
                    (passed / total).coerceIn(0f, 1f)
                } else {
                    progressTowardNextWindow(nowMs, next)
                }
            }
            else -> progressTowardNextWindow(nowMs, next)
        }
    }

    /**
     * Начало предыдущего в цикле слота относительно [next.opensAt] (не конец часа).
     * Совпадает с якорем «Следующее через» ([current.opensAt]), чтобы при смене на «Ожидание» дуга не сбрасывалась.
     */
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

    /** Прогресс от открытия предыдущего слота до открытия next (ожидание, блокировка, запасной AlreadyUsed). */
    private fun progressTowardNextWindow(nowMs: Float, next: WindowInfo): Float {
        val anchor = opensAtBeforeNext(next)
        val endMs = next.opensAt.toInstant().toEpochMilli().toFloat()
        val startMs = anchor.toInstant().toEpochMilli().toFloat()
        val total = (endMs - startMs).coerceAtLeast(1f)
        val passed = (nowMs - startMs).coerceIn(0f, total)
        return (passed / total).coerceIn(0f, 1f)
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
