package com.example.ui.studio

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.MovieCreation
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ai.GeneratedVideoPlan
import com.example.ai.StudioAspectRatio
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.AbyssalBlack
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.GlassSurfaceHigh
import com.example.ui.theme.MagentaPulse
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividPurple

@Composable
fun StudioScreen(
    viewModel: StudioViewModel,
    topPadding: Dp,
    bottomNavHeight: Dp,
    onScrollProgressChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    val scrollProgress by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 1f
            else (listState.firstVisibleItemScrollOffset / 180f).coerceIn(0f, 1f)
        }
    }
    LaunchedEffect(scrollProgress) {
        onScrollProgressChanged(scrollProgress)
    }

    var pendingExportItem by remember { mutableStateOf<GeneratedImageItem?>(null) }
    val saveImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/png")
    ) { uri: Uri? ->
        val item = pendingExportItem
        if (uri != null && item != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    item.bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                viewModel.showNotice("Saved PNG image to device storage")
            } catch (e: Exception) {
                viewModel.showNotice("Image export failed: ${e.localizedMessage ?: "I/O error"}")
            }
        }
        pendingExportItem = null
    }

    uiState.selectedImageForInspect?.let { inspected ->
        InspectImageDialog(
            item = inspected,
            onDismiss = { viewModel.inspectImage(null) },
            onExportPng = {
                pendingExportItem = inspected
                saveImageLauncher.launch("kainat_art_${inspected.id.take(6)}.png")
            }
        )
    }

    val isBusy = uiState.genState is StudioGenState.GeneratingImage ||
        uiState.genState is StudioGenState.GeneratingVideoPlan ||
        uiState.genState is StudioGenState.EnhancingPrompt

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 780.dp)
                .align(Alignment.Center)
                .padding(top = topPadding, bottom = bottomNavHeight),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hero Studio Banner
            item(key = "studio_hero_banner") {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColors = listOf(
                        NeonCyan.copy(alpha = 0.55f),
                        VividPurple.copy(alpha = 0.65f)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(126.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_nebula_banner_1794936194737),
                            contentDescription = "Kainat Creative Studio visual backdrop",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0x33000000),
                                            Color(0xCC04050C)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "MULTIMEDIA CREATIVE ENGINE",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonCyan
                            )
                            Text(
                                text = "Video Director & Image Synthesis",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // Generator Controls Card
            item(key = "studio_controls_card") {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColors = listOf(
                        NeonCyan.copy(alpha = 0.55f),
                        ElectricBlue.copy(alpha = 0.45f),
                        MagentaPulse.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Mode Switcher
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(AbyssalBlack)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            StudioModeButton(
                                label = "Image Studio",
                                icon = Icons.Rounded.Image,
                                selected = uiState.mode == StudioMode.IMAGE_SYNTHESIS,
                                onClick = { viewModel.setMode(StudioMode.IMAGE_SYNTHESIS) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("studio_mode_image")
                            )
                            StudioModeButton(
                                label = "Video Director",
                                icon = Icons.Rounded.MovieCreation,
                                selected = uiState.mode == StudioMode.VIDEO_DIRECTOR,
                                onClick = { viewModel.setMode(StudioMode.VIDEO_DIRECTOR) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("studio_mode_video")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = uiState.prompt,
                            onValueChange = viewModel::updatePrompt,
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.studio_hint),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted
                                )
                            },
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan.copy(alpha = 0.7f),
                                unfocusedBorderColor = VividPurple.copy(alpha = 0.35f),
                                focusedContainerColor = AbyssalBlack,
                                unfocusedContainerColor = AbyssalBlack,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = NeonCyan
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("studio_prompt_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Aspect ratio chips in Image mode
                        if (uiState.mode == StudioMode.IMAGE_SYNTHESIS) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "RATIO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                                StudioAspectRatio.entries.forEach { ratio ->
                                    val selected = uiState.aspectRatio == ratio
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (selected) NeonCyan.copy(alpha = 0.2f) else DeepObsidian
                                            )
                                            .border(
                                                1.dp,
                                                if (selected) NeonCyan else TextMuted.copy(alpha = 0.35f),
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable { viewModel.selectAspectRatio(ratio) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = ratio.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (selected) NeonCyan else TextSecondary
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Starter prompts
                        val samplePrompts = if (uiState.mode == StudioMode.IMAGE_SYNTHESIS) {
                            listOf(
                                "Futuristic Lahore skyline at night in neon rain",
                                "Liquid crystal spaceship orbiting Saturn",
                                "Bioluminescent cybernetic orchid macro"
                            )
                        } else {
                            listOf(
                                "Cyberpunk drone race through neon canyon",
                                "Astronaut discovering crystal monolith on Europa",
                                "Liquid glass AI core awakening in zero gravity"
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            samplePrompts.forEach { sample ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(GlassSurfaceHigh)
                                        .border(
                                            1.dp,
                                            ElectricBlue.copy(alpha = 0.4f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable(enabled = !isBusy) {
                                            viewModel.updatePrompt(sample)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        text = sample,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Enhance Prompt button
                            Box(
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(DeepObsidian)
                                    .border(
                                        1.dp,
                                        VividPurple.copy(alpha = 0.6f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable(enabled = uiState.prompt.isNotBlank() && !isBusy) {
                                        viewModel.enhancePrompt()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                                    .testTag("studio_enhance_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (uiState.genState is StudioGenState.EnhancingPrompt) {
                                        "ENHANCING…"
                                    } else {
                                        "ENHANCE"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = VividPurple
                                )
                            }

                            // Generate CTA
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(NeonCyan, ElectricBlue, VividPurple)
                                        )
                                    )
                                    .clickable(enabled = uiState.prompt.isNotBlank() && !isBusy) {
                                        viewModel.generateCurrentMode()
                                    }
                                    .padding(vertical = 12.dp)
                                    .testTag("studio_generate_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isBusy && uiState.genState !is StudioGenState.EnhancingPrompt) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = PitchBlack
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    } else {
                                        Icon(
                                            imageVector = Icons.Rounded.AutoAwesome,
                                            contentDescription = null,
                                            tint = PitchBlack,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = when (uiState.genState) {
                                            StudioGenState.GeneratingImage -> "RENDERING (30–60s)…"
                                            StudioGenState.GeneratingVideoPlan -> "DIRECTING SCENES…"
                                            else -> if (uiState.mode == StudioMode.IMAGE_SYNTHESIS) {
                                                "GENERATE IMAGE"
                                            } else {
                                                "CREATE VIDEO PLAN"
                                            }
                                        },
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = PitchBlack
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(visible = uiState.genState is StudioGenState.Error) {
                            val err = (uiState.genState as? StudioGenState.Error)?.message.orEmpty()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CrimsonError.copy(alpha = 0.14f))
                                    .border(1.dp, CrimsonError.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                                    .padding(10.dp),
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
                                    text = err,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }
                        }

                        AnimatedVisibility(visible = uiState.statusNotice != null) {
                            uiState.statusNotice?.let { notice ->
                                Text(
                                    text = notice,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldGlow,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Image Gallery Section
            if (uiState.mode == StudioMode.IMAGE_SYNTHESIS) {
                if (uiState.imageGallery.isEmpty()) {
                    item(key = "image_empty_state") {
                        LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Image,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No Rendered Artworks Yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Enter a visual prompt above or select a preset to synthesize high-resolution artwork with Gemini Nano Banana 2.1.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.imageGallery, key = { it.id }) { item ->
                        LiquidGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.inspectImage(item) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Image(
                                    bitmap = item.bitmap.asImageBitmap(),
                                    contentDescription = item.prompt,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(
                                            when (item.aspectRatio) {
                                                StudioAspectRatio.SQUARE -> 1f
                                                StudioAspectRatio.LANDSCAPE -> 16f / 9f
                                                StudioAspectRatio.PORTRAIT -> 9f / 16f
                                                StudioAspectRatio.CINEMA -> 4f / 3f
                                            }
                                        )
                                        .clip(RoundedCornerShape(16.dp))
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.prompt,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary,
                                        maxLines = 2,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            pendingExportItem = item
                                            saveImageLauncher.launch(
                                                "kainat_art_${item.id.take(6)}.png"
                                            )
                                        },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Download,
                                            contentDescription = "Save image as PNG",
                                            tint = NeonCyan
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Video Storyboards Section
                if (uiState.videoPlans.isEmpty()) {
                    item(key = "video_empty_state") {
                        LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Videocam,
                                    contentDescription = null,
                                    tint = VividPurple,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No Video Storyboards Yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Describe a scene to generate a 4-shot cinematic video blueprint, master prompt, and 1-tap keyframe renders.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.videoPlans, key = { it.title + it.masterVideoPrompt.hashCode() }) { plan ->
                        VideoPlanCard(
                            plan = plan,
                            activeSceneIndex = uiState.activeVideoSceneIndex,
                            onSelectScene = viewModel::selectSceneIndex,
                            onCopyMasterPrompt = {
                                val clipboard = context.getSystemService(
                                    Context.CLIPBOARD_SERVICE
                                ) as ClipboardManager
                                clipboard.setPrimaryClip(
                                    ClipData.newPlainText("Master Video Prompt", plan.masterVideoPrompt)
                                )
                                viewModel.showNotice("Master video prompt copied")
                            },
                            onVisualizeKeyframe = { scenePrompt ->
                                viewModel.visualizeKeyframePrompt(scenePrompt)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudioModeButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (selected) {
                    Modifier.background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                NeonCyan.copy(alpha = 0.22f),
                                VividPurple.copy(alpha = 0.24f)
                            )
                        )
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) NeonCyan else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) TextPrimary else TextSecondary
            )
        }
    }
}

@Composable
private fun VideoPlanCard(
    plan: GeneratedVideoPlan,
    activeSceneIndex: Int,
    onSelectScene: (Int) -> Unit,
    onCopyMasterPrompt: () -> Unit,
    onVisualizeKeyframe: (String) -> Unit
) {
    val safeIndex = activeSceneIndex.coerceIn(0, (plan.scenes.size - 1).coerceAtLeast(0))
    val activeScene = plan.scenes.getOrNull(safeIndex)

    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColors = listOf(VividPurple.copy(alpha = 0.65f), NeonCyan.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${plan.durationSeconds}s CINEMATIC BLUEPRINT",
                        style = MaterialTheme.typography.labelSmall,
                        color = VividPurple
                    )
                    Text(
                        text = plan.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                }
                IconButton(
                    onClick = onCopyMasterPrompt,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Copy master video prompt",
                        tint = NeonCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Style: ${plan.visualStyle}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Text(
                text = "Camera: ${plan.cameraMotion}  •  Audio: ${plan.audioDesign}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AbyssalBlack)
                    .border(1.dp, ElectricBlue.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = plan.masterVideoPrompt,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }

            if (plan.scenes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    plan.scenes.forEachIndexed { idx, scene ->
                        val isSelected = idx == safeIndex
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) VividPurple.copy(alpha = 0.25f) else DeepObsidian
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) VividPurple else TextMuted.copy(alpha = 0.3f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onSelectScene(idx) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Scene ${scene.sceneNumber} (${scene.timecode})",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) TextPrimary else TextSecondary
                            )
                        }
                    }
                }

                activeScene?.let { scene ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(GlassSurfaceHigh)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "${scene.cameraAngle} • ${scene.motionCue}",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = scene.visualDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(NeonCyan.copy(alpha = 0.22f), VividPurple.copy(alpha = 0.28f))
                                    )
                                )
                                .border(1.dp, NeonCyan.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                                .clickable { onVisualizeKeyframe(scene.keyframePrompt) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "VISUALIZE SCENE KEYFRAME",
                                style = MaterialTheme.typography.labelLarge,
                                color = NeonCyan
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InspectImageDialog(
    item: GeneratedImageItem,
    onDismiss: () -> Unit,
    onExportPng: () -> Unit
) {
    BackHandler(onBack = onDismiss)
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PitchBlack)
                .statusBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STUDIO PREVIEW (${item.aspectRatio.label})",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close preview",
                        tint = TextPrimary
                    )
                }
            }

            Image(
                bitmap = item.bitmap.asImageBitmap(),
                contentDescription = item.prompt,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clip(RoundedCornerShape(20.dp))
            )

            Column {
                Text(
                    text = item.prompt,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(colors = listOf(NeonCyan, ElectricBlue))
                        )
                        .clickable(onClick = onExportPng)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = null,
                            tint = PitchBlack,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "EXPORT PNG TO STORAGE",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = PitchBlack
                        )
                    }
                }
            }
        }
    }
}
