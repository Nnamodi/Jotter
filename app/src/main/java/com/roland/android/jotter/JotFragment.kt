package com.roland.android.jotter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation

class JotFragment : Fragment() {
    private lateinit var edit: View
    private lateinit var noteTitle: TextView
    private lateinit var noteBody: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_jot, container, false)
        val note = Note()
        edit = view.findViewById(R.id.edit)
        edit.setOnClickListener {
            Navigation.createNavigateOnClickListener(R.id.move_to_edit, null)
        }
        noteTitle = view.findViewById(R.id.note_title)
        noteTitle.text = note.title
        noteBody = view.findViewById(R.id.note_body)
        noteBody.text = note.body
        return view
    }
}