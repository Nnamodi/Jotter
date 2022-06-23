package com.roland.android.jotter.util

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

object Preference {
    private const val DARK_MODE = "isDark"
    private const val LOCKED = "locked"
    private const val SECURED = "back_button_config"
    private const val PIN = "pin"
    private const val TEXT_SIZE = "text_size"

    fun getDarkMode(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(DARK_MODE, false)
    }

    fun setDarkMode(context: Context, isDark: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putBoolean(DARK_MODE, isDark)
        }
    }

    fun getLockState(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(LOCKED, false)
    }

    fun setLockState(context: Context, locked: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putBoolean(LOCKED, locked)
        }
    }

    fun getLock(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(SECURED, false)
    }

    fun setLock(context: Context, configBackButton: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putBoolean(SECURED, configBackButton)
        }
    }

    fun getPIN(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(PIN, "")!!
    }

    fun setPIN(context: Context, pin: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(PIN, pin)
        }
    }

    fun getSize(context: Context): Int {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getInt(TEXT_SIZE, 18)
    }

    fun setSize(context: Context, textSize: Int) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putInt(TEXT_SIZE, textSize)
        }
    }
}