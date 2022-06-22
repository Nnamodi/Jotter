package com.roland.android.jotter.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.roland.android.jotter.R
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.viewModel.JotterViewModel
import java.text.SimpleDateFormat
import java.util.*

class JotterFragment : Fragment() {
    private lateinit var jotterRecyclerView: RecyclerView
    private lateinit var jotterViewModel: JotterViewModel
    private lateinit var selectedNotes: MutableList<Note>
    private lateinit var actionMode: ActionMode
    private lateinit var emptyText: TextView
    private lateinit var jot: ExtendedFloatingActionButton
    private val adapter = JotterAdapter()
    private var actionEnabled = false
    private var manySelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        jotterViewModel = ViewModelProvider(this) [JotterViewModel::class.java]
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_jotter, container, false)
        val navBackStackEntry = findNavController().getBackStackEntry(R.id.jotterFragment)
        selectedNotes = mutableListOf()
        jotterRecyclerView = view.findViewById(R.id.recycler_view)
        jotterRecyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    jot.shrink()
                } else {
                    jot.extend()
                }
                Log.d("ScrollState", "Scroll direction is: $dy on y-axis")
            }
        })
        jot = view.findViewById(R.id.jot)
        jot.setOnClickListener {
            val action = JotterFragmentDirections.moveIntoEditing(null)
            findNavController().navigate(action)
        }
        emptyText = view.findViewById(R.id.jotter_empty_text)
        jotterRecyclerView.adapter = adapter
        navBackStackEntry.savedStateHandle.getLiveData<Note>("archive").observe(
            viewLifecycleOwner
        ) { note ->
            if (note.archived) {
                Snackbar.make(requireView(), getString(R.string.jot_archived, note.title), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.undo)) {
                        jotterViewModel.archiveNote(note, false)
                    }.show()
            }
        }
        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                navBackStackEntry.savedStateHandle.set("archive", Note())
            }
        })
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        jotterViewModel.getNotes.observe(
            viewLifecycleOwner
        ) { note ->
            Log.d("JotterFragment", "Notes received: $note")
            adapter.submitList(note)
            if (note.isEmpty()) {
                emptyText.visibility = View.VISIBLE
                jotterRecyclerView.visibility = View.GONE
            } else {
                emptyText.visibility = View.GONE
                jotterRecyclerView.visibility = View.VISIBLE
            }
            Log.d("ItemPosition", "Note increased = ${note.size == +1}")

            // Swipe_to_archive implementation
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ) = true

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val index = viewHolder.bindingAdapterPosition
                    jotterViewModel.archiveNote(note[index], true)
                    Snackbar.make(requireContext(), requireView(), getString(R.string.archived), Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.undo)) {
                            jotterViewModel.archiveNote(note[index], false)
                        }.show()
                }
            }).attachToRecyclerView(jotterRecyclerView)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_more, menu)
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

    private inner class JotterHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        private lateinit var note: Note
        private lateinit var noteTitle: TextView
        private lateinit var noteBody: TextView
        private lateinit var dateText: TextView
        private val card = itemView as MaterialCardView

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
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
            if (actionEnabled) {
                select(card, note)
            } else {
                val action = JotterFragmentDirections.moveToJot(note)
                findNavController(view).navigate(action)
            }
        }

        override fun onLongClick(view: View): Boolean {
            if (!actionEnabled) {
                val callback = object : ActionMode.Callback {
                    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                        val menuInflater = mode.menuInflater
                        menuInflater.inflate(R.menu.jotter_item_selected, menu)
                        selectedNotes.clear()
                        actionMode = mode
                        actionEnabled = true
                        card.isChecked = true
                        selectedNotes.add(note)
                        mode.title = "${selectedNotes.size}"
                        jot.hide()
                        return true
                    }

                    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                        menu.findItem(R.id.share_note).isVisible = !manySelected
                        return true
                    }

                    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                        when (item.itemId) {
                            R.id.delete -> {
                                val text = if (selectedNotes.size == 1) {
                                    getString(R.string.delete_this_message, note.title)
                                } else {
                                    getString(R.string.delete_multiple_messages, selectedNotes.size)
                                }
                                val builder = MaterialAlertDialogBuilder(requireContext())
                                builder.setTitle(getString(R.string.delete_))
                                    .setIcon(R.drawable.dialog_delete_icon)
                                    .setMessage(text)
                                    .setPositiveButton(getString(R.string.continue_action)) { _, _ ->
                                        selectedNotes.forEach { note ->
                                            jotterViewModel.deleteNote(note)
                                        }
                                        mode.finish()
                                        Toast.makeText(requireContext(), getString(R.string.deleted), Toast.LENGTH_SHORT).show()
                                    }
                                    .setNegativeButton(getString(R.string.close)) { _, _ -> }
                                    .show()
                            }
                            R.id.share_note -> {
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, getString(R.string.text, note.title, note.body))
                                    putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject_text))
                                }.also { intent ->
                                    val chooserIntent = Intent.createChooser(intent, getString(R.string.chooser_title))
                                    startActivity(chooserIntent)
                                }
                            }
                            R.id.archive_note -> {
                                val text = if (selectedNotes.size == 1) {
                                    getString(R.string.archived)
                                } else {
                                    getString(R.string.note_archived)
                                }
                                selectedNotes.forEach { note ->
                                    jotterViewModel.archiveNote(note, true)
                                }
                                Snackbar.make(requireContext(), view, text, Snackbar.LENGTH_LONG)
                                    .setAction(getString(R.string.undo)) {
                                        selectedNotes.forEach { note ->
                                            jotterViewModel.archiveNote(note, false)
                                        }
                                    }.show()
                                mode.finish()
                            }
                        }
                        return true
                    }

                    override fun onDestroyActionMode(mode: ActionMode) {
                        if (view is MaterialCardView) {
                            view.isChecked = false
                        }
                        actionEnabled = false
                        manySelected = false
                        mode.finish()
                        jot.show()
                    }
                }
                activity?.startActionMode(callback)
            } else {
                select(card, note)
            }
            return true
        }
    }

    private inner class JotterAdapter : ListAdapter<Note, JotterHolder>(DiffCallBack()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JotterHolder {
            val view = layoutInflater.inflate(R.layout.jotter_item, parent, false)
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

    private fun select(card: MaterialCardView, note: Note) {
        if (!card.isChecked) {
            card.isChecked = !card.isChecked
            selectedNotes.add(note)
        } else {
            card.isChecked = !card.isChecked
            selectedNotes.remove(note)
        }
        if (selectedNotes.size == 0) {
            actionMode.finish()
        }
        manySelected = !(selectedNotes.size == 1 || selectedNotes.size == 0)
        actionMode.title = "${selectedNotes.size}"
        actionMode.invalidate()
    }
}