package com.roland.android.jotter.view.archive

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.addCallback
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
import com.roland.android.jotter.view.archive.adapter.ArchiveAdapter
import com.roland.android.jotter.viewModel.ArchiveViewModel

class ArchiveFragment : Fragment() {
    private lateinit var binding: FragmentArchiveBinding
    private lateinit var archiveViewModel: ArchiveViewModel
    private var adapter = ArchiveAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        archiveViewModel = ViewModelProvider(this) [ArchiveViewModel::class.java]
        binding = FragmentArchiveBinding.inflate(layoutInflater)
        binding.archiveRecyclerView.adapter = adapter
        if (Preference.getLock(requireContext())) {
            activity?.onBackPressedDispatcher?.addCallback(this) {
                findNavController().navigate(R.id.back_to_jotterFragment)
            }
        }
        setHasOptionsMenu(true)
        savedStateHandle()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        archiveViewModel.getArchivedNotes.observe(
            viewLifecycleOwner
        ) { archive ->
            Log.d("ArchiveFragment", "Received archived notes: $archive")
            adapter.submitList(archive)
            binding.archive = archive
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

    private fun savedStateHandle() {
        val navBackStackEntry = findNavController().getBackStackEntry(R.id.archiveFragment)
        navBackStackEntry.savedStateHandle.apply {
            getLiveData<String>("PIN").observe(viewLifecycleOwner) { set ->
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
            getLiveData<Note>("trashed").observe(viewLifecycleOwner) { note ->
                if (note.trashed) {
                    val snackbar = Snackbar.make(requireView(), getString(R.string.moved_to_trash), Snackbar.LENGTH_LONG)
                    snackbar.setAction(getString(R.string.undo)) {
                        archiveViewModel.trashNote(note, archive = true, trash = false)
                    }.show()
                    viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_STOP) {
                            snackbar.dismiss()
                        }
                    })
                }
            }
        }
        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                navBackStackEntry.savedStateHandle.set("PIN", "")
                navBackStackEntry.savedStateHandle.set("trashed", Note())
            }
        })
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