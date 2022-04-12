package com.roland.android.jotter.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.roland.android.jotter.R
import com.roland.android.jotter.model.Note
import java.text.SimpleDateFormat
import java.util.*

class JotFragment : Fragment() {
    private lateinit var note : Note
    private lateinit var edit: View
    private lateinit var noteTitle: TextView
    private lateinit var noteBody: TextView
    private lateinit var date: TextView
    private lateinit var time: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_jot, container, false)
        val args by navArgs<JotFragmentArgs>()
        note = Note()
        edit = view.findViewById(R.id.edit)
        edit.setOnClickListener {
            val action = JotFragmentDirections.moveToEdit(args.note)
            findNavController().navigate(action)
        }
        noteTitle = view.findViewById(R.id.note_title)
        noteTitle.text = args.note.title
        noteBody = view.findViewById(R.id.note_body)
        noteBody.text = args.note.body
        date = view.findViewById(R.id.date)
        date.text = SimpleDateFormat("d|M|yy", Locale.getDefault()).format(args.note.date)
        time = view.findViewById(R.id.time)
        time.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(args.note.time)
        return view
    }
}