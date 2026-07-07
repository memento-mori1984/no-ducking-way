package com.example.noduckingway

import android.content.Context

object NoDuckingPrefs {
    const val PREFS_NAME = "noducking_prefs"
    const val KEY_MODE = "mode"
    const val KEY_START_ON_BOOT = "start_on_boot"
    const val KEY_BATTERY_PROMPT_SHOWN = "battery_prompt_shown"

    const val MODE_MIXER = "mixer"
    const val MODE_OWNER = "owner"

    fun getMode(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_MODE, MODE_MIXER) ?: MODE_MIXER
    }

    fun setMode(context: Context, mode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MODE, mode)
            .apply()
    }

    fun isMixerMode(context: Context): Boolean = getMode(context) == MODE_MIXER

    fun isStartOnBootEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_START_ON_BOOT, false)
    }

    fun setStartOnBoot(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_START_ON_BOOT, enabled)
            .apply()
    }

    fun wasBatteryPromptShown(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_BATTERY_PROMPT_SHOWN, false)
    }

    fun setBatteryPromptShown(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_BATTERY_PROMPT_SHOWN, true)
            .apply()
    }
}