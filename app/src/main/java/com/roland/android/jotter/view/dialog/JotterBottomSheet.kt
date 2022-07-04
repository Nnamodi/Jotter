package com.roland.android.jotter.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.roland.android.jotter.R
import com.roland.android.jotter.databinding.JotterBottomSheetBinding
import com.roland.android.jotter.util.Preference

class JotterBottomSheet : BottomSheetDialogFragment() {
    private var _binding: JotterBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = JotterBottomSheetBinding.inflate(inflater, container, false)
        val isDark = Preference.getDarkMode(requireContext())
        val archiveLocked = Preference.getLockState(requireContext())
        binding.apply {
            archive.setOnClickListener {
                if (archiveLocked) {
                    findNavController().navigate(R.id.archiveLock)
                    Preference.setLock(requireContext(), true)
                } else {
                    findNavController().navigate(R.id.archiveFragment)
                    Preference.setLock(requireContext(), false)
                }
            }
            darKModeView.setOnClickListener {
                nightMode.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.loading_icon, 0)
                if (isDark) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    Preference.setDarkMode(requireContext(), false)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    Preference.setDarkMode(requireContext(), true)
                }
            }
            trash.setOnClickListener {
                findNavController().navigate(R.id.trashFragment)
            }
            if (isDark) {
                nightMode.text = getString(R.string.light_mode)
                nightMode.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.light_icon, 0)
                darKModeView.contentDescription = getString(R.string.switch_to_light)
            } else {
                nightMode.text = getString(R.string.night_mode)
                nightMode.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.night_icon, 0)
                darKModeView.contentDescription = getString(R.string.switch_to_night)
            }
        }
        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) { _binding = null }
        })
        return binding.root
    }
}