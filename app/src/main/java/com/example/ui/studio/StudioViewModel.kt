package com.example.ui.studio

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeneratedVideoPlan
import com.example.ai.KainatAiRepository
import com.example.ai.StudioAspectRatio
import com.google.firebase.ai.type.QuotaExceededException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.UUID

private const val TAG = "StudioViewModel"

enum class StudioMode {
    IMAGE_SYNTHESIS,
    VIDEO_DIRECTOR
}

data class GeneratedImageItem(
    val id: String = UUID.randomUUID().toString(),
    val prompt: String,
    val aspectRatio: StudioAspectRatio,
    val bitmap: Bitmap
)

sealed interface StudioGenState {
    data object Idle : StudioGenState
    data object EnhancingPrompt : StudioGenState
    data object GeneratingImage : StudioGenState
    data object GeneratingVideoPlan : StudioGenState
    data class Error(val message: String) : StudioGenState
}

data class StudioUiState(
    val mode: StudioMode = StudioMode.IMAGE_SYNTHESIS,
    val prompt: String = "",
    val aspectRatio: StudioAspectRatio = StudioAspectRatio.LANDSCAPE,
    val genState: StudioGenState = StudioGenState.Idle,
    val imageGallery: List<GeneratedImageItem> = emptyList(),
    val selectedImageForInspect: GeneratedImageItem? = null,
    val videoPlans: List<GeneratedVideoPlan> = emptyList(),
    val activeVideoSceneIndex: Int = 0,
    val statusNotice: String? = null
)

class StudioViewModel : ViewModel() {

    private val repository = KainatAiRepository()

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    fun setMode(mode: StudioMode) {
        _uiState.update { it.copy(mode = mode, genState = StudioGenState.Idle) }
    }

    fun updatePrompt(text: String) {
        _uiState.update { it.copy(prompt = text) }
    }

    fun selectAspectRatio(ratio: StudioAspectRatio) {
        _uiState.update { it.copy(aspectRatio = ratio) }
    }

    fun inspectImage(item: GeneratedImageItem?) {
        _uiState.update { it.copy(selectedImageForInspect = item) }
    }

    fun selectSceneIndex(index: Int) {
        _uiState.update { it.copy(activeVideoSceneIndex = index) }
    }

    fun showNotice(message: String) {
        _uiState.update { it.copy(statusNotice = message) }
    }

    fun clearNotice() {
        _uiState.update { it.copy(statusNotice = null) }
    }

    fun resetStudio() {
        _uiState.update { StudioUiState(mode = it.mode, aspectRatio = it.aspectRatio) }
    }

    fun enhancePrompt() {
        val raw = _uiState.value.prompt.trim()
        if (raw.isEmpty()) return

        _uiState.update { it.copy(genState = StudioGenState.EnhancingPrompt) }
        viewModelScope.launch {
            try {
                val enhanced = withTimeout(30_000L) {
                    repository.enhancePrompt(raw)
                }
                _uiState.update {
                    it.copy(
                        prompt = enhanced,
                        genState = StudioGenState.Idle
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Prompt enhancement timed out", e)
                _uiState.update {
                    it.copy(genState = StudioGenState.Error("Prompt enhancement timed out."))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Prompt enhancement failed", e)
                _uiState.update {
                    it.copy(
                        genState = StudioGenState.Error(
                            e.localizedMessage ?: "Failed to enhance prompt."
                        )
                    )
                }
            }
        }
    }

    fun generateCurrentMode(overridePrompt: String? = null) {
        val activePrompt = (overridePrompt ?: _uiState.value.prompt).trim()
        if (activePrompt.isEmpty()) return

        if (overridePrompt != null) {
            _uiState.update { it.copy(prompt = overridePrompt) }
        }

        when (_uiState.value.mode) {
            StudioMode.IMAGE_SYNTHESIS -> generateImage(activePrompt)
            StudioMode.VIDEO_DIRECTOR -> generateVideoBlueprint(activePrompt)
        }
    }

    fun visualizeKeyframePrompt(keyframePrompt: String) {
        _uiState.update {
            it.copy(
                mode = StudioMode.IMAGE_SYNTHESIS,
                prompt = keyframePrompt
            )
        }
        generateImage(keyframePrompt)
    }

    private fun generateImage(prompt: String) {
        val ratio = _uiState.value.aspectRatio
        _uiState.update {
            it.copy(genState = StudioGenState.GeneratingImage, statusNotice = null)
        }

        viewModelScope.launch {
            try {
                val bitmap = withTimeout(60_000L) {
                    repository.generateImage(prompt, ratio)
                }
                if (bitmap != null) {
                    val newItem = GeneratedImageItem(
                        prompt = prompt,
                        aspectRatio = ratio,
                        bitmap = bitmap
                    )
                    _uiState.update {
                        it.copy(
                            genState = StudioGenState.Idle,
                            imageGallery = listOf(newItem) + it.imageGallery,
                            statusNotice = "Image rendered via Nano Banana 2.1"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(genState = StudioGenState.Error("No image was returned by the model."))
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Image generation timed out", e)
                _uiState.update {
                    it.copy(genState = StudioGenState.Error("Image generation timed out after 60 seconds."))
                }
            } catch (e: QuotaExceededException) {
                Log.e(TAG, "Image generation quota exceeded", e)
                _uiState.update {
                    it.copy(genState = StudioGenState.Error("Rate limit or Blaze quota exceeded. Please try again later."))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Image generation failed", e)
                _uiState.update {
                    it.copy(
                        genState = StudioGenState.Error(
                            e.localizedMessage ?: "Image generation failed."
                        )
                    )
                }
            }
        }
    }

    private fun generateVideoBlueprint(prompt: String) {
        _uiState.update {
            it.copy(genState = StudioGenState.GeneratingVideoPlan, statusNotice = null)
        }

        viewModelScope.launch {
            try {
                val plan = withTimeout(60_000L) {
                    repository.generateVideoPlan(prompt)
                }
                _uiState.update {
                    it.copy(
                        genState = StudioGenState.Idle,
                        videoPlans = listOf(plan) + it.videoPlans,
                        activeVideoSceneIndex = 0,
                        statusNotice = "Video blueprint '${plan.title}' synthesized"
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Video blueprint timed out", e)
                _uiState.update {
                    it.copy(genState = StudioGenState.Error("Video blueprint timed out after 60 seconds."))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Video blueprint failed", e)
                _uiState.update {
                    it.copy(
                        genState = StudioGenState.Error(
                            e.localizedMessage ?: "Video blueprint generation failed."
                        )
                    )
                }
            }
        }
    }
}
