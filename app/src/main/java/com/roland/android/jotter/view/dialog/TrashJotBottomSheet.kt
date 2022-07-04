package com.roland.android.jotter.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.roland.android.jotter.R
import com.roland.android.jotter.databinding.TrashJotBottomSheetBinding
import com.roland.android.jotter.viewModel.TrashViewModel

class TrashJotBottomSheet : BottomSheetDialogFragment() {
    private lateinit var viewModel: TrashViewModel
    private var _binding: TrashJotBottomSheetBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<TrashJotBottomSheetArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = TrashJotBottomSheetBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this) [TrashViewModel::class.java]
        val deleteDialog = {
            val dialog = MaterialAlertDialogBuilder(requireContext())
            dialog.setTitle(getString(R.string.delete_permanently_))
                .setIcon(R.drawable.dialog_delete_icon)
                .setMessage(getString(R.string.delete_permanently_dialog))
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    viewModel.deleteNote(args.trash)
                    findNavController().popBackStack(R.id.jotFragment, true)
                    Toast.makeText(requireContext(), getString(R.string.deleted), Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(getString(R.string.no)) { _, _ -> }
                .show()
        }
        binding.restoreNote.setOnClickListener {
            viewModel.trashNote(args.trash, archive = false, trash = false)
            findNavController().popBackStack(R.id.jotFragment, true)
            Toast.makeText(requireContext(), getString(R.string.restored), Toast.LENGTH_SHORT).show()
        }
        binding.deleteNote.setOnClickListener {
            deleteDialog()
        }
        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) { _binding = null }
        })
        return binding.root
    }
}