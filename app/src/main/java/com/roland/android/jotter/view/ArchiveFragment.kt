package com.roland.android.jotter.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.roland.android.jotter.R
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.viewModel.ArchiveViewModel
import java.text.SimpleDateFormat
import java.util.*

class ArchiveFragment : Fragment() {
    private lateinit var archiveViewModel: ArchiveViewModel
    private lateinit var archiveRecyclerView: RecyclerView
    private lateinit var archiveEmptyText: TextView
    private var adapter = ArchiveAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        archiveViewModel = ViewModelProvider(this) [ArchiveViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_archive, container, false)
        archiveRecyclerView = view.findViewById(R.id.archive_recycler_view)
        archiveEmptyText = view.findViewById(R.id.archive_empty_text)
        archiveRecyclerView.adapter = adapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        archiveViewModel.getArchivedNotes.observe(
            viewLifecycleOwner
        ) { note ->
            Log.d("ArchiveFragment", "Received archived notes: $note")
            (archiveRecyclerView.adapter as ArchiveAdapter).submitList(note)
            if (note.isEmpty()) {
                archiveRecyclerView.visibility = View.GONE
                archiveEmptyText.visibility = View.VISIBLE
            } else {
                archiveRecyclerView.visibility = View.VISIBLE
                archiveEmptyText.visibility = View.GONE
            }
        }
    }

    private inner class ArchiveHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
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
            val action = ArchiveFragmentDirections.archiveToJot(note)
            findNavController().navigate(action)
        }
    }

    private inner class ArchiveAdapter : ListAdapter<Note, ArchiveHolder>(DiffCallback()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchiveHolder {
            val view = layoutInflater.inflate(R.layout.fragment_jotter, parent, false)
            return ArchiveHolder(view)
        }

        override fun onBindViewHolder(holder: ArchiveHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Note>()  {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
}