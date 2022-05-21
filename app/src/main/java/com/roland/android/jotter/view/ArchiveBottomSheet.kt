package com.roland.android.jotter.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.roland.android.jotter.R
import com.roland.android.jotter.util.Preference

class ArchiveBottomSheet : BottomSheetDialogFragment() {
    private lateinit var lock: SwitchMaterial
    private lateinit var lockText: TextView
    private lateinit var lockField: View
    private lateinit var changePin: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.archive_bottom_sheet, container, false)
        val locked = Preference.getLockState(requireContext())
        lockText = view.findViewById(R.id.lock_text)
        lockField = view.findViewById(R.id.lock_field)
        if (locked) {
            lockText.text = getString(R.string.archive_locked_text)
            lockField.contentDescription = getString(R.string.unlock_archive)
        } else {
            lockText.text = getString(R.string.archive_not_locked_text)
            lockField.contentDescription = getString(R.string.lock_archive)
        }
        lockField.setOnClickListener {
            lock.isChecked = !lock.isChecked
        }
        lock = view.findViewById(R.id.lock_archive)
        lock.isChecked = Preference.getLockState(requireContext())
        lock.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                lockText.text = getString(R.string.archive_locked_text)
                Preference.setLockState(requireContext(), true)
            } else {
                lockText.text = getString(R.string.archive_not_locked_text)
                Preference.setLockState(requireContext(), false)
            }
        }
        changePin = view.findViewById(R.id.change_pin)
        changePin.setOnClickListener {
            val action = ArchiveBottomSheetDirections.archiveBottomSheetToArchiveLock(changePassword = true)
            findNavController().navigate(action)
        }
        return view
    }
}