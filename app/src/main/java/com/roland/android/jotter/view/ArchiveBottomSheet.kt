package com.roland.android.jotter.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.roland.android.jotter.R
import com.roland.android.jotter.util.Preference

class ArchiveBottomSheet : BottomSheetDialogFragment() {
    private lateinit var lock: SwitchMaterial
    private lateinit var lockText: TextView
    private lateinit var changePinText: TextView
    private lateinit var lockField: View
    private lateinit var changePin: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.archive_bottom_sheet, container, false)
        val locked = Preference.getLockState(requireContext())
        val pinSet = Preference.getPIN(requireContext()).isNotEmpty()
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
            if (pinSet) { lock.isChecked = !lock.isChecked }
            else {
                val dialog = AlertDialog.Builder(requireContext())
                dialog.setMessage(getString(R.string.lock_dialog_message))
                    .setPositiveButton(getString(R.string.lock_dialog_pos_button)) { _, _ ->
                        val action = ArchiveBottomSheetDirections.archiveBottomSheetToArchiveLock(changePassword = "set")
                        findNavController().navigate(action)
                    }
                    .setNegativeButton(getString(R.string.lock_dialog_neg_button)) { _, _ -> }
                    .setCancelable(false)
                    .create().show()
            }
        }
        lock = view.findViewById(R.id.lock_archive)
        lock.isChecked = Preference.getLockState(requireContext())
        lock.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                lockText.text = getString(R.string.archive_locked_text)
                lockField.contentDescription = getString(R.string.unlock_archive)
                Preference.setLockState(requireContext(), true)
            } else {
                lockText.text = getString(R.string.archive_not_locked_text)
                lockField.contentDescription = getString(R.string.lock_archive)
                Preference.setLockState(requireContext(), false)
            }
        }
        changePinText = view.findViewById(R.id.change_pin_text)
        changePin = view.findViewById(R.id.change_pin)
        changePin.setOnClickListener {
            val action = if (pinSet) {
                ArchiveBottomSheetDirections
                    .archiveBottomSheetToArchiveLock(changePassword = "change")
            } else {
                ArchiveBottomSheetDirections
                    .archiveBottomSheetToArchiveLock(changePassword = "set")
            }
            findNavController().navigate(action)
        }
        if (!pinSet) {
            changePinText.text = getString(R.string.set_pin)
        }
        return view
    }
}