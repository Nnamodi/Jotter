package com.roland.android.jotter.view

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.roland.android.jotter.R
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.util.Preference
import com.roland.android.jotter.viewModel.ArchiveViewModel
import java.text.SimpleDateFormat
import java.util.*

class ArchiveFragment : Fragment() {
    private lateinit var archiveViewModel: ArchiveViewModel
    private lateinit var archiveRecyclerView: RecyclerView
    private lateinit var archiveEmptyText: View
    private var adapter = ArchiveAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        archiveViewModel = ViewModelProvider(this) [ArchiveViewModel::class.java]
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_archive, container, false)
        val navBackStackEntry = findNavController().getBackStackEntry(R.id.archiveFragment)
        archiveRecyclerView = view.findViewById(R.id.archive_recycler_view)
        archiveEmptyText = view.findViewById(R.id.archive_empty_text)
        archiveRecyclerView.adapter = adapter
        if (Preference.getLock(requireContext())) {
            activity?.onBackPressedDispatcher?.addCallback(this) {
                findNavController().navigate(R.id.back_to_jotterFragment)
            }
        }
        navBackStackEntry.savedStateHandle.getLiveData<String>("PIN").observe(
            viewLifecycleOwner
        ) { set ->
            when (set) {
                "set" -> {
                    snackbar(requireView(), getString(R.string.pin_set_text))
                }
                "change" -> {
                    snackbar(requireView(), getString(R.string.pin_changed_text))
                }
                else -> {}
            }
        }
        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                navBackStackEntry.savedStateHandle.set("PIN", "")
            }
        })
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        archiveViewModel.getArchivedNotes.observe(
            viewLifecycleOwner
        ) { note ->
            Log.d("ArchiveFragment", "Received archived notes: $note")
            adapter.submitList(note)
            if (note.isEmpty()) {
                archiveRecyclerView.visibility = View.GONE
                archiveEmptyText.visibility = View.VISIBLE
            } else {
                archiveRecyclerView.visibility = View.VISIBLE
                archiveEmptyText.visibility = View.GONE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_more, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.more_options -> {
                findNavController().navigate(R.id.archiveBottomSheet)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
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
            val view = layoutInflater.inflate(R.layout.jotter_item, parent, false)
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

    private fun snackbar(view: View, text: CharSequence) {
        val locked = Preference.getLockState(requireContext())
        val duration = if (locked) { Snackbar.LENGTH_SHORT } else { Snackbar.LENGTH_LONG }
        val snackbar = Snackbar.make(view, text, duration)
        if (!locked) {
            snackbar.setAction(getString(R.string.lock).uppercase()) {
                Preference.setLockState(requireContext(), true)
                Toast.makeText(context, getString(R.string.archive_locked), Toast.LENGTH_SHORT).show()
            }
        }
        snackbar.show()
        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                snackbar.dismiss()
            }
        })
    }
}