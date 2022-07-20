package com.roland.android.jotter.view.archive

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.roland.android.jotter.R
import com.roland.android.jotter.databinding.FragmentArchiveBinding
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.util.Preference
import com.roland.android.jotter.util.actionEnabled
import com.roland.android.jotter.util.allCards
import com.roland.android.jotter.util.allNotes
import com.roland.android.jotter.view.archive.adapter.ArchiveAdapter
import com.roland.android.jotter.viewModel.ArchiveViewModel

class ArchiveFragment : Fragment() {
    private lateinit var binding: FragmentArchiveBinding
    private lateinit var archiveViewModel: ArchiveViewModel
    private var adapter = ArchiveAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        archiveViewModel = ViewModelProvider(this) [ArchiveViewModel::class.java]
        binding = FragmentArchiveBinding.inflate(layoutInflater)
        allCards.clear()
        binding.apply {
            archiveRecyclerView.adapter = adapter
            archiveRecyclerView.setHasFixedSize(true)
        }
        if (Preference.getLock(requireContext())) {
            activity?.onBackPressedDispatcher?.addCallback(this) {
                findNavController().navigate(R.id.back_to_jotterFragment)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        actionEnabled.value = false
        archiveViewModel.getArchivedNotes.observe(viewLifecycleOwner) { archive ->
            Log.d("ArchiveFragment", "Received archived notes: $archive")
            archive.forEach {
                if (actionEnabled.value == false) {
                    if (it.selected) {
                        it.selected = false
                        archiveViewModel.updateNote(it)
                    }
                }
            }
            adapter.submitList(archive)
            binding.archive = archive
            allNotes = archive.toMutableList()
        }
        setupMenuItems()
        savedStateHandle()
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
                        findNavController().navigate(R.id.archiveBottomSheet)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun savedStateHandle() {
        val snackbar = Snackbar.make(requireView(), "", Snackbar.LENGTH_LONG)
        val navBackStackEntry = findNavController().getBackStackEntry(R.id.archiveFragment)
        navBackStackEntry.savedStateHandle.apply {
            getLiveData<String>("PIN").observe(viewLifecycleOwner) { set ->
                when (set) {
                    "set" -> {
                        lockSnackbar(requireView(), getString(R.string.pin_set_text))
                    }
                    "change" -> {
                        lockSnackbar(requireView(), getString(R.string.pin_changed_text))
                    }
                    else -> {}
                }
            }
            getLiveData<Note>("unarchive").observe(viewLifecycleOwner) { note ->
                if (!note.archived) {
                    val noteTitle: String = note.title.ifEmpty { getString(R.string.note) }
                    snackbar.setText(getString(R.string.jot_unarchived, noteTitle))
                    snackbar.setAction(getString(R.string.undo)) {
                        note.archived = true
                        archiveViewModel.updateNote(note)
                    }.show()
                }
            }
            getLiveData<Note>("trashed").observe(viewLifecycleOwner) { note ->
                if (note.trashed) {
                    snackbar.setText(getString(R.string.moved_to_trash))
                    snackbar.setAction(getString(R.string.undo)) {
                        archiveViewModel.trashNote(note, archive = true, trash = false)
                    }.show()
                }
            }
        }
        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                navBackStackEntry.savedStateHandle.apply {
                    set("PIN", "")
                    set("unarchive", Note(archived = true))
                    set("trashed", Note())
                }
                if (snackbar.isShown) { snackbar.dismiss() }
            }
        })
    }

    private fun lockSnackbar(view: View, text: CharSequence) {
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
                if (snackbar.isShown) { snackbar.dismiss() }
            }
        })
    }
}