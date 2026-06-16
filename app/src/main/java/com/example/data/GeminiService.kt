package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class GeminiPart(val text: String)

@JsonClass(generateAdapter = true)
data class GeminiContent(val parts: List<GeminiPart>)

@JsonClass(generateAdapter = true)
data class GeminiRequest(val contents: List<GeminiContent>)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(val content: GeminiContent?)

@JsonClass(generateAdapter = true)
data class GeminiResponse(val candidates: List<GeminiCandidate>?)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val api: GeminiApi = retrofit.create(GeminiApi::class.java)

    /**
     * Ask Gemini for smart recommendations based on current cart items and menu
     */
    suspend fun getSmartRecommendations(
        cartSummary: String,
        availableItemsList: List<MenuItem>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getRuleBasedRecommendation(cartSummary, availableItemsList)
        }

        val itemsPrompt = availableItemsList.joinToString { "${it.name} (Category: ${it.category}, Info: ${it.description})" }
        val prompt = """
            You are "Bite & Sip" Cafe's friendly virtual AI sommelier and assistant.
            Here is the current user's basket/cart: [$cartSummary].
            Here is the menu list: [$itemsPrompt].
            
            Identify 1-2 complementary items or combos from the menu list that would pair beautifully with what the user is ordering.
            Provide:
            1. An appetizing recommendation headline (e.g., "Complete your meal with Peri Peri Fries!").
            2. A short mouth-watering sentence explaining why this pairing was recommended based on taste harmony or a special café promotion.
            Keep the response brief, appetizing, and friendly (under 80 words total). Return plain text without formatting or markdown headers.
        """.trimIndent()

        try {
            val request = GeminiRequest(listOf(GeminiContent(listOf(GeminiPart(prompt)))))
            val response = api.generateContent(apiKey, request)
            val result = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!result.isNullOrEmpty()) {
                result.replace("*", "").trim()
            } else {
                getRuleBasedRecommendation(cartSummary, availableItemsList)
            }
        } catch (e: Exception) {
            getRuleBasedRecommendation(cartSummary, availableItemsList)
        }
    }

    /**
     * Graceful fallback local recommendation.
     */
    fun getRuleBasedRecommendation(cartSummary: String, availableItemsList: List<MenuItem>): String {
        if (cartSummary.isEmpty() || cartSummary.contains("empty", true)) {
            // Hot general recommendation
            val popularItem = availableItemsList.find { it.name == "Non-Veg Combo" || it.name == "Veg Combo Pack" }
                ?: availableItemsList.firstOrNull()
            return "Try Bite & Sip's Best Seller!\nOur ${popularItem?.name} is on hot special today. Super crispy, paired perfectly, saving you up to ₹40 on combos!"
        }

        val lowercaseCart = cartSummary.lowercase()
        return when {
            lowercaseCart.contains("burger") && !lowercaseCart.contains("fries") -> {
                val fries = availableItemsList.find { it.category == "Fries" }?.name ?: "French Fries"
                "Recommended: Classic pairing alert!\nAdd crispy golden $fries to your juicy burger for the ultimate crunch harmony."
            }
            lowercaseCart.contains("burger") && !lowercaseCart.contains("shake") && !lowercaseCart.contains("mojito") -> {
                val beverage = availableItemsList.find { it.category == "Shakes & Mojitos" }?.name ?: "Oreo Shake"
                "Thirsty? Wash down that burger!\nPair your hot food with a perfectly chilled $beverage to balance the savory heat."
            }
            lowercaseCart.contains("fries") && !lowercaseCart.contains("mojito") -> {
                val mojito = availableItemsList.find { it.name.contains("Mojito") }?.name ?: "Blue Mojito"
                "Spicy & Sweet Pair:\nCool off those spicy salted fries with our fresh minty $mojito. Perfect thirst quencher!"
            }
            else -> {
                val dessert = availableItemsList.find { it.name == "Oreo Shake" }?.name ?: "Oreo Shake"
                "Sweeten your Day!\nNo feast is complete without our thick, premium whipped $dessert. Experience blended chocolate bliss!"
            }
        }
    }
}
