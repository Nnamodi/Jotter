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
    private lateinit var archiveNote: View
    private lateinit var unarchiveNote: View
    private lateinit var viewModel: JotterViewModel
    private val args by navArgs<JotBottomSheetArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this) [JotterViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.jot_bottom_sheet, container, false)
        deleteNote = view.findViewById(R.id.delete_note)
        deleteNote.setOnClickListener {
            deleteNote(args.utils)
        }
        shareNote = view.findViewById(R.id.share_note)
        shareNote.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getString(R.string.text, args.utils.title, args.utils.body))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject_text))
            }.also { intent ->
                val chooserIntent = Intent.createChooser(intent, getString(R.string.chooser_title))
                startActivity(chooserIntent)
            }
        }
        archiveNote = view.findViewById(R.id.archive_note)
        archiveNote.setOnClickListener {
            viewModel.archiveNote(args.utils, true)
            findNavController().popBackStack(R.id.jotFragment, true)
            Toast.makeText(context, getString(R.string.jot_archived, args.utils.title), Toast.LENGTH_SHORT).show()
        }
        unarchiveNote = view.findViewById(R.id.unarchive_note)
        unarchiveNote.setOnClickListener {
            viewModel.archiveNote(args.utils, false)
            findNavController().popBackStack(R.id.jotFragment, true)
            Toast.makeText(context, getString(R.string.jot_unarchived, args.utils.title), Toast.LENGTH_SHORT).show()
        }
        if (args.utils.archived) {
            archiveNote.visibility = View.GONE
            unarchiveNote.visibility = View.VISIBLE
        } else {
            archiveNote.visibility = View.VISIBLE
            unarchiveNote.visibility = View.GONE
        }
        return view
    }

    override fun onDestroy() {
        val title = args.utils.title
        findNavController().previousBackStackEntry?.savedStateHandle?.set("title", title)
        super.onDestroy()
    }

    private fun deleteNote(note: Note) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(getString(R.string.delete_message, note.title))
        builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
            viewModel.deleteNote(note)
            findNavController().popBackStack(R.id.jotFragment, true)
            Toast.makeText(context, getString(R.string.note_deleted_text), Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton(getString(R.string.no)) { _, _ -> }
        builder.create().show()
    }
}