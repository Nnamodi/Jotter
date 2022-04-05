package com.roland.android.jotter

import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController

class JotEditFragment : Fragment() {
    private lateinit var noteTitle: TextView
    private lateinit var noteBody: TextView
    private lateinit var jotViewModel: JotterViewModel
    private val note = Note()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        jotViewModel = ViewModelProvider(this) [JotterViewModel::class.java]
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_jot_edit, container, false)
        noteTitle = view.findViewById(R.id.edit_title)
        noteTitle.text = note.title
        noteBody = view.findViewById(R.id.edit_body)
        noteBody.text = note.body
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
                note.title = noteTitle.text.toString()
                note.body = noteBody.text.toString()
                jotViewModel.addNotes(note)
                Toast.makeText(context, "Note [$note] saved.", Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}