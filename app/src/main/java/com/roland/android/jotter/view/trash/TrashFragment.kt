package com.roland.android.jotter.view.trash

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import com.roland.android.jotter.databinding.FragmentTrashBinding
import com.roland.android.jotter.databinding.JotterItemBinding
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.util.*
import com.roland.android.jotter.view.trash.adapter.TrashAdapter
import com.roland.android.jotter.viewModel.TrashViewModel

class TrashFragment : Fragment() {
    private lateinit var trashViewModel: TrashViewModel
    private var _binding: FragmentTrashBinding? = null
    private val binding get() = _binding!!
    private var adapter = TrashAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTrashBinding.inflate(layoutInflater)
        trashViewModel = ViewModelProvider(this) [TrashViewModel::class.java]
        allCards.clear()
        actionEnabled.observe(viewLifecycleOwner) { enabled ->
            if (!enabled) { actionMode?.finish() }
        }
        binding.apply {
            trashRecyclerView.adapter = adapter
            trashRecyclerView.setHasFixedSize(true)
        }
        // Restore actionMode if destroyed by configuration change
        if (trashViewModel.actionWasEnabled) {
            manySelected = trashViewModel.manyWereSelected
            allIsSelected = trashViewModel.allWereSelected
            val bind = JotterItemBinding.inflate(LayoutInflater.from(binding.root.context))
            activity?.startActionMode(callBack(Note(trashed = true), bind, binding.root, isActive = true))
        }
        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                trashViewModel.apply {
                    actionWasEnabled = actionEnabled.value == true
                    manyWereSelected = manySelected == true
                    allWereSelected = allIsSelected == true
                }
            }
            if (event == Lifecycle.Event.ON_DESTROY) { _binding = null }
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        trashViewModel.getTrashedNotes.observe(viewLifecycleOwner) { trash ->
            Log.d("TrashFragment", "Received trashed notes: $trash")
            trash.forEach {
                if (it.starred) {
                    it.starred = false
                    trashViewModel.updateNote(it)
                }
                if (actionEnabled.value == false) {
                    if (it.selected) {
                        it.selected = false
                        trashViewModel.updateNote(it)
                    }
                }
            }
            adapter.submitList(trash)
            binding.trash = trash
            allNotes = trash.toMutableList()
        }
    }
}