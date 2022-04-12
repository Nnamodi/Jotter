package com.roland.android.jotter.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.roland.android.jotter.util.Preference
import com.roland.android.jotter.R

class BottomSheetDialog : BottomSheetDialogFragment() {
    private lateinit var darkMode: CheckBox

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.jotter_bottom_sheet, container, false)
        darkMode = view.findViewById(R.id.night_mode)
        val isDark = Preference.getDarkMode(requireContext())
        darkMode.isChecked = isDark
        if (isDark) {
            darkMode.text = getString(R.string.light_mode)
            darkMode.setButtonDrawable(R.drawable.light_icon)
        } else {
            darkMode.text = getString(R.string.night_mode)
            darkMode.setButtonDrawable(R.drawable.night_icon)
        }
        darkMode.setOnCheckedChangeListener { switch, isChecked ->
            if (switch.isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            Preference.setDarkMode(requireContext(), isChecked)
            Log.i("DarkActivated", "$isChecked")
        }
        return view
    }
}