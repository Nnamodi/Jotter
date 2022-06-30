package com.roland.android.jotter.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.roland.android.jotter.R
import com.roland.android.jotter.viewModel.TrashViewModel

class TrashJotBottomSheet : BottomSheetDialogFragment() {
    private lateinit var viewModel: TrashViewModel
    private lateinit var restoreNote: View
    private lateinit var deleteNote: View
    private val args by navArgs<TrashJotBottomSheetArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this) [TrashViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.trash_jot_bottom_sheet, container, false)
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
        restoreNote = view.findViewById(R.id.restore_note)
        restoreNote.setOnClickListener {
            viewModel.trashNote(args.trash, archive = false, trash = false)
            findNavController().popBackStack(R.id.jotFragment, true)
            Toast.makeText(requireContext(), getString(R.string.restored), Toast.LENGTH_SHORT).show()
        }
        deleteNote = view.findViewById(R.id.delete_note)
        deleteNote.setOnClickListener {
            deleteDialog()
        }
        return view
    }
}