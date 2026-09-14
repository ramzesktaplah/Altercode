package com.ai.altercode

import android.content.Context
import com.ai.altercode.ads.RewardedAdManager
import com.ai.altercode.ai.AiCodeService
import com.ai.altercode.data.DeviceFingerprint
import com.ai.altercode.data.EditorSession
import com.ai.altercode.data.SettingsRepository
import com.ai.altercode.data.SnippetRepository
import com.ai.altercode.data.UsageTracker

/** Manual dependency container so view models can be constructed without a DI graph. */
object ServiceLocator {

    lateinit var appContext: Context
        private set
    lateinit var snippets: SnippetRepository
        private set
    lateinit var settings: SettingsRepository
        private set
    lateinit var editorSession: EditorSession
        private set
    lateinit var ai: AiCodeService
        private set
    lateinit var usageTracker: UsageTracker
        private set
    lateinit var rewardedAds: RewardedAdManager
        private set
    lateinit var deviceFingerprint: DeviceFingerprint
        private set

    fun init(context: Context) {
        if (::appContext.isInitialized) return
        appContext = context.applicationContext
        snippets = SnippetRepository(appContext)
        settings = SettingsRepository(appContext)
        editorSession = EditorSession()
        deviceFingerprint = DeviceFingerprint(appContext)
        ai = AiCodeService(deviceId = deviceFingerprint.deviceId)
        usageTracker = UsageTracker(appContext)
        rewardedAds = RewardedAdManager(appContext, usageTracker)
    }
}
