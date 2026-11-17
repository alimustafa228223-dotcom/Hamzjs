package com.example.ui.components

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AbyssalBlack
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceHigh
import com.example.ui.theme.MagentaPulse
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividPurple
import kotlin.math.cos
import kotlin.math.sin

enum class KainatTab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    CHAT(
        route = "chat",
        labelRes = R.string.tab_chat,
        selectedIcon = Icons.Rounded.GraphicEq,
        unselectedIcon = Icons.Outlined.GraphicEq,
        testTag = "nav_tab_chat"
    ),
    DEVELOPER(
        route = "developer",
        labelRes = R.string.tab_developer,
        selectedIcon = Icons.Rounded.Code,
        unselectedIcon = Icons.Outlined.Code,
        testTag = "nav_tab_developer"
    ),
    STUDIO(
        route = "studio",
        labelRes = R.string.tab_studio,
        selectedIcon = Icons.Rounded.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome,
        testTag = "nav_tab_studio"
    )
}

@Composable
fun LiquidCosmicBackdrop(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "cosmic_bg")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cosmic_phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlack)
    ) {
        val w = size.width
        val h = size.height

        // Top-left drifting cyan/blue liquid aura
        val cx1 = w * (0.22f + 0.08f * cos(phase))
        val cy1 = h * (0.16f + 0.05f * sin(phase))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    NeonCyan.copy(alpha = 0.14f),
                    ElectricBlue.copy(alpha = 0.06f),
                    Color.Transparent
                ),
                center = Offset(cx1, cy1),
                radius = w * 0.72f
            ),
            radius = w * 0.72f,
            center = Offset(cx1, cy1)
        )

        // Mid-right drifting purple/magenta aura
        val cx2 = w * (0.82f - 0.08f * sin(phase))
        val cy2 = h * (0.48f + 0.06f * cos(phase))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    VividPurple.copy(alpha = 0.16f),
                    MagentaPulse.copy(alpha = 0.05f),
                    Color.Transparent
                ),
                center = Offset(cx2, cy2),
                radius = w * 0.68f
            ),
            radius = w * 0.68f,
            center = Offset(cx2, cy2)
        )

        // Bottom ambient electric blue reflection behind floating dock
        val cx3 = w * (0.5f + 0.1f * sin(phase * 0.7f))
        val cy3 = h * 0.92f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    ElectricBlue.copy(alpha = 0.13f),
                    NeonCyan.copy(alpha = 0.04f),
                    Color.Transparent
                ),
                center = Offset(cx3, cy3),
                radius = w * 0.65f
            ),
            radius = w * 0.65f,
            center = Offset(cx3, cy3)
        )
    }
}

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    borderColors: List<Color> = listOf(
        NeonCyan.copy(alpha = 0.55f),
        ElectricBlue.copy(alpha = 0.35f),
        VividPurple.copy(alpha = 0.55f)
    ),
    containerColor: Color = GlassSurface,
    glowColor: Color = ElectricBlue.copy(alpha = 0.18f),
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 14.dp,
                shape = shape,
                ambientColor = glowColor,
                spotColor = glowColor
            )
            .clip(shape)
            .background(containerColor)
            .drawBehind {
                // Subtle 3D top glass refraction highlight
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.09f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.20f)
                        )
                    )
                )
            }
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(colors = borderColors),
                shape = shape
            ),
        content = content
    )
}

@Composable
fun ScrollReactiveTopBar(
    scrollProgress: Float,
    subtitle: String,
    onClearAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val clamped = scrollProgress.coerceIn(0f, 1f)
    val bgAlpha by animateFloatAsState(
        targetValue = 0.25f + clamped * 0.65f,
        animationSpec = tween(180),
        label = "topbar_alpha"
    )
    val borderAlpha by animateFloatAsState(
        targetValue = 0.16f + clamped * 0.45f,
        animationSpec = tween(180),
        label = "topbar_border"
    )
    val blurRadius by animateDpAsState(
        targetValue = (clamped * 6f).dp,
        animationSpec = tween(180),
        label = "topbar_blur"
    )

    val pulseTransition = rememberInfiniteTransition(label = "online_pulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Low backdrop blur glass layer (safe for API 31+, clean translucent fallback below)
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(22.dp))
                .then(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius > 0.dp) {
                        Modifier.blur(blurRadius)
                    } else {
                        Modifier
                    }
                )
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            AbyssalBlack.copy(alpha = bgAlpha),
                            GlassSurfaceHigh.copy(alpha = bgAlpha)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = borderAlpha),
                            ElectricBlue.copy(alpha = borderAlpha * 0.8f),
                            VividPurple.copy(alpha = borderAlpha)
                        )
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(EmeraldGlow.copy(alpha = pulseAlpha))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.app_name_urdu),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (onClearAction != null) {
                IconButton(
                    onClick = onClearAction,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("top_bar_clear_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteSweep,
                        contentDescription = "Reset current session",
                        tint = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun FloatingGlassBottomNav(
    selectedTab: KainatTab,
    onSelectTab: (KainatTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassCard(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            containerColor = Color(0xCC0A0D1A),
            borderColors = listOf(
                NeonCyan.copy(alpha = 0.65f),
                ElectricBlue.copy(alpha = 0.55f),
                VividPurple.copy(alpha = 0.65f)
            ),
            glowColor = NeonCyan.copy(alpha = 0.25f),
            borderWidth = 1.25.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                KainatTab.entries.forEach { tab ->
                    val isSelected = tab == selectedTab
                    val labelColor by animateColorAsState(
                        targetValue = if (isSelected) TextPrimary else TextMuted,
                        animationSpec = spring(),
                        label = "tab_label_color"
                    )
                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) NeonCyan else TextSecondary,
                        animationSpec = spring(),
                        label = "tab_icon_tint"
                    )
                    val interactionSource = remember { MutableInteractionSource() }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(24.dp))
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    NeonCyan.copy(alpha = 0.18f),
                                                    ElectricBlue.copy(alpha = 0.20f),
                                                    VividPurple.copy(alpha = 0.22f)
                                                )
                                            )
                                        )
                                        .border(
                                            width = 0.8.dp,
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    NeonCyan.copy(alpha = 0.65f),
                                                    VividPurple.copy(alpha = 0.65f)
                                                )
                                            ),
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                } else {
                                    Modifier
                                }
                            )
                            .clickable(
                                interactionSource = interactionSource,
                                indication = ripple(color = NeonCyan),
                                onClick = { onSelectTab(tab) }
                            )
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                            .testTag(tab.testTag),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = stringResource(tab.labelRes),
                                tint = iconTint,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = stringResource(tab.labelRes),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = labelColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
