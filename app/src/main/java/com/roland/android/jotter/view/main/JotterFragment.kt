package com.roland.android.jotter.view.main

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.roland.android.jotter.R
import com.roland.android.jotter.databinding.FragmentJotterBinding
import com.roland.android.jotter.databinding.JotterItemBinding
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.util.*
import com.roland.android.jotter.view.main.adapter.JotterAdapter
import com.roland.android.jotter.viewModel.JotterViewModel

class JotterFragment : Fragment() {
    private var _binding: FragmentJotterBinding? = null
    private val binding get() = _binding!!
    private lateinit var jotterViewModel: JotterViewModel
    private val adapter = JotterAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentJotterBinding.inflate(layoutInflater)
        jotterViewModel = ViewModelProvider(this) [JotterViewModel::class.java]
        allCards.clear()
        actionEnabled.observe(viewLifecycleOwner) { enabled ->
            if (!enabled) { binding.jot.show(); actionMode?.finish() }
            else { binding.jot.hide() }
        }
        binding.apply {
            recyclerView.adapter = adapter
            recyclerView.setHasFixedSize(true)
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) { jot.shrink() } else { jot.extend() }
                    Log.d("ScrollState", "Scroll direction is: $dy on y-axis")
                }
            })
            jot.setOnClickListener {
                val action = JotterFragmentDirections.moveIntoEditing(null)
                findNavController().navigate(action)
            }
        }
        savedStateHandle()

        // Restore actionMode if destroyed by configuration change
        if (jotterViewModel.actionWasEnabled) {
            manySelected = jotterViewModel.manyWereSelected
            allIsSelected = jotterViewModel.allWereSelected
            val bind = JotterItemBinding.inflate(LayoutInflater.from(binding.root.context))
            activity?.startActionMode(callBack(Note(), bind, binding.root, isActive = true))
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        jotterViewModel.getNotes.observe(viewLifecycleOwner) { notes ->
            Log.d("JotterFragment", "Notes received: $notes")
            notes.forEach { note ->
                if (actionEnabled.value == false) {
                    if (note.selected) {
                        jotterViewModel.selectNote(note, false)
                    }
                }
            }
            adapter.submitList(notes)
            binding.notes = notes
            allNotes = notes.toMutableList()

            // Swipe_to_archive implementation
            activity?.swipeCallback(binding.root, binding.recyclerView, jotterViewModel, viewLifecycleOwner)
        }
        setupMenuItems()
    }

    private fun setupMenuItems() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
                inflater.inflate(R.menu.menu_more, menu)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.more_options -> {
                        // invoke bottom sheet
                        findNavController().navigate(R.id.jotterBottomSheet)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun savedStateHandle() {
        val navBackStackEntry = findNavController().getBackStackEntry(R.id.jotterFragment)
        navBackStackEntry.savedStateHandle.apply {
            getLiveData<Note>("archive").observe(viewLifecycleOwner) { note ->
                if (note.archived) {
                    val noteTitle: String = note.title.ifEmpty { getString(R.string.note) }
                    Snackbar.make(requireView(), getString(R.string.jot_archived, noteTitle), Snackbar.LENGTH_LONG)
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
                navBackStackEntry.savedStateHandle["archive"] = Note()
                navBackStackEntry.savedStateHandle["trashed"] = Note()
                jotterViewModel.apply {
                    actionWasEnabled = actionEnabled.value == true
                    manyWereSelected = manySelected == true
                    allWereSelected = allIsSelected == true
                }
            }
            if (event == Lifecycle.Event.ON_DESTROY) { _binding = null }
        })
    }
}