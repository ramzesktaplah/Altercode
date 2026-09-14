package com.ai.altercode.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.ai.altercode.data.UsageTracker
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages loading and displaying AdMob rewarded video ads.
 *
 * Design decisions:
 * - Ads are **preloaded** in the background so they're ready when the gate triggers.
 * - If an ad fails to load, the app **degrades gracefully**: the user gets a free run
 *   rather than being blocked by a broken ad. This ensures basic functionality is
 *   never gated behind a potentially-unavailable network resource.
 * - Ad loading is retried automatically after a failure or after an ad is shown.
 */
class RewardedAdManager(
    private val context: Context,
    private val usageTracker: UsageTracker
) {

    private val TAG = "RewardedAdManager"

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    private val _isAdReady = MutableStateFlow(false)
    /** True when a rewarded ad is loaded and ready to show. */
    val isAdReady: StateFlow<Boolean> = _isAdReady.asStateFlow()

    private val _isAdLoading = MutableStateFlow(false)
    val isAdLoading: StateFlow<Boolean> = _isAdLoading.asStateFlow()

    /**
     * Starts loading a rewarded ad in the background. Safe to call multiple times;
     * duplicate calls while already loading are ignored.
     */
    fun loadAd() {
        if (isLoading || rewardedAd != null) return
        isLoading = true
        _isAdLoading.value = true

        RewardedAd.load(
            context,
            UsageTracker.REWARDED_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded ad loaded successfully")
                    rewardedAd = ad
                    isLoading = false
                    _isAdLoading.value = false
                    _isAdReady.value = true
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Ad dismissed")
                            rewardedAd = null
                            _isAdReady.value = false
                            // Preload the next ad
                            loadAd()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.w(TAG, "Ad failed to show: ${adError.message}")
                            rewardedAd = null
                            _isAdReady.value = false
                            loadAd()
                        }
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.w(TAG, "Rewarded ad failed to load (code ${adError.code}): ${adError.message}")
                    rewardedAd = null
                    isLoading = false
                    _isAdLoading.value = false
                    _isAdReady.value = false
                    // Retry after a short delay
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        loadAd()
                    }, 15_000L)
                }
            }
        )
    }

    /**
     * Shows the rewarded ad if one is loaded. When the user earns the reward
     * (or if the ad isn't available), [onRewardEarned] is called so the caller
     * can grant the bonus runs.
     *
     * If no ad is ready, this **gracefully** calls [onRewardEarned] to avoid
     * blocking the user — a missing ad should never prevent app usage.
     *
     * @param activity The current activity (required by the ads SDK).
     * @param onRewardEarned Called when the user should receive bonus runs.
     */
    fun showAd(activity: Activity, onRewardEarned: () -> Unit) {
        val ad = rewardedAd
        if (ad == null) {
            Log.w(TAG, "No ad ready — granting free run to avoid blocking user")
            onRewardEarned()
            // Try to preload for next time
            loadAd()
            return
        }

        ad.show(activity) { rewardItem ->
            val amount = rewardItem.amount
            val type = rewardItem.type
            Log.d(TAG, "User earned reward: $amount $type")
            usageTracker.grantReward()
            onRewardEarned()
        }
    }

    /** Whether an ad is loaded and ready to display right now. */
    fun hasAdReady(): Boolean = rewardedAd != null

    /**
     * Releases the current ad and stops loading. Called on app teardown.
     */
    fun release() {
        rewardedAd = null
        isLoading = false
        _isAdReady.value = false
        _isAdLoading.value = false
    }
}
