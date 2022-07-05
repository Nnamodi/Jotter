package com.roland.android.jotter.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatDelegate
import com.roland.android.jotter.util.Preference

class Launch : ComponentActivity() {
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var notInterrupted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val isDark = Preference.getDarkMode(this)
        val mode = if (isDark) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
        super.onCreate(savedInstanceState)
        notInterrupted = true
        splash()
    }

    override fun onResume() {
        super.onResume()
        if (!notInterrupted) { handler.postDelayed(runnable, 100) }
    }

    override fun onStop() {
        super.onStop()
        notInterrupted = false
        handler.removeCallbacks(runnable)
    }

    private fun splash() {
        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            val intent = Intent(this, JotterActivity::class.java)
            startActivity(intent)
            finish()
        }
        handler.postDelayed(runnable, 1000)
    }
}