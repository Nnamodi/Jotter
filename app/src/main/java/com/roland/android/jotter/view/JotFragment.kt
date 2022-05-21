package com.roland.android.jotter.view

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.roland.android.jotter.R
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.viewModel.JotterViewModel
import java.text.SimpleDateFormat
import java.util.*

class JotFragment : Fragment() {
    private lateinit var note : Note
    private lateinit var edit: View
    private lateinit var noteTitle: TextView
    private lateinit var noteBody: TextView
    private lateinit var date: TextView
    private lateinit var time: TextView
    private lateinit var viewModel: JotterViewModel
    private val args by navArgs<JotFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this) [JotterViewModel::class.java]
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_jot, container, false)
        (activity as AppCompatActivity).supportActionBar?.title = args.note.title
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
        time.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(args.note.date)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_jot, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.more_options -> {
                val action = JotFragmentDirections.jotFragmentToJotBottomSheet(args.note)
                findNavController().navigate(action)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}