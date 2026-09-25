package com.example.ai.api

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ApiUsageStats(
    val totalRequests: Long = 0,
    val totalInputTokens: Long = 0,
    val totalOutputTokens: Long = 0,
    val dailyRequests: Long = 0,
    val monthlyRequests: Long = 0,
    val inputPricePerMillion: Double = 0.0,
    val outputPricePerMillion: Double = 0.0,
    val estimatedCostUsd: Double? = null,
    val isCostAvailable: Boolean = false
)

/**
 * Persists and computes real local API token & request metrics.
 * Supports user-configured token pricing per 1M tokens for local calculation.
 */
class ApiUsageTracker private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("titonox_api_usage_metrics", Context.MODE_PRIVATE)

    private val _usageStats = MutableStateFlow(loadStats())
    val usageStats: StateFlow<ApiUsageStats> = _usageStats.asStateFlow()

    companion object {
        private const val KEY_TOTAL_REQUESTS = "total_requests"
        private const val KEY_INPUT_TOKENS = "input_tokens"
        private const val KEY_OUTPUT_TOKENS = "output_tokens"
        private const val KEY_DAILY_REQUESTS = "daily_requests"
        private const val KEY_MONTHLY_REQUESTS = "monthly_requests"
        private const val KEY_LAST_DAY = "last_day"
        private const val KEY_LAST_MONTH = "last_month"
        private const val KEY_PRICE_INPUT_1M = "price_input_1m"
        private const val KEY_PRICE_OUTPUT_1M = "price_output_1m"

        @Volatile
        private var instance: ApiUsageTracker? = null

        fun getInstance(context: Context): ApiUsageTracker {
            return instance ?: synchronized(this) {
                instance ?: ApiUsageTracker(context.applicationContext).also { instance = it }
            }
        }
    }

    private fun loadStats(): ApiUsageStats {
        val totalReq = prefs.getLong(KEY_TOTAL_REQUESTS, 0L)
        val inTok = prefs.getLong(KEY_INPUT_TOKENS, 0L)
        val outTok = prefs.getLong(KEY_OUTPUT_TOKENS, 0L)
        val dailyReq = prefs.getLong(KEY_DAILY_REQUESTS, 0L)
        val monthlyReq = prefs.getLong(KEY_MONTHLY_REQUESTS, 0L)
        val inPrice = prefs.getFloat(KEY_PRICE_INPUT_1M, 0.0f).toDouble()
        val outPrice = prefs.getFloat(KEY_PRICE_OUTPUT_1M, 0.0f).toDouble()

        val hasPricing = inPrice > 0.0 || outPrice > 0.0
        val cost = if (hasPricing) {
            (inTok / 1_000_000.0 * inPrice) + (outTok / 1_000_000.0 * outPrice)
        } else null

        return ApiUsageStats(
            totalRequests = totalReq,
            totalInputTokens = inTok,
            totalOutputTokens = outTok,
            dailyRequests = dailyReq,
            monthlyRequests = monthlyReq,
            inputPricePerMillion = inPrice,
            outputPricePerMillion = outPrice,
            estimatedCostUsd = cost,
            isCostAvailable = hasPricing
        )
    }

    @Synchronized
    fun recordRequest(inputTokens: Int = 0, outputTokens: Int = 0) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val thisMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())

        val lastDay = prefs.getString(KEY_LAST_DAY, "")
        val lastMonth = prefs.getString(KEY_LAST_MONTH, "")

        val dailyReq = if (lastDay == today) prefs.getLong(KEY_DAILY_REQUESTS, 0L) + 1 else 1L
        val monthlyReq = if (lastMonth == thisMonth) prefs.getLong(KEY_MONTHLY_REQUESTS, 0L) + 1 else 1L

        val totalReq = prefs.getLong(KEY_TOTAL_REQUESTS, 0L) + 1
        val inTok = prefs.getLong(KEY_INPUT_TOKENS, 0L) + inputTokens
        val outTok = prefs.getLong(KEY_OUTPUT_TOKENS, 0L) + outputTokens

        prefs.edit()
            .putLong(KEY_TOTAL_REQUESTS, totalReq)
            .putLong(KEY_INPUT_TOKENS, inTok)
            .putLong(KEY_OUTPUT_TOKENS, outTok)
            .putLong(KEY_DAILY_REQUESTS, dailyReq)
            .putLong(KEY_MONTHLY_REQUESTS, monthlyReq)
            .putString(KEY_LAST_DAY, today)
            .putString(KEY_LAST_MONTH, thisMonth)
            .apply()

        _usageStats.value = loadStats()
    }

    @Synchronized
    fun updatePricing(inputPricePerMillion: Double, outputPricePerMillion: Double) {
        prefs.edit()
            .putFloat(KEY_PRICE_INPUT_1M, inputPricePerMillion.toFloat())
            .putFloat(KEY_PRICE_OUTPUT_1M, outputPricePerMillion.toFloat())
            .apply()

        _usageStats.value = loadStats()
    }

    @Synchronized
    fun clearUsage() {
        prefs.edit()
            .putLong(KEY_TOTAL_REQUESTS, 0L)
            .putLong(KEY_INPUT_TOKENS, 0L)
            .putLong(KEY_OUTPUT_TOKENS, 0L)
            .putLong(KEY_DAILY_REQUESTS, 0L)
            .putLong(KEY_MONTHLY_REQUESTS, 0L)
            .apply()

        _usageStats.value = loadStats()
    }
}
