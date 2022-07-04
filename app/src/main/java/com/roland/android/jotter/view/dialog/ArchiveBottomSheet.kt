package com.roland.android.jotter.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.roland.android.jotter.R
import com.roland.android.jotter.databinding.ArchiveBottomSheetBinding
import com.roland.android.jotter.util.Preference

class ArchiveBottomSheet : BottomSheetDialogFragment() {
    private var _binding: ArchiveBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.archive)
        _binding = ArchiveBottomSheetBinding.inflate(inflater, container, false)
        val locked = Preference.getLockState(requireContext())
        val pinSet = Preference.getPIN(requireContext()).isNotEmpty()
        if (locked) {
            binding.lockText.text = getString(R.string.archive_locked_text)
            binding.lockField.contentDescription = getString(R.string.unlock_archive)
        } else {
            binding.lockText.text = getString(R.string.archive_not_locked_text)
            binding.lockField.contentDescription = getString(R.string.lock_archive)
        }
        binding.lockField.setOnClickListener {
            if (pinSet) { binding.lock.isChecked = !binding.lock.isChecked }
            else {
                val dialog = MaterialAlertDialogBuilder(requireContext())
                dialog.setTitle(R.string.archive_lock)
                    .setIcon(R.drawable.lock_icon)
                    .setMessage(getString(R.string.lock_dialog_message))
                    .setPositiveButton(getString(R.string.lock_dialog_pos_button)) { _, _ ->
                        val action = ArchiveBottomSheetDirections.archiveBottomSheetToArchiveLock(changePassword = "set")
                        findNavController().navigate(action)
                    }
                    .setNegativeButton(getString(R.string.lock_dialog_neg_button)) { _, _ -> }
                    .setCancelable(false)
                    .show()
            }
        }
        binding.lock.isChecked = Preference.getLockState(requireContext())
        binding.lock.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                binding.lockText.text = getString(R.string.archive_locked_text)
                binding.lockField.contentDescription = getString(R.string.unlock_archive)
                Preference.setLockState(requireContext(), true)
            } else {
                binding.lockText.text = getString(R.string.archive_not_locked_text)
                binding.lockField.contentDescription = getString(R.string.lock_archive)
                Preference.setLockState(requireContext(), false)
            }
        }
        binding.changePin.setOnClickListener {
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
            binding.changePinText.text = getString(R.string.set_pin)
        }
        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) { _binding = null }
        })
        return binding.root
    }
}