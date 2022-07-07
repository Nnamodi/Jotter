package com.roland.android.jotter.view.details

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.roland.android.jotter.R
import com.roland.android.jotter.databinding.FragmentJotBinding
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.util.Preference
import com.roland.android.jotter.util.TouchListener
import com.roland.android.jotter.viewModel.JotterViewModel
import java.text.SimpleDateFormat
import java.util.*

class JotFragment : Fragment() {
    private var _binding: FragmentJotBinding? = null
    private val binding get() = _binding!!
    private lateinit var note: Note
    private lateinit var viewModel: JotterViewModel
    private val args by navArgs<JotFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this) [JotterViewModel::class.java]
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentJotBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).supportActionBar?.title = args.note.title
        note = Note()
        binding.apply {
            edit.setOnTouchListener(TouchListener())
            edit.setOnClickListener {
                val action = JotFragmentDirections.moveToEdit(args.note)
                findNavController().navigate(action)
            }
            noteTitle.text = args.note.title
            noteBody.text = args.note.body
            noteBody.textSize = Preference.getSize(requireContext()).toFloat()
            date.text = SimpleDateFormat("d|M|yy", Locale.getDefault()).format(args.note.date)
            time.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(args.note.date)
            if (args.note.trashed) {
                edit.hide()
            }
        }
        savedStateHandle()
        setupMenuItems()
        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) { _binding = null }
        })
        return binding.root
    }

    private fun setupMenuItems() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
                inflater.inflate(R.menu.menu_jot, menu)
                val starItem = menu.findItem(R.id.star)
                if (args.note.starred) {
                    starItem.apply {
                        title = getString(R.string.unstar)
                        setIcon(R.drawable.menu_unstar)
                    }
                }
                starItem.isVisible = !args.note.trashed
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.more_options -> {
                        if (args.note.trashed) {
                            val action = JotFragmentDirections.jotToTrashJotBottomSheet(args.note)
                            findNavController().navigate(action)
                        } else {
                            val action =
                                JotFragmentDirections.jotFragmentToJotBottomSheet(args.note)
                            findNavController().navigate(action)
                        }
                        true
                    }
                    R.id.star -> {
                        if (args.note.starred) {
                            viewModel.starNote(args.note, false)
                        } else {
                            viewModel.starNote(args.note, true)
                        }
                        activity?.invalidateOptionsMenu()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun savedStateHandle() {
        val navBackStackEntry = findNavController().getBackStackEntry(R.id.jotFragment)
        navBackStackEntry.savedStateHandle.apply {
            getLiveData<Note>("archive").observe(viewLifecycleOwner) { note ->
                if (note.archived) {
                    findNavController().apply {
                        previousBackStackEntry?.savedStateHandle?.set("archive", note)
                        navigateUp()
                    }
                }
            }
            getLiveData<Note>("trashed").observe(viewLifecycleOwner) { note ->
                if (note.trashed) {
                    findNavController().apply {
                        previousBackStackEntry?.savedStateHandle?.set("trashed", note)
                        navigateUp()
                    }
                }
            }
            getLiveData<Note>("unarchive").observe(viewLifecycleOwner) { note ->
                if (!note.archived) {
                    findNavController().apply {
                        previousBackStackEntry?.savedStateHandle?.set("unarchive", note)
                        navigateUp()
                    }
                }
            }
            getLiveData<Float>("text_size").observe(viewLifecycleOwner) { textSize ->
                binding.noteBody.textSize = textSize
            }
        }
    }
}