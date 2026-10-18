package com.phonedex.app.ui.achievements

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.phonedex.app.domain.AchievementDef
import kotlin.random.Random

@Composable
fun UnlockDialog(unlocked: List<AchievementDef>, onDismiss: () -> Unit, onViewAll: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = cs.surfaceContainerHigh)
        ) {
            Box {
                Confetti(Modifier.matchParentSize())
                Column(
                    Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    var shown by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { shown = true }
                    val scale by animateFloatAsState(
                        if (shown) 1f else 0f,
                        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                        label = "trophy"
                    )
                    Box(
                        Modifier.size(88.dp).scale(scale).clip(CircleShape).background(cs.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (unlocked.size == 1) unlocked[0].icon else Icons.Rounded.EmojiEvents,
                            null, Modifier.size(44.dp), tint = cs.onPrimary
                        )
                    }
                    Text(
                        if (unlocked.size == 1) "Achievement unlocked!" else "${unlocked.size} achievements unlocked!",
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
                    )
                    unlocked.take(4).forEach {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(it.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = cs.primary)
                            Text(it.description, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, textAlign = TextAlign.Center)
                        }
                    }
                    if (unlocked.size > 4) Text("+ ${unlocked.size - 4} more", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onDismiss) { Text("Nice!") }
                        Button(onClick = onViewAll) { Text("View all") }
                    }
                }
            }
        }
    }
}

private class Particle(val x0: Float, val vx: Float, val vy: Float, val r: Float, val colorIdx: Int)

@Composable
private fun Confetti(modifier: Modifier) {
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) { t.animateTo(1f, tween(2000, easing = LinearOutSlowInEasing)) }
    val cs = MaterialTheme.colorScheme
    val palette = listOf(cs.primary, cs.tertiary, cs.secondary, Color(0xFFFFC857), Color(0xFFEF6F6C))
    val particles = remember {
        List(40) {
            Particle(
                x0 = Random.nextFloat(), vx = Random.nextFloat() * 0.3f - 0.15f,
                vy = Random.nextFloat() * 0.25f, r = 2f + Random.nextFloat() * 3f,
                colorIdx = Random.nextInt(5)
            )
        }
    }
    Canvas(modifier) {
        val p = t.value
        particles.forEach {
            drawCircle(
                color = palette[it.colorIdx].copy(alpha = (1f - p).coerceIn(0f, 1f)),
                radius = it.r.dp.toPx(),
                center = Offset(size.width * (it.x0 + it.vx * p), size.height * (0.1f + it.vy * p + 0.7f * p * p))
            )
        }
    }
}
