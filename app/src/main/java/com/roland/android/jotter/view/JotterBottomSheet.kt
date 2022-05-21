package com.roland.android.jotter.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.roland.android.jotter.R
import com.roland.android.jotter.util.Preference

class JotterBottomSheet : BottomSheetDialogFragment() {
    private lateinit var archive: View
    private lateinit var checkView: View
    private lateinit var darkMode: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.jotter_bottom_sheet, container, false)
        val isDark = Preference.getDarkMode(requireContext())
        val archiveLocked = Preference.getLockState(requireContext())
        archive = view.findViewById(R.id.archive)
        archive.setOnClickListener {
            if (archiveLocked) {
                findNavController().navigate(R.id.archiveLock)
                Preference.setLock(requireContext(), true)
            } else {
                findNavController().navigate(R.id.archiveFragment)
                Preference.setLock(requireContext(), false)
            }
        }
        darkMode = view.findViewById(R.id.night_mode)
        checkView = view.findViewById(R.id.check_view)
        checkView.setOnClickListener {
            if (isDark) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                Preference.setDarkMode(requireContext(), false)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                Preference.setDarkMode(requireContext(), true)
            }
        }
        if (isDark) {
            darkMode.text = getString(R.string.light_mode)
            darkMode.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.light_icon, 0)
            checkView.contentDescription = getString(R.string.switch_to_light)
        } else {
            darkMode.text = getString(R.string.night_mode)
            darkMode.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.night_icon, 0)
            checkView.contentDescription = getString(R.string.switch_to_night)
        }
        return view
    }
}