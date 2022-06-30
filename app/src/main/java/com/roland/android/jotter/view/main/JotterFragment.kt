package com.roland.android.jotter.view.main

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.roland.android.jotter.R
import com.roland.android.jotter.databinding.FragmentJotterBinding
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.util.swipeCallback
import com.roland.android.jotter.view.main.adapter.JotterAdapter
import com.roland.android.jotter.viewModel.JotterViewModel

class JotterFragment : Fragment() {
    private lateinit var binding: FragmentJotterBinding
    private lateinit var jotterViewModel: JotterViewModel
    private lateinit var selectedNotes: MutableList<Note>
    private lateinit var actionMode: ActionMode
    private lateinit var emptyText: TextView
    private lateinit var jot: ExtendedFloatingActionButton
    private val adapter = JotterAdapter()
    private var actionEnabled = false
    private var manySelected = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentJotterBinding.inflate(layoutInflater)
        jotterViewModel = ViewModelProvider(this) [JotterViewModel::class.java]
        binding.recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    binding.jot.shrink()
                } else {
                    binding.jot.extend()
                }
                Log.d("ScrollState", "Scroll direction is: $dy on y-axis")
            }
        })
        binding.recyclerView.adapter = adapter
        binding.jot.setOnClickListener {
            val action = JotterFragmentDirections.moveIntoEditing(null)
            findNavController().navigate(action)
        }
        setHasOptionsMenu(true)
        savedStateHandle()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        jotterViewModel.getNotes.observe(
            viewLifecycleOwner
        ) { note ->
            Log.d("JotterFragment", "Notes received: $note")
            adapter.submitList(note)
            binding.notes = note

            // Swipe_to_archive implementation
            activity?.swipeCallback(binding.root, binding.recyclerView, note, jotterViewModel)
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

    private fun savedStateHandle() {
        val navBackStackEntry = findNavController().getBackStackEntry(R.id.jotterFragment)
        navBackStackEntry.savedStateHandle.apply {
            getLiveData<Note>("archive").observe(viewLifecycleOwner) { note ->
                if (note.archived) {
                    Snackbar.make(requireView(), getString(R.string.jot_archived, note.title), Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.undo)) {
                            jotterViewModel.archiveNote(note, false)
                        }.show()
                }
            }
            getLiveData<Note>("trashed").observe(viewLifecycleOwner) { note ->
                if (note.trashed) {
                    Snackbar.make(requireView(), getString(R.string.moved_to_trash), Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.undo)) {
                            jotterViewModel.trashNote(note, archive = false, trash = false)
                        }.show()
                }
            }
        }
        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                navBackStackEntry.savedStateHandle.set("archive", Note())
                navBackStackEntry.savedStateHandle.set("trashed", Note())
            }
        })
    }
}