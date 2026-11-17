package com.example.ui.chat

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.AudioVisualizerOrb
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.OrbState
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceHigh
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividPurple

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    topPadding: Dp,
    bottomNavHeight: Dp,
    onScrollProgressChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val listState: LazyListState = rememberLazyListState()

    val scrollProgress by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 1f
            else (listState.firstVisibleItemScrollOffset / 180f).coerceIn(0f, 1f)
        }
    }
    LaunchedEffect(scrollProgress) {
        onScrollProgressChanged(scrollProgress)
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.size > 1) {
            listState.animateScrollToItem(uiState.messages.lastIndex + 1)
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startListening()
        } else {
            viewModel.updateInputText(uiState.inputText)
        }
    }

    val triggerVoiceInput: () -> Unit = {
        val hasPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPerm) {
            viewModel.startListening()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
                .align(Alignment.Center)
                .padding(top = topPadding, bottom = bottomNavHeight)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "orb_header") {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColors = listOf(
                            NeonCyan.copy(alpha = 0.55f),
                            ElectricBlue.copy(alpha = 0.4f),
                            VividPurple.copy(alpha = 0.55f)
                        ),
                        containerColor = Color(0x990A0D18)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "VOICE CORE • کائنات",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonCyan
                                )

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            if (uiState.isAutoSpeakEnabled) {
                                                NeonCyan.copy(alpha = 0.16f)
                                            } else {
                                                DeepObsidian
                                            }
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (uiState.isAutoSpeakEnabled) {
                                                NeonCyan.copy(alpha = 0.55f)
                                            } else {
                                                TextMuted.copy(alpha = 0.4f)
                                            },
                                            shape = RoundedCornerShape(50)
                                        )
                                        .minimumInteractiveComponentSize()
                                        .clickable { viewModel.toggleAutoSpeak() }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag("auto_speak_toggle"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isAutoSpeakEnabled) {
                                            Icons.AutoMirrored.Rounded.VolumeUp
                                        } else {
                                            Icons.AutoMirrored.Rounded.VolumeOff
                                        },
                                        contentDescription = "Toggle authoritative voice output",
                                        tint = if (uiState.isAutoSpeakEnabled) NeonCyan else TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (uiState.isAutoSpeakEnabled) "VOICE ON" else "MUTED",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (uiState.isAutoSpeakEnabled) NeonCyan else TextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            AudioVisualizerOrb(
                                orbState = uiState.orbState,
                                audioRmsLevel = uiState.audioRmsLevel,
                                onTapOrb = {
                                    when (uiState.orbState) {
                                        OrbState.SPEAKING -> viewModel.stopSpeaking()
                                        OrbState.LISTENING -> viewModel.stopListening()
                                        else -> triggerVoiceInput()
                                    }
                                },
                                orbSize = 142.dp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Quick directives row
                            val quickDirectives = listOf(
                                "Analyze AI trends 2026",
                                "کائنات کے بارے میں بتائیں",
                                "System security briefing"
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                quickDirectives.forEach { directive ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(GlassSurfaceHigh)
                                            .border(
                                                width = 1.dp,
                                                color = ElectricBlue.copy(alpha = 0.4f),
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .clickable { viewModel.sendMessage(directive) }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = directive,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                items(uiState.messages, key = { it.id }) { msg ->
                    ChatMessageCard(
                        message = msg,
                        isSpeaking = uiState.speakingMessageId == msg.id,
                        onSpeakClick = {
                            if (uiState.speakingMessageId == msg.id) {
                                viewModel.stopSpeaking()
                            } else {
                                viewModel.speakText(msg.text, msg.id)
                            }
                        },
                        onCopyClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Kainat AI", msg.text))
                        }
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                uiState.errorMessage?.let { errorMsg ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CrimsonError.copy(alpha = 0.14f))
                            .border(1.dp, CrimsonError.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                            .clickable { viewModel.dismissError() }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = "Error",
                            tint = CrimsonError,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMsg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Command Input Dock
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                containerColor = Color(0xCC0D1020),
                borderColors = listOf(
                    NeonCyan.copy(alpha = 0.5f),
                    VividPurple.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = triggerVoiceInput,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .background(
                                if (uiState.orbState == OrbState.LISTENING) {
                                    EmeraldGlow.copy(alpha = 0.25f)
                                } else {
                                    NeonCyan.copy(alpha = 0.12f)
                                }
                            )
                            .testTag("chat_mic_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.orbState == OrbState.LISTENING) {
                                Icons.Rounded.Stop
                            } else {
                                Icons.Rounded.Mic
                            },
                            contentDescription = "Voice input",
                            tint = if (uiState.orbState == OrbState.LISTENING) EmeraldGlow else NeonCyan
                        )
                    }

                    OutlinedTextField(
                        value = uiState.inputText,
                        onValueChange = viewModel::updateInputText,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.chat_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        },
                        singleLine = false,
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = NeonCyan
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { viewModel.sendMessage() }),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field")
                    )

                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(NeonCyan, ElectricBlue, VividPurple)
                                )
                            )
                            .clickable(
                                enabled = uiState.inputText.isNotBlank() &&
                                    uiState.orbState != OrbState.THINKING
                            ) { viewModel.sendMessage() }
                            .padding(12.dp)
                            .testTag("chat_send_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Send command",
                            tint = PitchBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageCard(
    message: ChatMessage,
    isSpeaking: Boolean,
    onSpeakClick: () -> Unit,
    onCopyClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        LiquidGlassCard(
            modifier = Modifier.widthIn(max = 520.dp),
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (message.isUser) 20.dp else 6.dp,
                bottomEnd = if (message.isUser) 6.dp else 20.dp
            ),
            containerColor = if (message.isUser) Color(0xCC111936) else GlassSurface,
            borderColors = if (message.isUser) {
                listOf(ElectricBlue.copy(alpha = 0.7f), VividPurple.copy(alpha = 0.7f))
            } else {
                listOf(NeonCyan.copy(alpha = 0.55f), ElectricBlue.copy(alpha = 0.35f))
            }
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (message.isUser) "COMMANDER" else "KAINAT AI",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (message.isUser) VividPurple else NeonCyan
                    )

                    if (!message.isUser && message.text.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onSpeakClick,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) {
                                        Icons.Rounded.Stop
                                    } else {
                                        Icons.AutoMirrored.Rounded.VolumeUp
                                    },
                                    contentDescription = if (isSpeaking) "Stop speaking" else "Read response aloud",
                                    tint = if (isSpeaking) EmeraldGlow else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = onCopyClick,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ContentCopy,
                                    contentDescription = "Copy response",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (message.text.isBlank() && message.isStreaming) {
                        "Processing neural stream…"
                    } else {
                        message.text + if (message.isStreaming) " ▍" else ""
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
            }
        }
    }
}
