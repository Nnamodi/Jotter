package com.roland.android.jotter

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

object QueryPreference {
    private const val DARK_MODE = "isDark"

    fun getDarkMode(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(DARK_MODE, false)
    }

    fun setDarkMode(context: Context, isDark: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putBoolean(DARK_MODE, isDark)
        }
    }
}