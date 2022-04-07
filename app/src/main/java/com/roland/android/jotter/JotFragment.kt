package com.roland.android.jotter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.text.SimpleDateFormat
import java.util.*

class JotFragment : Fragment() {
    private lateinit var edit: View
    private lateinit var noteTitle: TextView
    private lateinit var noteBody: TextView
    private lateinit var date: TextView
    private lateinit var time: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_jot, container, false)
        val note = Note()
        edit = view.findViewById(R.id.edit)
        edit.setOnClickListener {
            findNavController().navigate(R.id.move_to_edit, null)
        }
        noteTitle = view.findViewById(R.id.note_title)
        noteTitle.text = note.title
        noteBody = view.findViewById(R.id.note_body)
        noteBody.text = note.body
        date = view.findViewById(R.id.date)
        date.text = SimpleDateFormat("d|M|yy", Locale.getDefault()).format(note.date)
        time = view.findViewById(R.id.time)
        time.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(note.time)
        Log.d("JotterFragment", "JotFragment note [$note] saved.")
        return view
    }
}