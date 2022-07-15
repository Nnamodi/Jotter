package com.roland.android.jotter.view.trash

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.roland.android.jotter.databinding.FragmentTrashBinding
import com.roland.android.jotter.util.actionEnabled
import com.roland.android.jotter.view.trash.adapter.TrashAdapter
import com.roland.android.jotter.viewModel.TrashViewModel

class TrashFragment : Fragment() {
    private lateinit var trashViewModel: TrashViewModel
    private lateinit var binding: FragmentTrashBinding
    private var adapter = TrashAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTrashBinding.inflate(layoutInflater)
        trashViewModel = ViewModelProvider(this) [TrashViewModel::class.java]
        actionEnabled.value = false
        binding.apply {
            trashRecyclerView.adapter = adapter
            trashRecyclerView.setHasFixedSize(true)
        }
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
        }
    }
}