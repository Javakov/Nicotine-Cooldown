package org.javakov.antyvkid.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.javakov.antyvkid.domain.WindowSlot
import org.javakov.antyvkid.ui.theme.AmberWarm
import org.javakov.antyvkid.ui.theme.CoralGlow
import org.javakov.antyvkid.ui.theme.CrimsonAlert
import org.javakov.antyvkid.ui.theme.CyanGlow
import org.javakov.antyvkid.ui.theme.GlassStroke
import org.javakov.antyvkid.ui.theme.GlassStrokeSoft
import org.javakov.antyvkid.ui.theme.IndigoGlow
import org.javakov.antyvkid.ui.theme.InkMuted
import org.javakov.antyvkid.ui.theme.InkPrimary
import org.javakov.antyvkid.ui.theme.InkSecondary
import org.javakov.antyvkid.ui.theme.MidnightCore
import org.javakov.antyvkid.ui.theme.MidnightDeep
import org.javakov.antyvkid.ui.theme.MidnightSoft
import org.javakov.antyvkid.ui.theme.MidnightVeil
import org.javakov.antyvkid.ui.theme.MintCore
import org.javakov.antyvkid.ui.theme.VioletGlow

@Composable
fun SnusScreen(state: SnusUiState, onSubmit: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MidnightDeep
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            BackgroundAura(state.status)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(20.dp))
                TopBar(timerStatus = state.status)
                Spacer(Modifier.height(28.dp))
                Spacer(Modifier.weight(0.4f))
                TimerRing(
                    progress = state.progress,
                    timeLabel = state.countdownLabel,
                    caption = captionFor(state),
                    status = state.status,
                    pulse = state.justSubmitted,
                    canSubmit = state.status == SnusStatus.CanSubmit,
                    onSubmit = onSubmit,
                )
                Spacer(Modifier.weight(0.4f))
                WindowPills(
                    timerStatus = state.status,
                    currentWindow = state.currentWindow,
                    morningDone = state.morningDone,
                    eveningDone = state.eveningDone
                )
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun TopBar(timerStatus: SnusStatus) {
    var missionOpen by remember { mutableStateOf(false) }

    fun dismissMissionInfo() {
        missionOpen = false
    }
    fun openMissionInfo() {
        missionOpen = true
    }

    if (missionOpen) {
        MissionInfoDialog(
            onDismiss = ::dismissMissionInfo,
            timerStatus = timerStatus
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "NICOTINE",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 4.sp),
                color = InkMuted,
                maxLines = 1
            )
            Text(
                text = "COOLDOWN",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 4.sp),
                color = InkMuted,
                maxLines = 1
            )
        }
        Spacer(Modifier.width(12.dp))
        MissionInfoChip(accent = statusAccent(timerStatus), onClick = ::openMissionInfo)
    }
}

@Composable
private fun MissionInfoChip(accent: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(MidnightVeil, MidnightSoft),
                )
            )
            .border(1.dp, accent.copy(alpha = 0.28f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        InvertedExclamationGlyph(color = InkMuted)
    }
}

/** «¡» — точка и штрих с явным зазором, чтобы не сливались в «палку». */
@Composable
private fun InvertedExclamationGlyph(
    modifier: Modifier = Modifier,
    color: Color = InkMuted,
) {
    Canvas(modifier = modifier.size(width = 16.dp, height = 22.dp)) {
        val cx = size.width / 2f
        val dotR = 2.5.dp.toPx()
        val gap = 3.5.dp.toPx()
        val stemW = 2.2.dp.toPx()
        val padTop = 2.dp.toPx()
        val dotCy = padTop + dotR
        drawCircle(
            color = color,
            radius = dotR,
            center = Offset(cx, dotCy),
        )
        val stemTop = dotCy + dotR + gap
        val stemBottom = size.height - 2.5.dp.toPx()
        drawLine(
            color = color,
            start = Offset(cx, stemTop),
            end = Offset(cx, stemBottom),
            strokeWidth = stemW,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun MissionInfoDialog(onDismiss: () -> Unit, timerStatus: SnusStatus) {
    val accentColor = statusAccent(timerStatus)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Зачем это приложение",
                style = MaterialTheme.typography.titleLarge,
                color = InkPrimary
            )
        },
        text = {
            Text(
                text = buildString {
                    append("Идея простая: меньше никотина в день и шаг к тому, чтобы бросить.\n\n")
                    append("Одно правило для себя: дозернулся только тогда, когда нажал на таймер. ")
                    append("Без этой честной связи приложение по смыслу бесполезно.")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = InkSecondary,
                textAlign = TextAlign.Start
            )
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = accentColor)
            ) {
                Text("Понятно")
            }
        },
        containerColor = MidnightCore,
        titleContentColor = InkPrimary,
        textContentColor = InkSecondary
    )
}

@Composable
private fun TimerRing(
    progress: Float,
    timeLabel: String,
    caption: String,
    status: SnusStatus,
    pulse: Boolean,
    canSubmit: Boolean,
    onSubmit: () -> Unit,
) {
    val tapInteraction = remember { MutableInteractionSource() }
    val pressed by tapInteraction.collectIsPressedAsState()
    val tapScale by animateFloatAsState(
        targetValue = if (pressed && canSubmit) 0.96f else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMedium,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "ring-tap-scale"
    )
    val accent = statusAccent(status)
    val submitRipple = ripple(
        bounded = true,
        color = accent.copy(alpha = 0.38f),
    )
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1100, easing = LinearEasing),
        label = "ring-progress"
    )
    val scale by animateFloatAsState(
        targetValue = if (pulse) 1.04f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "ring-pulse"
    )
    val haloPulse by rememberInfiniteTransition(label = "halo").animateFloat(
        initialValue = 0.72f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(2800, easing = LinearEasing), RepeatMode.Reverse),
        label = "halo-amp"
    )
    val haloAngle by rememberInfiniteTransition(label = "halo-drift").animateFloat(
        initialValue = 0f,
        targetValue = (PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
        label = "halo-orbit"
    )

    val maxSize = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.width.toDp()
    } - 48.dp
    val ringSize = if (maxSize > 320.dp) 320.dp else maxSize

    Box(
        modifier = Modifier
            .size(ringSize)
            .scale(scale * tapScale)
            .clip(CircleShape)
            .then(
                if (canSubmit) {
                    Modifier.clickable(
                        interactionSource = tapInteraction,
                        indication = submitRipple,
                        onClick = onSubmit,
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val outerRadius = size.minDimension / 2f
            val stroke = 14.dp.toPx()
            val arcRadius = outerRadius - 16.dp.toPx() - stroke / 2f
            val discRadius = arcRadius - stroke / 2f - 1.dp.toPx()

            val driftPx = 9.dp.toPx()
            val haloCx = center.x + driftPx * 0.45f * cos(haloAngle.toDouble()).toFloat()
            val haloCy = center.y + driftPx * 0.32f * sin((haloAngle * 1.15f).toDouble()).toFloat()
            val haloBreathe = sin((haloAngle * 2.1f).toDouble()).toFloat() * 0.5f + 0.5f
            val haloRadius = outerRadius * (0.93f + 0.11f * haloBreathe)

            // Soft halo — лёгкий пульс яркости + медленный сдвиг центра (песочное «дыхание»)
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0f to accent.copy(alpha = 0.46f * haloPulse),
                        0.48f to accent.copy(alpha = 0.2f * haloPulse),
                        0.82f to accent.copy(alpha = 0.05f),
                        1f to Color.Transparent
                    ),
                    center = Offset(haloCx, haloCy),
                    radius = haloRadius
                ),
                radius = haloRadius,
                center = Offset(haloCx, haloCy)
            )

            // Disc fill
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(MidnightSoft, MidnightCore),
                    center = center,
                    radius = discRadius
                ),
                radius = discRadius,
                center = center
            )

            // Disc inner stroke
            drawCircle(
                color = GlassStroke,
                radius = discRadius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Track
            val arcSize = Size(arcRadius * 2, arcRadius * 2)
            val arcTopLeft = Offset(center.x - arcRadius, center.y - arcRadius)
            drawArc(
                color = MidnightVeil,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Progress: StrokeCap.Butt даёт ровный старт ровно в -90° (12 часов),
            // без «выпирания» влево. Круглый колпачок рисуем вручную только на конце дуги.
            if (animatedProgress > 0f) {
                drawArc(
                    color = accent,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Butt)
                )
                val endAngleRad = Math.toRadians((-90.0 + 360.0 * animatedProgress))
                drawCircle(
                    color = accent,
                    radius = stroke / 2f,
                    center = Offset(
                        center.x + arcRadius * cos(endAngleRad).toFloat(),
                        center.y + arcRadius * sin(endAngleRad).toFloat()
                    )
                )
            }

            // Tick marks inside the disc
            val tickStroke = 2.dp.toPx()
            val tickLen = 5.dp.toPx()
            val tickOuter = discRadius - 8.dp.toPx()
            val tickInner = tickOuter - tickLen
            for (i in 0 until 24) {
                val angle = Math.toRadians((i * 15f - 90f).toDouble())
                val c = cos(angle).toFloat()
                val s = sin(angle).toFloat()
                drawLine(
                    color = if (i % 6 == 0) GlassStroke else GlassStrokeSoft,
                    start = Offset(center.x + c * tickInner, center.y + s * tickInner),
                    end = Offset(center.x + c * tickOuter, center.y + s * tickOuter),
                    strokeWidth = tickStroke,
                    cap = StrokeCap.Round
                )
            }
        }

        // Center labels
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = timerCaptionTop(status),
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 4.sp),
                color = InkMuted
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = timeLabel,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1.6).sp
                ),
                color = InkPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = caption,
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 2.5.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = InkSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun WindowPills(
    timerStatus: SnusStatus,
    currentWindow: WindowSlot?,
    morningDone: Boolean,
    eveningDone: Boolean
) {
    val accent = statusAccent(timerStatus)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        WindowPill(
            modifier = Modifier.weight(1f),
            title = "Утро",
            time = "09:00",
            active = currentWindow == WindowSlot.MORNING,
            done = morningDone,
            accent = accent
        )
        WindowPill(
            modifier = Modifier.weight(1f),
            title = "Вечер",
            time = "21:00",
            active = currentWindow == WindowSlot.EVENING,
            done = eveningDone,
            accent = accent
        )
    }
}

@Composable
private fun WindowPill(
    modifier: Modifier = Modifier,
    title: String,
    time: String,
    active: Boolean,
    done: Boolean,
    accent: Color
) {
    val inactiveBorder = accent.copy(alpha = 0.26f)
    val borderColor by animateColorAsState(
        if (active) accent else inactiveBorder,
        tween(450),
        label = "pill-border"
    )
    val bgColor by animateColorAsState(
        if (active) accent.copy(alpha = 0.10f) else MidnightCore.copy(alpha = 0.6f),
        tween(450),
        label = "pill-bg"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(22.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = if (done)
                                listOf(accent.copy(alpha = 0.7f), accent.copy(alpha = 0.45f))
                            else
                                listOf(MidnightVeil, MidnightSoft)
                        )
                    )
                    .border(
                        1.dp,
                        if (done) accent else accent.copy(alpha = 0.28f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (done) "✓" else "·",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (done) MidnightDeep else InkSecondary,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                    color = InkMuted
                )
                Text(
                    text = time,
                    style = MaterialTheme.typography.titleLarge,
                    color = InkPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BackgroundAura(status: SnusStatus) {
    val transition = rememberInfiniteTransition(label = "aurora")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse),
        label = "aurora-phase"
    )
    val breathe by transition.animateFloat(
        initialValue = 0.52f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "aurora-breathe"
    )
    val orbit by transition.animateFloat(
        initialValue = 0f,
        targetValue = (PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(tween(16000, easing = LinearEasing), RepeatMode.Restart),
        label = "aurora-orbit"
    )
    val accent = statusAccent(status)
    val secondary = when (status) {
        SnusStatus.CanSubmit -> CyanGlow
        SnusStatus.AlreadyUsed -> IndigoGlow
        SnusStatus.Waiting -> VioletGlow
        SnusStatus.Blocked -> CoralGlow
    }
    val waiting = status == SnusStatus.Waiting
    val accentAlphaBase = if (waiting) 0.32f else 0.28f
    val accentAlphaSwing = if (waiting) 0.2f else 0.14f
    val radiusSwing = if (waiting) 0.22f else 0.16f
    val driftAmpX = if (waiting) 0.12f else 0.08f
    val driftAmpY = if (waiting) 0.09f else 0.06f

    Box(modifier = Modifier.fillMaxSize()) {
        // Главный тёплый «песок» за таймером — орбита + пульс яркости/радиуса (раньше почти не было видно из‑за blur)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(110.dp)
                .drawBehind {
                    val driftX = driftAmpX * sin(orbit.toDouble()).toFloat()
                    val driftY = driftAmpY * cos(orbit.toDouble() * 0.84).toFloat()
                    val cx = size.width * (0.4f + 0.08f * phase + driftX)
                    val cy = size.height * (0.34f + 0.06f * (1f - phase) + driftY)
                    val rad = size.minDimension * (0.56f + radiusSwing * breathe)
                    val alpha = (accentAlphaBase + accentAlphaSwing * breathe).coerceIn(0.04f, 0.58f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(accent.copy(alpha = alpha), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = rad
                        ),
                        radius = rad,
                        center = Offset(cx, cy)
                    )
                }
        )

        // Второй слой — мягкий контраст, тоже дышит
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(130.dp)
                .drawBehind {
                    val ox = 0.05f * sin((orbit * 0.65f).toDouble()).toFloat()
                    val oy = 0.04f * cos((orbit * 0.5f).toDouble()).toFloat()
                    val cx = size.width * (0.82f - 0.1f * phase + ox)
                    val cy = size.height * (0.74f + 0.06f * phase + oy)
                    val rad = size.minDimension * (0.52f + 0.12f * (1f - breathe * 0.35f))
                    val alpha = (0.22f + 0.12f * breathe) * if (waiting) 1.1f else 1f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(secondary.copy(alpha = alpha.coerceIn(0.08f, 0.42f)), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = rad
                        ),
                        radius = rad,
                        center = Offset(cx, cy)
                    )
                }
        )

        // Subtle vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MidnightDeep.copy(alpha = 0.35f),
                            MidnightDeep.copy(alpha = 0.85f)
                        )
                    )
                )
        )
    }
}

private fun statusAccent(status: SnusStatus): Color = when (status) {
    SnusStatus.CanSubmit -> MintCore
    SnusStatus.AlreadyUsed -> CyanGlow
    SnusStatus.Waiting -> AmberWarm
    SnusStatus.Blocked -> CrimsonAlert
}

private fun captionFor(state: SnusUiState): String = when (state.status) {
    SnusStatus.CanSubmit -> "до закрытия окна"
    SnusStatus.AlreadyUsed -> "до окна ${state.nextWindowAtLabel}"
    SnusStatus.Waiting -> "до окна ${state.nextWindowAtLabel}"
    SnusStatus.Blocked -> "проверьте системное\nвремя"
}

private fun timerCaptionTop(status: SnusStatus): String = when (status) {
    SnusStatus.CanSubmit -> "ОКНО ОТКРЫТО"
    SnusStatus.AlreadyUsed -> "СЛЕДУЮЩЕЕ ЧЕРЕЗ"
    SnusStatus.Waiting -> "ОЖИДАНИЕ"
    SnusStatus.Blocked -> "БЛОКИРОВКА"
}

