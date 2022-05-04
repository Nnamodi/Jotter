package com.roland.android.jotter.view

import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.roland.android.jotter.R
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.viewModel.JotterViewModel
import java.util.*

class JotEditFragment : Fragment() {
    private lateinit var noteTitle: TextView
    private lateinit var noteBody: TextView
    private lateinit var jotViewModel: JotterViewModel
    private val args by navArgs<JotEditFragmentArgs>()
    private val note = Note()
    private var noteIsNew = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.menu_cancel)
        jotViewModel = ViewModelProvider(this) [JotterViewModel::class.java]
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_jot_edit, container, false)
        noteTitle = view.findViewById(R.id.edit_title)
        noteTitle.text = args.edit?.title
        noteBody = view.findViewById(R.id.edit_body)
        noteBody.text = args.edit?.body
        noteIsNew = noteTitle.text.isEmpty() && noteBody.text.isEmpty()
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_jot_edit, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save_jot -> {
                // save jot
                if (!noteIsNew) {
                    updateNote()
                } else {
                    saveNote()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveNote() {
        if (noteTitle.text.isNotEmpty() || noteBody.text.isNotEmpty()) {
            note.title = noteTitle.text.toString()
            note.body = noteBody.text.toString()
            note.date = Calendar.getInstance().time
            jotViewModel.addNotes(note)
            findNavController().navigateUp()
            Toast.makeText(context, getString(R.string.save_note_text), Toast.LENGTH_SHORT).show()
        } else {
            findNavController().navigateUp()
            Toast.makeText(context, getString(R.string.saving_empty_note_text), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateNote() {
        val titleIsSame = noteTitle.text.contentEquals(args.edit?.title)
        val bodyIsSame = noteBody.text.contentEquals(args.edit?.body)
        if (noteTitle.text.isNotEmpty() || noteBody.text.isNotEmpty()) {
            note.id = args.edit?.id!!
            note.title = noteTitle.text.toString()
            note.body = noteBody.text.toString()
            note.date = Calendar.getInstance().time
            if (titleIsSame && bodyIsSame) {
                findNavController().navigateUp()
            } else {
                jotViewModel.updateNote(note)
                val action = JotEditFragmentDirections.actionJotEditToJot(note)
                findNavController().navigate(action)
                Toast.makeText(context, getString(R.string.note_updated_text), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, getString(R.string.note_not_updated_text), Toast.LENGTH_SHORT).show()
        }
    }
}