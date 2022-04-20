package com.roland.android.jotter.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.roland.android.jotter.R
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.util.Preference
import com.roland.android.jotter.viewModel.JotterViewModel
import java.text.SimpleDateFormat
import java.util.*

class JotterFragment : Fragment() {
    private lateinit var jot: View
    private lateinit var jotterRecyclerView: RecyclerView
    private lateinit var jotterViewModel: JotterViewModel
    private lateinit var emptyText: TextView
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
        emptyText = view.findViewById(R.id.jotter_empty_text)
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
            if (note.isEmpty()) {
                emptyText.visibility = View.VISIBLE
                jotterRecyclerView.visibility = View.GONE
            } else {
                emptyText.visibility = View.GONE
                jotterRecyclerView.visibility = View.VISIBLE
            }
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

    private inner class JotterHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        private lateinit var note: Note
        private lateinit var noteTitle: TextView
        private lateinit var noteBody: TextView
        private lateinit var dateText: TextView
        private lateinit var check: CheckBox
        private var actionEnabled = false

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
            check = itemView.findViewById(R.id.checkBox)
        }

        override fun onClick(view: View) {
            val action = JotterFragmentDirections.moveToJot(note)
            findNavController(view).navigate(action)
        }

        override fun onLongClick(view: View): Boolean {
            if (!actionEnabled) {
                val callback = object : ActionMode.Callback {
                    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                        val menuInflater =  mode.menuInflater
                        menuInflater.inflate(R.menu.jotter_item_selected, menu)
                        return true
                    }

                    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                        actionEnabled = true
                        if (check.visibility == View.GONE) {
                            check.visibility = View.VISIBLE
                            itemView.setBackgroundColor(ContextCompat.getColor(
                                requireContext(),
                                R.color.grey
                            ))
                        } else {
                            check.visibility = View.GONE
                            itemView.setBackgroundColor(ContextCompat.getColor(
                                requireContext(),
                                R.color.primaryTextColor
                            ))
                        }
                        return true
                    }

                    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                        when (item.itemId) {
                            R.id.delete -> {
                                val builder = AlertDialog.Builder(requireContext())
                                builder.setMessage("Delete selected notes\nThis action is irreversible")
                                builder.setPositiveButton("Continue") { _, _ ->
                                    jotterViewModel.deleteNote(note)
                                    Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show()
                                }
                                builder.setNegativeButton("Close") { _, _ -> }
                                builder.create().show()
                            }
                            R.id.share_note -> {
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, getString(R.string.text, note.title, note.body))
                                    putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject_text))
                                }.also { intent ->
                                    val chooserIntent = Intent.createChooser(intent, null)
                                    startActivity(chooserIntent)
                                }
                            }
                        }
                        return true
                    }

                    override fun onDestroyActionMode(mode: ActionMode) {
                        note.isSelected = false
                        mode.finish()
                    }
                }
                activity?.startActionMode(callback)
            } else {
                if (check.visibility == View.GONE) {
                    check.visibility = View.VISIBLE
                    itemView.setBackgroundColor(ContextCompat.getColor(
                        requireContext(),
                        R.color.grey
                    ))
                } else {
                    check.visibility = View.GONE
                    itemView.setBackgroundColor(ContextCompat.getColor(
                        requireContext(),
                        R.color.primaryTextColor
                    ))
                }
            }
            return true
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