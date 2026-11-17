package com.example.ai

import android.graphics.Bitmap
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.AspectRatio
import com.google.firebase.ai.type.ImageConfig
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.ImageSize
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.json.JSONObject

data class GeneratedWebApp(
    val title: String,
    val summary: String,
    val features: List<String>,
    val htmlCode: String
)

data class StoryboardScene(
    val sceneNumber: Int,
    val timecode: String,
    val visualDescription: String,
    val cameraAngle: String,
    val motionCue: String,
    val keyframePrompt: String
)

data class GeneratedVideoPlan(
    val title: String,
    val visualStyle: String,
    val cameraMotion: String,
    val audioDesign: String,
    val durationSeconds: Int,
    val masterVideoPrompt: String,
    val scenes: List<StoryboardScene>
)

enum class StudioAspectRatio(val label: String, val sdkRatio: AspectRatio) {
    SQUARE("1:1", AspectRatio.SQUARE_1x1),
    LANDSCAPE("16:9", AspectRatio.LANDSCAPE_16x9),
    PORTRAIT("9:16", AspectRatio.PORTRAIT_9x16),
    CINEMA("4:3", AspectRatio.LANDSCAPE_4x3)
}

class KainatAiRepository {

    private val chatModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(
                    """
                    You are Kainat AI (کائنات اے آئی), an advanced, authoritative, sci-fi command-center intelligence.
                    Deliver clear, decisive, high-signal responses with poised executive clarity.
                    You are fluent in both English and Urdu — respond in whichever language the user addresses you in (or seamlessly support bilingual Urdu/English queries).
                    Keep formatting clean and concise for mobile reading and voice synthesis.
                    """.trimIndent()
                )
            }
        )
    }

    private val codeGenSchema = Schema.obj(
        properties = mapOf(
            "title" to Schema.string(description = "Short title of the generated interactive web app"),
            "summary" to Schema.string(description = "Brief 1-2 sentence explanation of what was built"),
            "features" to Schema.array(
                items = Schema.string(),
                description = "3-4 short bullet labels of implemented features"
            ),
            "htmlCode" to Schema.string(
                description = "Complete, self-contained single-file HTML5 document with embedded <style> CSS and <script> JS that works offline inside an Android WebView without external CDN dependencies"
            )
        )
    )

    private val codeGenModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = codeGenSchema
            },
            systemInstruction = content {
                text(
                    """
                    You are Kainat AI's elite frontend & WebGL/Canvas systems architect.
                    Generate complete, self-contained single-file HTML5 + CSS3 + vanilla JavaScript applications.
                    Visual aesthetic: pitch-black (#000000) background, neon cyan (#00E5FF), electric blue (#3B82F6), vivid purple (#A855F7), glassmorphic cards, responsive mobile viewport (<meta name="viewport" content="width=device-width, initial-scale=1.0">), and smooth touch/pointer interactivity.
                    CRITICAL: All CSS and JS must be embedded inline in the single HTML file with zero external scripts or broken CDN links so it executes immediately inside a sandboxed WebView.
                    """.trimIndent()
                )
            }
        )
    }

    private val videoDirectorSchema = Schema.obj(
        properties = mapOf(
            "title" to Schema.string(description = "Cinematic production title"),
            "visualStyle" to Schema.string(description = "Color grading, lighting, and rendering style"),
            "cameraMotion" to Schema.string(description = "Primary camera choreography"),
            "audioDesign" to Schema.string(description = "Soundtrack, ambience, and SFX direction"),
            "durationSeconds" to Schema.integer(description = "Recommended total duration in seconds"),
            "masterVideoPrompt" to Schema.string(description = "Single cohesive high-detail prompt for AI video synthesis"),
            "scenes" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "sceneNumber" to Schema.integer(description = "1-based index"),
                        "timecode" to Schema.string(description = "Time range like 00:00 - 00:03"),
                        "visualDescription" to Schema.string(description = "What happens visually in the shot"),
                        "cameraAngle" to Schema.string(description = "Lens and framing specification"),
                        "motionCue" to Schema.string(description = "Movement and lighting dynamics"),
                        "keyframePrompt" to Schema.string(description = "Standalone still-image prompt to visualize this keyframe")
                    )
                ),
                description = "Exactly 4 sequential cinematic scenes"
            )
        )
    )

    private val videoDirectorModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = videoDirectorSchema
            },
            systemInstruction = content {
                text(
                    "You are Kainat AI's cinematic director. Transform user concepts into richly detailed 4-scene AI video production blueprints with vivid camera choreography, atmospheric lighting, and keyframe prompts."
                )
            }
        )
    }

    private val promptEnhancerModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            systemInstruction = content {
                text(
                    "Rewrite the user's brief visual idea into a single rich, vivid, high-detail generative prompt (max 55 words) specifying lighting, composition, texture, and mood. Output only the enhanced prompt text without quotes or preamble."
                )
            }
        )
    }

    fun createChatSession(): Chat = chatModel.startChat()

    fun sendChatMessageStream(chat: Chat, prompt: String): Flow<String> {
        return chat.sendMessageStream(prompt).mapNotNull { chunk -> chunk.text }
    }

    suspend fun generateWebApp(prompt: String): GeneratedWebApp {
        val response = codeGenModel.generateContent(prompt)
        val rawJson = response.text
            ?: throw IllegalStateException("Code generator returned an empty response.")
        val json = JSONObject(rawJson)
        val featuresJson = json.optJSONArray("features")
        val features = if (featuresJson != null) {
            (0 until featuresJson.length()).map { featuresJson.getString(it) }
        } else {
            emptyList()
        }
        return GeneratedWebApp(
            title = json.optString("title", "Kainat Web Module"),
            summary = json.optString("summary", "Interactive HTML/CSS/JS module"),
            features = features,
            htmlCode = json.getString("htmlCode")
        )
    }

    suspend fun generateImage(prompt: String, aspectRatio: StudioAspectRatio): Bitmap? {
        val config = generationConfig {
            responseModalities = listOf(ResponseModality.IMAGE)
            imageConfig = ImageConfig.builder()
                .setAspectRatio(aspectRatio.sdkRatio)
                .setImageSize(ImageSize.SIZE_1K)
                .build()
        }
        val imageModel = Firebase.ai.generativeModel(
            modelName = "gemini-nano-banana-2.1",
            generationConfig = config
        )
        val response = imageModel.generateContent(prompt)
        val imagePart = response.candidates.firstOrNull()
            ?.content?.parts?.filterIsInstance<ImagePart>()
            ?.firstOrNull()
        return imagePart?.image
    }

    suspend fun generateVideoPlan(prompt: String): GeneratedVideoPlan {
        val response = videoDirectorModel.generateContent(prompt)
        val rawJson = response.text
            ?: throw IllegalStateException("Video director returned an empty response.")
        val json = JSONObject(rawJson)
        val scenesArr = json.getJSONArray("scenes")
        val scenes = (0 until scenesArr.length()).map { i ->
            val obj = scenesArr.getJSONObject(i)
            StoryboardScene(
                sceneNumber = obj.optInt("sceneNumber", i + 1),
                timecode = obj.optString("timecode", "00:0${i * 3} - 00:0${(i + 1) * 3}"),
                visualDescription = obj.getString("visualDescription"),
                cameraAngle = obj.getString("cameraAngle"),
                motionCue = obj.getString("motionCue"),
                keyframePrompt = obj.getString("keyframePrompt")
            )
        }
        return GeneratedVideoPlan(
            title = json.getString("title"),
            visualStyle = json.getString("visualStyle"),
            cameraMotion = json.getString("cameraMotion"),
            audioDesign = json.getString("audioDesign"),
            durationSeconds = json.optInt("durationSeconds", 12),
            masterVideoPrompt = json.getString("masterVideoPrompt"),
            scenes = scenes
        )
    }

    suspend fun enhancePrompt(rawPrompt: String): String {
        val response = promptEnhancerModel.generateContent(rawPrompt)
        return response.text?.trim()?.takeIf { it.isNotBlank() } ?: rawPrompt
    }
}
