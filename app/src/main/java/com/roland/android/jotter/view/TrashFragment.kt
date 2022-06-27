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
import com.roland.android.jotter.viewModel.TrashViewModel
import java.text.SimpleDateFormat
import java.util.*

class TrashFragment : Fragment() {
    private lateinit var trashRecyclerView: RecyclerView
    private lateinit var trashViewModel: TrashViewModel
    private lateinit var trashEmptyText: View
    private var adapter = TrashAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        trashViewModel = ViewModelProvider(this) [TrashViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_trash, container, false)
        trashRecyclerView = view.findViewById(R.id.trash_recycler_view)
        trashEmptyText = view.findViewById(R.id.trash_empty_text)
        trashRecyclerView.adapter = adapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        trashViewModel.getTrashedNotes.observe(
            viewLifecycleOwner
        ) { trash ->
            Log.d("TrashFragment", "Received trashed notes: $trash")
            adapter.submitList(trash)
            if (trash.isEmpty()) {
                trashRecyclerView.visibility = View.GONE
                trashEmptyText.visibility = View.VISIBLE
            } else {
                trashRecyclerView.visibility = View.VISIBLE
                trashEmptyText.visibility = View.GONE
            }
        }
    }

    private inner class TrashHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
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
            val action = TrashFragmentDirections.trashToJot(note)
            findNavController().navigate(action)
        }
    }

    private inner class TrashAdapter : ListAdapter<Note, TrashHolder>(DiffCallback()) {
         override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrashHolder {
            val view = layoutInflater.inflate(R.layout.jotter_item, parent, false)
            return TrashHolder(view)
        }

        override fun onBindViewHolder(holder: TrashHolder, position: Int) {
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