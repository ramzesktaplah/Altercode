package com.ai.altercode.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tracks how many AI code conversions the user has performed.
 * After [FREE_RUNS] uses, the user is prompted to watch a rewarded ad
 * to continue. Watching an ad grants [REWARD_RUNS] additional free runs.
 *
 * Persistence is handled via SharedPreferences so the counter survives
 * app restarts. Basic app functionality (editing, browsing history,
 * settings) is never blocked — only new AI actions are gated.
 */
class UsageTracker(context: Context) {

    companion object {
        /** Number of free AI runs before the ad gate appears. */
        const val FREE_RUNS = 5

        /** Number of bonus runs granted after watching a rewarded ad. */
        const val REWARD_RUNS = 3

        /** Ad unit IDs — replace with real AdMob unit IDs before production release. */
        const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917" // Google's test unit

        private const val KEY_RUNS_USED = "runs_used"
        private const val KEY_TOTAL_RUNS = "total_runs"
        private const val KEY_ADS_WATCHED = "ads_watched"
    }

    private val prefs = CryptoKeyManager.encryptedPrefs(context.applicationContext, "altercode_usage")

    private val _runsUsed = MutableStateFlow(prefs.getInt(KEY_RUNS_USED, 0))
    val runsUsed: StateFlow<Int> = _runsUsed.asStateFlow()

    /** Total times the user has ever triggered an AI action (for stats). */
    private val _totalRuns = MutableStateFlow(prefs.getInt(KEY_TOTAL_RUNS, 0))
    val totalRuns: StateFlow<Int> = _totalRuns.asStateFlow()

    private val _totalAdsWatched = MutableStateFlow(prefs.getInt(KEY_ADS_WATCHED, 0))
    val totalAdsWatched: StateFlow<Int> = _totalAdsWatched.asStateFlow()

    /** How many free runs remain before the ad gate triggers. */
    val freeRunsRemaining: Int
        get() = (FREE_RUNS - _runsUsed.value).coerceAtLeast(0)

    /** True when the user has exhausted all free runs and must watch an ad. */
    val isAdGateActive: Boolean
        get() = _runsUsed.value >= FREE_RUNS

    /**
     * Called before an AI action runs. Returns true if the action is allowed
     * (free runs remain), false if the ad gate is active.
     */
    fun canRun(): Boolean = !isAdGateActive

    /**
     * Record that an AI action was consumed. Increments both the current
     * cycle counter and the lifetime total.
     */
    fun recordRun() {
        val newUsed = _runsUsed.value + 1
        val newTotal = _totalRuns.value + 1
        _runsUsed.value = newUsed
        _totalRuns.value = newTotal
        prefs.edit()
            .putInt(KEY_RUNS_USED, newUsed)
            .putInt(KEY_TOTAL_RUNS, newTotal)
            .apply()
    }

    /**
     * Called after a rewarded ad is successfully watched.
     * Grants [REWARD_RUNS] bonus runs and records the ad view.
     */
    fun grantReward() {
        val newUsed = (_runsUsed.value - REWARD_RUNS).coerceAtLeast(0)
        val newAdsWatched = _totalAdsWatched.value + 1
        _runsUsed.value = newUsed
        _totalAdsWatched.value = newAdsWatched
        prefs.edit()
            .putInt(KEY_RUNS_USED, newUsed)
            .putInt(KEY_ADS_WATCHED, newAdsWatched)
            .apply()
    }

    /**
     * Refund a consumed run (e.g. when an AI action failed).
     * Decrements the cycle counter so the user doesn't lose a free attempt.
     */
    fun refundRun() {
        val refunded = (_runsUsed.value - 1).coerceAtLeast(0)
        val refundedTotal = (_totalRuns.value - 1).coerceAtLeast(0)
        _runsUsed.value = refunded
        _totalRuns.value = refundedTotal
        prefs.edit()
            .putInt(KEY_RUNS_USED, refunded)
            .putInt(KEY_TOTAL_RUNS, refundedTotal)
            .apply()
    }

    /** Reset the usage counter (used in Settings or for testing). */
    fun reset() {
        _runsUsed.value = 0
        prefs.edit().putInt(KEY_RUNS_USED, 0).apply()
    }

}
