package com.roland.android.jotter

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

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
            findNavController().navigate(R.id.jotFragment)
        }
        jotterRecyclerView.adapter = adapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        jotterViewModel.getNotes.observe(
            viewLifecycleOwner,
            { note ->
                Log.d("JotterFragment", "Notes received: $note")
                (jotterRecyclerView.adapter as JotterAdapter).submitList(note)
                Toast.makeText(context, "You have ${note.size} notes", Toast.LENGTH_LONG).show()
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_jot_fragment, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.more_options -> {
                // invoke bottom sheet
                findNavController().navigate(R.id.bottomSheetDialog)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private class JotterHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private lateinit var note: Note
        private lateinit var noteTitle: TextView
        private lateinit var noteBody: TextView

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(note: Note) {
            this.note = note
            noteTitle = itemView.findViewById(R.id.note_title)
            noteTitle.text = note.title
            noteBody = itemView.findViewById(R.id.note_body)
            noteBody.text = note.body
        }

        override fun onClick(view: View) {
            Navigation.createNavigateOnClickListener(R.id.move_to_jot, null)
        }
    }

    private inner class JotterAdapter : ListAdapter<Note, JotterHolder>(DiffCallBack()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JotterHolder {
            val view = layoutInflater.inflate(R.layout.fragment_jotter_list, parent, false)
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