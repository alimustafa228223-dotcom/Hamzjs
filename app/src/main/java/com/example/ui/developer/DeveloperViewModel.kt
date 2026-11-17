package com.example.ui.developer

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeneratedWebApp
import com.example.ai.KainatAiRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TAG = "DeveloperViewModel"

sealed interface DevGenState {
    data object Ready : DevGenState
    data object Generating : DevGenState
    data class Error(val message: String) : DevGenState
}

data class DeveloperUiState(
    val prompt: String = "",
    val currentApp: GeneratedWebApp = DefaultOrbitalStarterApp,
    val isEditMode: Boolean = false,
    val isPreviewOpen: Boolean = false,
    val genState: DevGenState = DevGenState.Ready,
    val statusNotice: String? = null
)

class DeveloperViewModel : ViewModel() {

    private val repository = KainatAiRepository()

    private val _uiState = MutableStateFlow(DeveloperUiState())
    val uiState: StateFlow<DeveloperUiState> = _uiState.asStateFlow()

    fun updatePrompt(text: String) {
        _uiState.update { it.copy(prompt = text) }
    }

    fun updateCode(newHtml: String) {
        _uiState.update { state ->
            state.copy(currentApp = state.currentApp.copy(htmlCode = newHtml))
        }
    }

    fun toggleEditMode() {
        _uiState.update { it.copy(isEditMode = !it.isEditMode) }
    }

    fun openPreview() {
        _uiState.update { it.copy(isPreviewOpen = true) }
    }

    fun closePreview() {
        _uiState.update { it.copy(isPreviewOpen = false) }
    }

    fun showNotice(message: String) {
        _uiState.update { it.copy(statusNotice = message) }
    }

    fun clearNotice() {
        _uiState.update { it.copy(statusNotice = null) }
    }

    fun resetToStarter() {
        _uiState.update {
            DeveloperUiState(currentApp = DefaultOrbitalStarterApp)
        }
    }

    fun generateApp(overridePrompt: String? = null) {
        val targetPrompt = (overridePrompt ?: _uiState.value.prompt).trim()
        if (targetPrompt.isEmpty()) return

        _uiState.update {
            it.copy(
                prompt = targetPrompt,
                genState = DevGenState.Generating,
                statusNotice = null
            )
        }

        viewModelScope.launch {
            try {
                val result = withTimeout(60_000L) {
                    repository.generateWebApp(targetPrompt)
                }
                _uiState.update {
                    it.copy(
                        currentApp = result,
                        genState = DevGenState.Ready,
                        isEditMode = false,
                        statusNotice = "Compiled ${result.title}"
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Code generation timed out", e)
                _uiState.update {
                    it.copy(genState = DevGenState.Error("Code generation timed out after 60 seconds."))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Code generation failed", e)
                _uiState.update {
                    it.copy(
                        genState = DevGenState.Error(
                            e.localizedMessage ?: "Failed to generate code."
                        )
                    )
                }
            }
        }
    }
}

private val DefaultOrbitalStarterApp = GeneratedWebApp(
    title = "Kainat Neon Telemetry HUD",
    summary = "Interactive HTML5 Canvas quantum pulse visualizer & live telemetry clock ready to test or customize with AI.",
    features = listOf("HTML5 Canvas Pulse", "Touch Ripple Physics", "Real-Time Telemetry"),
    htmlCode = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Kainat Neon Telemetry HUD</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      background: #000000;
      color: #f8fafc;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      overflow: hidden;
      padding: 20px;
    }
    .hud-card {
      position: relative;
      width: 100%;
      max-width: 380px;
      background: rgba(14, 17, 32, 0.72);
      border: 1px solid rgba(0, 229, 255, 0.45);
      border-radius: 24px;
      padding: 24px;
      text-align: center;
      box-shadow: 0 0 40px rgba(59, 130, 246, 0.25);
      backdrop-filter: blur(12px);
    }
    h1 {
      font-size: 1.2rem;
      letter-spacing: 2px;
      color: #00E5FF;
      margin-bottom: 6px;
    }
    .clock {
      font-size: 2.3rem;
      font-weight: 700;
      background: linear-gradient(90deg, #00E5FF, #3B82F6, #A855F7);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      margin: 10px 0;
    }
    canvas {
      width: 100%;
      height: 180px;
      display: block;
      margin: 12px 0;
      border-radius: 16px;
      background: rgba(5, 6, 12, 0.9);
      border: 1px solid rgba(168, 85, 247, 0.35);
    }
    button {
      margin-top: 8px;
      width: 100%;
      padding: 12px 16px;
      border: none;
      border-radius: 14px;
      font-weight: 700;
      font-size: 0.95rem;
      color: #000000;
      background: linear-gradient(90deg, #00E5FF, #A855F7);
      cursor: pointer;
    }
    button:active { transform: scale(0.98); }
  </style>
</head>
<body>
  <div class="hud-card">
    <h1>KAINAT QUANTUM HUD</h1>
    <div id="clock" class="clock">00:00:00</div>
    <canvas id="wave"></canvas>
    <button id="boostBtn">TRIGGER ENERGY SURGE</button>
  </div>
  <script>
    const clockEl = document.getElementById('clock');
    function updateClock() {
      const now = new Date();
      clockEl.textContent = now.toLocaleTimeString([], { hour12: false });
    }
    setInterval(updateClock, 1000);
    updateClock();

    const canvas = document.getElementById('wave');
    const ctx = canvas.getContext('2d');
    let surge = 1;
    let t = 0;

    function resize() {
      canvas.width = canvas.clientWidth * 2;
      canvas.height = canvas.clientHeight * 2;
    }
    resize();

    document.getElementById('boostBtn').addEventListener('click', () => {
      surge = 3.2;
    });

    function draw() {
      t += 0.05 * surge;
      surge += (1 - surge) * 0.04;
      const w = canvas.width, h = canvas.height;
      ctx.clearRect(0, 0, w, h);

      const colors = ['#00E5FF', '#3B82F6', '#A855F7'];
      colors.forEach((color, idx) => {
        ctx.beginPath();
        ctx.strokeStyle = color;
        ctx.lineWidth = 3;
        for (let x = 0; x <= w; x += 6) {
          const y = h / 2 + Math.sin(x * 0.014 + t + idx * 1.1) * (28 * surge) * Math.sin(x / w * Math.PI);
          if (x === 0) ctx.moveTo(x, y); else ctx.lineTo(x, y);
        }
        ctx.stroke();
      });
      requestAnimationFrame(draw);
    }
    draw();
  </script>
</body>
</html>
    """.trimIndent()
)
