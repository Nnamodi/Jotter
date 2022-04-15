package com.roland.android.jotter.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.roland.android.jotter.R
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.viewModel.JotterViewModel

class JotBottomSheet : BottomSheetDialogFragment() {
    private lateinit var deleteNote: View
    private lateinit var shareNote: View
    private lateinit var viewModel: JotterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this) [JotterViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.jot_bottom_sheet, container, false)
        val args by navArgs<JotBottomSheetArgs>()
        deleteNote = view.findViewById(R.id.delete_note)
        deleteNote.setOnClickListener {
            deleteNote(args.utils)
            dismiss()
        }
        shareNote = view.findViewById(R.id.share_note)
        shareNote.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getString(R.string.text, args.utils.title, args.utils.body))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject_text))
            }.also { intent ->
                val chooserIntent = Intent.createChooser(intent, null)
                startActivity(chooserIntent)
            }
        }
        return view
    }

    private fun deleteNote(note: Note) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(getString(R.string.delete_message, note.title))
        builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
            viewModel.deleteNote(note)
            findNavController().popBackStack()
            Toast.makeText(context, getString(R.string.note_deleted_text), Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton(getString(R.string.no)) { _, _ -> }
        builder.create().show()
    }
}