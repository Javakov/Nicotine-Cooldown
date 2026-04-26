package org.javakov.antyvkid.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures
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
import org.javakov.antyvkid.ui.theme.MintGlow
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
                TopBar()
                Spacer(Modifier.height(28.dp))
                StatusBadge(state.status)
                Spacer(Modifier.weight(0.4f))
                TimerRing(
                    progress = state.progress,
                    timeLabel = state.countdownLabel,
                    caption = captionFor(state),
                    status = state.status,
                    pulse = state.justSubmitted
                )
                Spacer(Modifier.weight(0.4f))
                WindowPills(
                    currentWindow = state.currentWindow,
                    morningDone = state.morningDone,
                    eveningDone = state.eveningDone
                )
                Spacer(Modifier.height(24.dp))
                SubmitButton(
                    enabled = state.status == SnusStatus.CanSubmit,
                    label = buttonLabel(state.status),
                    onClick = onSubmit
                )
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun TopBar() {
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
        ScheduleChip()
    }
}

@Composable
private fun ScheduleChip() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(MidnightVeil.copy(alpha = 0.85f), MidnightSoft.copy(alpha = 0.85f))
                )
            )
            .border(1.dp, GlassStrokeSoft, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MintGlow)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "09:00 · 21:00",
                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
                color = InkPrimary,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun StatusBadge(status: SnusStatus) {
    val targetColor = statusAccent(status)
    val color by animateColorAsState(targetColor, tween(450), label = "badge")
    val text = when (status) {
        SnusStatus.CanSubmit -> "Можно принять"
        SnusStatus.AlreadyUsed -> "Уже принято в этом окне"
        SnusStatus.Waiting -> "Ожидание окна"
        SnusStatus.Blocked -> "Время сбито · блокировка"
    }
    val pulseTransition = rememberInfiniteTransition(label = "badge-pulse")
    val pulse by pulseTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse-alpha"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(50))
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = pulse))
            )
            Spacer(Modifier.width(10.dp))
            AnimatedContent(
                targetState = text,
                transitionSpec = {
                    (fadeIn(tween(300)) + slideInVertically { it / 3 })
                        .togetherWith(fadeOut(tween(180)) + slideOutVertically { -it / 3 })
                },
                label = "badge-text"
            ) { value ->
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge,
                    color = InkPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun TimerRing(
    progress: Float,
    timeLabel: String,
    caption: String,
    status: SnusStatus,
    pulse: Boolean
) {
    val accent = statusAccent(status)
    // Прогресс приходит ~30 раз/с из ViewModel — snap без «догоняющего» tween, дуга визуально живёт.
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = snap(),
        label = "ring-progress"
    )
    val scale by animateFloatAsState(
        targetValue = if (pulse) 1.04f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "ring-pulse"
    )
    val haloPulse by rememberInfiniteTransition(label = "halo").animateFloat(
        initialValue = 0.85f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Reverse),
        label = "halo-amp"
    )

    val maxSize = LocalConfiguration.current.screenWidthDp.dp - 48.dp
    val ringSize = if (maxSize > 320.dp) 320.dp else maxSize

    Box(
        modifier = Modifier
            .size(ringSize)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val outerRadius = size.minDimension / 2f
            val stroke = 14.dp.toPx()
            val arcRadius = outerRadius - 16.dp.toPx() - stroke / 2f
            val discRadius = arcRadius - stroke / 2f - 1.dp.toPx()

            // Soft halo — fades to transparent at the corners so no square edge
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0f to accent.copy(alpha = 0.42f * haloPulse),
                        0.55f to accent.copy(alpha = 0.18f * haloPulse),
                        0.85f to accent.copy(alpha = 0.04f),
                        1f to Color.Transparent
                    ),
                    center = center,
                    radius = outerRadius
                ),
                radius = outerRadius,
                center = center
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

            // Progress — solid accent, no rotation, clean ring
            if (animatedProgress > 0f) {
                drawArc(
                    color = accent,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }

            // Tick marks inside the disc
            val tickStroke = 2.dp.toPx()
            val tickLen = 5.dp.toPx()
            val tickOuter = discRadius - 8.dp.toPx()
            val tickInner = tickOuter - tickLen
            for (i in 0 until 24) {
                val angle = Math.toRadians((i * 15f - 90f).toDouble())
                val c = kotlin.math.cos(angle).toFloat()
                val s = kotlin.math.sin(angle).toFloat()
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
                style = MaterialTheme.typography.bodyMedium,
                color = InkSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun WindowPills(
    currentWindow: WindowSlot?,
    morningDone: Boolean,
    eveningDone: Boolean
) {
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
            accent = MintCore
        )
        WindowPill(
            modifier = Modifier.weight(1f),
            title = "Вечер",
            time = "21:00",
            active = currentWindow == WindowSlot.EVENING,
            done = eveningDone,
            accent = VioletGlow
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
    val borderColor by animateColorAsState(
        if (active) accent else GlassStrokeSoft, tween(450), label = "pill-border"
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
                        if (done) accent else GlassStrokeSoft,
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
private fun SubmitButton(
    enabled: Boolean,
    label: String,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "btn-scale"
    )
    val gradientStart by animateColorAsState(
        if (enabled) MintCore else MidnightSoft,
        tween(450),
        label = "btn-grad-start"
    )
    val gradientEnd by animateColorAsState(
        if (enabled) MintGlow else MidnightCore,
        tween(450),
        label = "btn-grad-end"
    )
    val textColor by animateColorAsState(
        if (enabled) MidnightDeep else InkMuted,
        tween(450),
        label = "btn-text"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .scale(scale)
            .clip(RoundedCornerShape(26.dp))
            .drawBehind {
                if (enabled) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(MintGlow.copy(alpha = 0.45f), Color.Transparent),
                            radius = size.maxDimension * 0.7f,
                            center = Offset(size.width / 2f, size.height + 30f)
                        )
                    )
                }
            }
            .background(
                brush = Brush.linearGradient(listOf(gradientStart, gradientEnd))
            )
            .border(1.dp, if (enabled) MintCore.copy(alpha = 0.6f) else GlassStrokeSoft, RoundedCornerShape(26.dp))
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures(onTap = { onClick() })
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(textColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text("◉", color = textColor, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge.copy(letterSpacing = 2.sp),
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BackgroundAura(status: SnusStatus) {
    val transition = rememberInfiniteTransition(label = "aurora")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing), RepeatMode.Reverse),
        label = "aurora-phase"
    )
    val accent = statusAccent(status)
    val secondary = when (status) {
        SnusStatus.CanSubmit -> CyanGlow
        SnusStatus.AlreadyUsed -> IndigoGlow
        SnusStatus.Waiting -> VioletGlow
        SnusStatus.Blocked -> CoralGlow
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Top blob
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(120.dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(accent.copy(alpha = 0.35f), Color.Transparent),
                            center = Offset(size.width * (0.3f + 0.1f * phase), size.height * 0.4f),
                            radius = size.minDimension * 0.7f
                        )
                    )
                }
        )

        // Bottom-right blob
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(140.dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(secondary.copy(alpha = 0.32f), Color.Transparent),
                            center = Offset(size.width * (0.85f - 0.08f * phase), size.height * (0.78f + 0.05f * phase)),
                            radius = size.minDimension * 0.6f
                        )
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
    SnusStatus.Blocked -> "проверьте системное время"
}

private fun timerCaptionTop(status: SnusStatus): String = when (status) {
    SnusStatus.CanSubmit -> "ОКНО ОТКРЫТО"
    SnusStatus.AlreadyUsed -> "СЛЕДУЮЩЕЕ ЧЕРЕЗ"
    SnusStatus.Waiting -> "ОЖИДАНИЕ"
    SnusStatus.Blocked -> "БЛОКИРОВКА"
}

private fun buttonLabel(status: SnusStatus): String = when (status) {
    SnusStatus.CanSubmit -> "ПРИНЯТЬ"
    SnusStatus.AlreadyUsed -> "УЖЕ ПРИНЯТО"
    SnusStatus.Waiting -> "ОЖИДАНИЕ ОКНА"
    SnusStatus.Blocked -> "БЛОКИРОВКА"
}
