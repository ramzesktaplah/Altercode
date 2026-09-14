package com.ai.altercode

import android.app.Application
import com.ai.altercode.ads.RewardedAdManager
import com.ai.altercode.data.UsageTracker
import com.google.android.gms.ads.MobileAds

class AlterCodeApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Load SQLCipher native library before any database operation.
        runCatching { System.loadLibrary("sqlcipher") }

        ServiceLocator.init(this)

        // Initialize the Google Mobile Ads SDK. This is a lightweight call
        // that schedules full initialization on a background thread.
        MobileAds.initialize(this) {
            // Preload a rewarded ad so it's ready when the free-runs limit is hit.
            ServiceLocator.rewardedAds.loadAd()
        }
    }
}
