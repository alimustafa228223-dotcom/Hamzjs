package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ai.configureAppCheck
import com.example.ui.chat.ChatScreen
import com.example.ui.chat.ChatViewModel
import com.example.ui.components.FloatingGlassBottomNav
import com.example.ui.components.KainatTab
import com.example.ui.components.LiquidCosmicBackdrop
import com.example.ui.components.ScrollReactiveTopBar
import com.example.ui.developer.DeveloperScreen
import com.example.ui.developer.DeveloperViewModel
import com.example.ui.studio.StudioScreen
import com.example.ui.studio.StudioViewModel
import com.example.ui.theme.KainatAiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check with launch intent before UI or ViewModels
        configureAppCheck(this, intent)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )

        setContent {
            KainatAiTheme {
                KainatCommandCenterApp()
            }
        }
    }
}

@Composable
fun KainatCommandCenterApp() {
    var selectedTab by remember { mutableStateOf(KainatTab.CHAT) }
    var chatScrollProgress by remember { mutableFloatStateOf(0f) }
    var devScrollProgress by remember { mutableFloatStateOf(0f) }
    var studioScrollProgress by remember { mutableFloatStateOf(0f) }

    val chatViewModel: ChatViewModel = viewModel()
    val developerViewModel: DeveloperViewModel = viewModel()
    val studioViewModel: StudioViewModel = viewModel()

    // Navigate back to Chat tab before exiting when on Developer or Studio tabs
    BackHandler(enabled = selectedTab != KainatTab.CHAT) {
        selectedTab = KainatTab.CHAT
    }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    val topBarClearance = statusBarTop + 76.dp
    val bottomDockClearance = max(navBarBottom + 88.dp, imeBottom + 8.dp)

    val currentSubtitle = when (selectedTab) {
        KainatTab.CHAT -> stringResource(R.string.chat_subtitle)
        KainatTab.DEVELOPER -> stringResource(R.string.dev_subtitle)
        KainatTab.STUDIO -> stringResource(R.string.studio_subtitle)
    }

    val activeScrollProgress = when (selectedTab) {
        KainatTab.CHAT -> chatScrollProgress
        KainatTab.DEVELOPER -> devScrollProgress
        KainatTab.STUDIO -> studioScrollProgress
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LiquidCosmicBackdrop()

        Crossfade(
            targetState = selectedTab,
            animationSpec = tween(220),
            label = "kainat_tab_crossfade",
            modifier = Modifier.fillMaxSize()
        ) { tab ->
            when (tab) {
                KainatTab.CHAT -> ChatScreen(
                    viewModel = chatViewModel,
                    topPadding = topBarClearance,
                    bottomNavHeight = bottomDockClearance,
                    onScrollProgressChanged = { chatScrollProgress = it }
                )

                KainatTab.DEVELOPER -> DeveloperScreen(
                    viewModel = developerViewModel,
                    topPadding = topBarClearance,
                    bottomNavHeight = bottomDockClearance,
                    onScrollProgressChanged = { devScrollProgress = it }
                )

                KainatTab.STUDIO -> StudioScreen(
                    viewModel = studioViewModel,
                    topPadding = topBarClearance,
                    bottomNavHeight = bottomDockClearance,
                    onScrollProgressChanged = { studioScrollProgress = it }
                )
            }
        }

        ScrollReactiveTopBar(
            scrollProgress = activeScrollProgress,
            subtitle = currentSubtitle,
            onClearAction = {
                when (selectedTab) {
                    KainatTab.CHAT -> chatViewModel.resetSession()
                    KainatTab.DEVELOPER -> developerViewModel.resetToStarter()
                    KainatTab.STUDIO -> studioViewModel.resetStudio()
                }
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        FloatingGlassBottomNav(
            selectedTab = selectedTab,
            onSelectTab = { selectedTab = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
