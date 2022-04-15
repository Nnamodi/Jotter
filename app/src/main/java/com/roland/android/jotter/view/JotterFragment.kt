package com.roland.android.jotter.view

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.roland.android.jotter.util.Preference
import com.roland.android.jotter.R
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.viewModel.JotterViewModel
import java.text.SimpleDateFormat
import java.util.*

class JotterFragment : Fragment() {
    private lateinit var jot: View
    private lateinit var jotterRecyclerView: RecyclerView
    private lateinit var jotterViewModel: JotterViewModel
    private var adapter = JotterAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        val isDark = Preference.getDarkMode(requireContext())
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        super.onCreate(savedInstanceState)
        jotterViewModel = ViewModelProvider(this) [JotterViewModel::class.java]
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_jotter_list, container, false)
        jotterRecyclerView = view.findViewById(R.id.recycler_view)
        jot = view.findViewById(R.id.jot)
        jot.setOnClickListener {
            val action = JotterFragmentDirections.moveIntoEditing(null)
            findNavController().navigate(action)
        }
        jotterRecyclerView.adapter = adapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        jotterViewModel.getNotes.observe(
            viewLifecycleOwner
        ) { note ->
            Log.d("JotterFragment", "Notes received: $note")
            (jotterRecyclerView.adapter as JotterAdapter).submitList(note)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_jotter_fragment, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.more_options -> {
                // invoke bottom sheet
                findNavController().navigate(R.id.jotterBottomSheet)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private class JotterHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private lateinit var note: Note
        private lateinit var noteTitle: TextView
        private lateinit var noteBody: TextView
        private lateinit var dateText: TextView

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(note: Note) {
            this.note = note
            noteTitle = itemView.findViewById(R.id.jot_title)
            noteTitle.text = note.title
            noteBody = itemView.findViewById(R.id.jot_body)
            noteBody.text = note.body
            dateText = itemView.findViewById(R.id.date_text)
            dateText.text = SimpleDateFormat("d|M|yy", Locale.getDefault()).format(note.date)
        }

        override fun onClick(view: View) {
            val action = JotterFragmentDirections.moveToJot(note)
            findNavController(view).navigate(action)
        }
    }

    private inner class JotterAdapter : ListAdapter<Note, JotterHolder>(DiffCallBack()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JotterHolder {
            val view = layoutInflater.inflate(R.layout.fragment_jotter, parent, false)
            return JotterHolder(view)
        }

        override fun onBindViewHolder(holder: JotterHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }

    private class DiffCallBack : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
}