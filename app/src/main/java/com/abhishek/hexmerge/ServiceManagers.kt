package com.abhishek.hexmerge

import android.media.AudioManager
import android.media.ToneGenerator

class GameAudioManager {
    private val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 55)
    var sfxEnabled = true
    fun click() { if (sfxEnabled) tone.startTone(ToneGenerator.TONE_PROP_BEEP, 70) }
    fun place() { if (sfxEnabled) tone.startTone(ToneGenerator.TONE_PROP_ACK, 80) }
    fun invalid() { if (sfxEnabled) tone.startTone(ToneGenerator.TONE_PROP_NACK, 90) }
    fun merge(high: Boolean = false) { if (sfxEnabled) tone.startTone(if (high) ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD else ToneGenerator.TONE_PROP_BEEP, if (high) 180 else 90) }
    fun success() { if (sfxEnabled) tone.startTone(ToneGenerator.TONE_PROP_PROMPT, 220) }
}

class AnalyticsManager {
    var enabled = false
    fun event(name: String, params: Map<String, Any> = emptyMap()) { if (enabled) android.util.Log.d("HexMergeAnalytics", "$name $params") }
}

class AdManager {
    fun isRewardedAvailable() = false
    fun showRewarded(onReward: () -> Unit, onUnavailable: () -> Unit = {}) { onUnavailable() }
    fun showInterstitial() {}
}
