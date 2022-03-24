package com.roland.android.jotter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class JotEditFragment : Fragment() {
    private lateinit var noteTitle: TextView
    private lateinit var noteBody: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_jot_edit, container, false)
        val note = Note()
        noteTitle = view.findViewById(R.id.note_title)
        noteTitle.text = note.title
        noteBody = view.findViewById(R.id.note_body)
        noteBody.text = note.body
        return view
    }
}