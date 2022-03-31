package com.roland.android.jotter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetDialog : BottomSheetDialogFragment() {
    private lateinit var darkText: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.jotter_bottom_sheet, container, false)
        darkText = view.findViewById(R.id.night_mode)
        darkText.setOnClickListener {
            var isActivated = it.isActivated
            isActivated = !isActivated
            if (isActivated){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            QueryPreference.setDarkMode(requireContext(), isActivated)
        }
        return view
    }

    companion object {
        const val TAG = "BottomSheetDialog"
    }
}