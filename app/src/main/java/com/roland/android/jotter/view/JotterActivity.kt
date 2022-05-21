package com.roland.android.jotter.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.roland.android.jotter.R

class JotterActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
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
                }
                R.id.jotterBottomSheet -> {
                    toolbar.isTitleCentered = true
                }
                R.id.jotBottomSheet -> {
                    toolbar.isTitleCentered = false
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                }
                R.id.archiveBottomSheet -> {
                    toolbar.isTitleCentered = false
                    title = "Archive"
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                }
                else -> {
                    toolbar.isTitleCentered = false
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