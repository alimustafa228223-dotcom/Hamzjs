package com.example.ui.developer

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Visibility
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.AbyssalBlack
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.GlassSurfaceHigh
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.SyntaxAttr
import com.example.ui.theme.SyntaxComment
import com.example.ui.theme.SyntaxKeyword
import com.example.ui.theme.SyntaxNumber
import com.example.ui.theme.SyntaxString
import com.example.ui.theme.SyntaxTag
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividPurple

@Composable
fun DeveloperScreen(
    viewModel: DeveloperViewModel,
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

    // Zero-permission Storage Access Framework file export (index.html)
    val exportDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/html")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(uiState.currentApp.htmlCode.toByteArray(Charsets.UTF_8))
                }
                viewModel.showNotice("Exported HTML file to device storage")
            } catch (e: Exception) {
                viewModel.showNotice("Export failed: ${e.localizedMessage ?: "I/O error"}")
            }
        }
    }

    if (uiState.isPreviewOpen) {
        LiveCodePreviewModal(
            title = uiState.currentApp.title,
            htmlCode = uiState.currentApp.htmlCode,
            onDismiss = viewModel::closePreview
        )
    }

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
            // 1. AI Code Prompt Architect Card
            item(key = "dev_prompt_card") {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColors = listOf(
                        NeonCyan.copy(alpha = 0.6f),
                        ElectricBlue.copy(alpha = 0.45f),
                        VividPurple.copy(alpha = 0.6f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Terminal,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "CODE SANDBOX ARCHITECT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonCyan
                                )
                            }
                            if (uiState.genState is DevGenState.Generating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = NeonCyan
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = uiState.prompt,
                            onValueChange = viewModel::updatePrompt,
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.dev_hint),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted
                                )
                            },
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan.copy(alpha = 0.7f),
                                unfocusedBorderColor = ElectricBlue.copy(alpha = 0.35f),
                                focusedContainerColor = AbyssalBlack,
                                unfocusedContainerColor = AbyssalBlack,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = NeonCyan
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dev_prompt_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val starterTemplates = listOf(
                            "3D Neon Solar System canvas",
                            "Glassmorphic scientific calculator",
                            "Interactive synth pad visualizer",
                            "Retro neon snake arcade game"
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            starterTemplates.forEach { preset ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(GlassSurfaceHigh)
                                        .border(
                                            1.dp,
                                            VividPurple.copy(alpha = 0.45f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable(enabled = uiState.genState !is DevGenState.Generating) {
                                            viewModel.generateApp(preset)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        text = preset,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(NeonCyan, ElectricBlue, VividPurple)
                                    )
                                )
                                .clickable(
                                    enabled = uiState.prompt.isNotBlank() &&
                                        uiState.genState !is DevGenState.Generating
                                ) { viewModel.generateApp() }
                                .padding(vertical = 12.dp)
                                .testTag("dev_generate_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoFixHigh,
                                    contentDescription = null,
                                    tint = PitchBlack,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (uiState.genState is DevGenState.Generating) {
                                        "SYNTHESIZING HTML / CSS / JS…"
                                    } else {
                                        "GENERATE CODE"
                                    },
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = PitchBlack
                                )
                            }
                        }

                        AnimatedVisibility(visible = uiState.genState is DevGenState.Error) {
                            val err = (uiState.genState as? DevGenState.Error)?.message.orEmpty()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CrimsonError.copy(alpha = 0.14f))
                                    .border(1.dp, CrimsonError.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ErrorOutline,
                                    contentDescription = "Generation error",
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

            // 2. Action Toolbar: Try Code & Export Code
            item(key = "dev_actions_card") {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColors = listOf(
                        ElectricBlue.copy(alpha = 0.55f),
                        VividPurple.copy(alpha = 0.55f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = uiState.currentApp.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.currentApp.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        if (uiState.currentApp.features.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                uiState.currentApp.features.forEach { feat ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(NeonCyan.copy(alpha = 0.12f))
                                            .border(
                                                0.8.dp,
                                                NeonCyan.copy(alpha = 0.45f),
                                                RoundedCornerShape(50)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = feat,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NeonCyan
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // TRY CODE button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(EmeraldGlow, NeonCyan)
                                        )
                                    )
                                    .clickable { viewModel.openPreview() }
                                    .padding(vertical = 12.dp)
                                    .testTag("dev_try_code_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.PlayArrow,
                                        contentDescription = null,
                                        tint = PitchBlack,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.dev_try_code),
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = PitchBlack
                                    )
                                }
                            }

                            // EXPORT CODE button (downloads index.html to device storage via SAF)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(VividPurple.copy(alpha = 0.22f))
                                    .border(
                                        1.dp,
                                        Brush.horizontalGradient(
                                            colors = listOf(ElectricBlue, VividPurple)
                                        ),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        val safeSlug = uiState.currentApp.title
                                            .lowercase()
                                            .replace(Regex("[^a-z0-9]+"), "_")
                                            .trim('_')
                                            .ifBlank { "kainat_app" }
                                        exportDocumentLauncher.launch("$safeSlug.html")
                                    }
                                    .padding(vertical = 12.dp)
                                    .testTag("dev_export_code_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Download,
                                        contentDescription = null,
                                        tint = TextPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.dev_export_code),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Syntax-Formatted Code Editor Card
            item(key = "dev_code_editor_card") {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color(0xE6050711),
                    borderColors = listOf(
                        NeonCyan.copy(alpha = 0.45f),
                        ElectricBlue.copy(alpha = 0.35f),
                        VividPurple.copy(alpha = 0.45f)
                    )
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Editor header bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DeepObsidian)
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Code,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "index.html",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextPrimary
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = viewModel::toggleEditMode,
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isEditMode) {
                                            Icons.Rounded.Visibility
                                        } else {
                                            Icons.Rounded.Edit
                                        },
                                        contentDescription = if (uiState.isEditMode) {
                                            "Switch to syntax-highlighted view"
                                        } else {
                                            "Edit code manually"
                                        },
                                        tint = NeonCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(
                                            Context.CLIPBOARD_SERVICE
                                        ) as ClipboardManager
                                        clipboard.setPrimaryClip(
                                            ClipData.newPlainText(
                                                "Kainat HTML Code",
                                                uiState.currentApp.htmlCode
                                            )
                                        )
                                        viewModel.showNotice("Code copied to clipboard")
                                    },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("dev_copy_code_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ContentCopy,
                                        contentDescription = "Copy code",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, uiState.currentApp.title)
                                            putExtra(Intent.EXTRA_TEXT, uiState.currentApp.htmlCode)
                                        }
                                        context.startActivity(
                                            Intent.createChooser(shareIntent, "Share generated code")
                                        )
                                    },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Share,
                                        contentDescription = "Share code",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        if (uiState.isEditMode) {
                            OutlinedTextField(
                                value = uiState.currentApp.htmlCode,
                                onValueChange = viewModel::updateCode,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    cursorColor = NeonCyan
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(380.dp)
                                    .padding(8.dp)
                            )
                        } else {
                            SyntaxFormattedCodeView(
                                code = uiState.currentApp.htmlCode,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(360.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SyntaxFormattedCodeView(
    code: String,
    modifier: Modifier = Modifier
) {
    val highlightedLines = remember(code) {
        code.lines().map { line -> highlightCodeLine(line) }
    }

    Box(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .horizontalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            highlightedLines.forEachIndexed { index, annotatedLine ->
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = "${index + 1}".padStart(3, ' '),
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.5.sp,
                        lineHeight = 17.sp,
                        color = TextMuted,
                        modifier = Modifier.width(34.dp)
                    )
                    Text(
                        text = annotatedLine,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

private fun highlightCodeLine(line: String): AnnotatedString {
    return buildAnnotatedString {
        append(line)
        val trimmed = line.trimStart()
        if (trimmed.startsWith("//") || trimmed.startsWith("<!--") || trimmed.startsWith("/*")) {
            addStyle(SpanStyle(color = SyntaxComment), 0, line.length)
            return@buildAnnotatedString
        }

        // HTML tags
        Regex("</?[a-zA-Z0-9\\-]+>?|>").findAll(line).forEach { match ->
            addStyle(SpanStyle(color = SyntaxTag), match.range.first, match.range.last + 1)
        }
        // JS/CSS keywords
        Regex("\\b(const|let|var|function|return|if|else|for|while|class|import|export|new|true|false|null|document|window|requestAnimationFrame)\\b")
            .findAll(line)
            .forEach { match ->
                addStyle(SpanStyle(color = SyntaxKeyword), match.range.first, match.range.last + 1)
            }
        // Attributes / CSS keys
        Regex("\\b([a-zA-Z\\-]+)(?=\\s*[:=])").findAll(line).forEach { match ->
            addStyle(SpanStyle(color = SyntaxAttr), match.range.first, match.range.last + 1)
        }
        // Numbers
        Regex("\\b\\d+(\\.\\d+)?(px|rem|em|vh|vw|%|s|ms)?\\b").findAll(line).forEach { match ->
            addStyle(SpanStyle(color = SyntaxNumber), match.range.first, match.range.last + 1)
        }
        // Quoted strings
        Regex("([\"'`]).*?\\1").findAll(line).forEach { match ->
            addStyle(SpanStyle(color = SyntaxString), match.range.first, match.range.last + 1)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LiveCodePreviewModal(
    title: String,
    htmlCode: String,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    var reloadTrigger by remember { mutableIntStateOf(0) }
    val consoleLogs = remember { mutableStateListOf<String>() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PitchBlack)
                .statusBarsPadding()
        ) {
            // Top bar of live sandbox preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepObsidian)
                    .border(0.5.dp, NeonCyan.copy(alpha = 0.35f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LIVE SANDBOX PREVIEW",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldGlow
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1
                    )
                }

                Row {
                    IconButton(
                        onClick = {
                            consoleLogs.clear()
                            reloadTrigger++
                        },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Reload preview",
                            tint = NeonCyan
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("close_preview_modal")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close live preview",
                            tint = TextPrimary
                        )
                    }
                }
            }

            // Sandboxed WebView
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        setBackgroundColor(android.graphics.Color.BLACK)
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.allowFileAccess = false
                        settings.allowContentAccess = false
                        webViewClient = WebViewClient()
                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                consoleMessage?.let {
                                    consoleLogs.add(
                                        "[${it.messageLevel().name}] ${it.message()} (L${it.lineNumber()})"
                                    )
                                    if (consoleLogs.size > 6) consoleLogs.removeAt(0)
                                }
                                return true
                            }
                        }
                        loadDataWithBaseURL(null, htmlCode, "text/html", "UTF-8", null)
                    }
                },
                update = { webView ->
                    if (reloadTrigger >= 0) {
                        webView.loadDataWithBaseURL(null, htmlCode, "text/html", "UTF-8", null)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("live_webview_preview")
            )

            // Live Console strip if logs emitted
            if (consoleLogs.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AbyssalBlack)
                        .border(0.5.dp, VividPurple.copy(alpha = 0.4f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "CONSOLE OUTPUT",
                        style = MaterialTheme.typography.labelSmall,
                        color = VividPurple
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    consoleLogs.takeLast(3).forEach { logLine ->
                        Text(
                            text = logLine,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
