package com.example.chomba.ai
import android.content.Context
import android.util.Log
import android.os.AsyncTask
import com.example.chomba.R
import com.example.chomba.data.Card
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.json.JSONObject

class GptEvaluator(private val context: Context){

    private val client = OkHttpClient()
    private val apiKey = context.getString(R.string.gpt_key)
    private val prompt = context.getString(R.string.gpt_prompt)

    suspend fun predict(playerHand: List<Card>): Int? {
        val hand = playerHand.joinToString(", ") { "{value: ${it.value}, suit: ${it.suit}}" }
        val systemMessage = "System: $prompt"
        val userMessage = "User: $hand"

        return withContext(Dispatchers.IO) {
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = """
            {
                "model": "gpt-3.5-turbo",
                "messages": [
                    {
                        "role": "system",
                        "content": "$systemMessage"
                    },
                    {
                        "role": "user",
                        "content": "$userMessage"
                    }
                ]
            }
        """.trimIndent().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            Log.d("GPT", "Response: $responseBody")

            // Парсим JSON-ответ и извлекаем значение Score
            val jsonObject = JSONObject(responseBody)
            val choices = jsonObject.getJSONArray("choices")
            if (choices.length() > 0) {
                val message = choices.getJSONObject(0).getJSONObject("message").getString("content")
                // Use regular expression to find the number after "Score:"
                val regex = """\{Score:\s*(\d+)\}""".toRegex()
                val matchResult = regex.find(message)
                if (matchResult != null) {
                    val scoreString = matchResult.groupValues[1]
                    return@withContext scoreString.toIntOrNull()
                } else {
                    return@withContext null
                }
            } else {
                return@withContext null
            }
        }
    }
}