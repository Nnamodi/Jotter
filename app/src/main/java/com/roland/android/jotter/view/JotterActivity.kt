package com.roland.android.jotter.view

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.roland.android.jotter.R
import com.roland.android.jotter.util.Preference

class JotterActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        val isDark = Preference.getDarkMode(this)
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jot)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val host = supportFragmentManager
            .findFragmentById(R.id.activity_jotter) as NavHostFragment
        navController = host.navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.jotterFragment -> {
                    toolbar.isTitleCentered = true
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
                R.id.jotterBottomSheet -> {
                    toolbar.isTitleCentered = true
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
                R.id.archiveLock -> {
                    toolbar.isTitleCentered = false
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
                else -> {
                    toolbar.isTitleCentered = false
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }
        }
        setupActionBarWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val archiveFragment = navController.findDestination(R.id.archiveFragment)
        return if (navController.currentDestination != archiveFragment) {
            navController.navigateUp()
        } else {
            onBackPressed()
            true
        }
    }
}