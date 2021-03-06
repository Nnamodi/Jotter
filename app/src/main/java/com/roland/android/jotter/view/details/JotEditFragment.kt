package com.roland.android.jotter.view.details

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.roland.android.jotter.R
import com.roland.android.jotter.databinding.FragmentJotEditBinding
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.util.Preference
import com.roland.android.jotter.viewModel.JotterViewModel
import java.util.*

class JotEditFragment : Fragment() {
    private lateinit var jotViewModel: JotterViewModel
    private lateinit var noteTitle: TextView
    private lateinit var noteBody: TextView
    private var _binding: FragmentJotEditBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<JotEditFragmentArgs>()
    private val note = Note()
    private var noteIsNew = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        jotViewModel = ViewModelProvider(this) [JotterViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.menu_cancel)
        _binding = FragmentJotEditBinding.inflate(inflater, container, false)
        noteTitle = binding.editTitle
        noteBody = binding.editBody
        noteTitle.text = args.edit?.title
        noteBody.text = args.edit?.body
        noteBody.textSize = Preference.getSize(requireContext()).toFloat()
        noteIsNew = noteTitle.text.isEmpty() && noteBody.text.isEmpty()
        activity?.onBackPressedDispatcher?.addCallback(this) {
            val noteIsEdited = !noteTitle.text.contentEquals(args.edit?.title) ||
                    !noteBody.text.contentEquals(args.edit?.body)
            val noteIsNotEmpty = noteTitle.text.isNotEmpty() || noteBody.text.isNotEmpty()
            if (noteIsEdited && noteIsNotEmpty) {
                confirmationDialog()
            } else {
                findNavController().navigateUp()
            }
        }
        if (noteIsNew) { noteTitle.requestFocus() } else { noteBody.requestFocus() }
        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                val activity = activity?.currentFocus
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(activity?.windowToken, 0)
            }
            if (event == Lifecycle.Event.ON_DESTROY) { _binding = null }
        })
        setupMenuItems()
        return binding.root
    }

    private fun setupMenuItems() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
                inflater.inflate(R.menu.menu_jot_edit, menu)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean {
                return when (item.itemId) {
                    android.R.id.home -> {
                        val noteIsEdited = !noteTitle.text.contentEquals(args.edit?.title) ||
                                !noteBody.text.contentEquals(args.edit?.body)
                        val noteIsNotEmpty =
                            noteTitle.text.isNotEmpty() || noteBody.text.isNotEmpty()
                        if (noteIsEdited && noteIsNotEmpty) {
                            confirmationDialog()
                        } else {
                            findNavController().navigateUp()
                        }
                        true
                    }
                    R.id.save_jot -> {
                        // save jot
                        if (!noteIsNew) updateNote() else saveNote()
                        true
                    }
                    else -> true
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun saveNote() {
        if (noteTitle.text.isNotEmpty() || noteBody.text.isNotEmpty()) {
            note.apply {
                title = noteTitle.text.toString()
                body = noteBody.text.toString()
                date = Calendar.getInstance().time
            }
            jotViewModel.addNotes(note)
            findNavController().navigateUp()
            Toast.makeText(context, getString(R.string.save_note_text), Toast.LENGTH_SHORT).show()
        } else {
            findNavController().navigateUp()
            Toast.makeText(context, getString(R.string.saving_empty_note_text), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateNote() {
        val titleIsSame = noteTitle.text.contentEquals(args.edit?.title)
        val bodyIsSame = noteBody.text.contentEquals(args.edit?.body)
        if (noteTitle.text.isNotEmpty() || noteBody.text.isNotEmpty()) {
            note.apply {
                id = args.edit?.id!!
                title = noteTitle.text.toString()
                body = noteBody.text.toString()
                date = Calendar.getInstance().time
                archived = args.edit?.archived!!
                starred = args.edit?.starred == true
            }
            if (titleIsSame && bodyIsSame) {
                findNavController().navigateUp()
            } else {
                jotViewModel.updateNote(note)
                val action = JotEditFragmentDirections.actionJotEditToJot(note)
                findNavController().navigate(action)
                Toast.makeText(context, getString(R.string.note_updated_text), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, getString(R.string.note_not_updated_text), Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmationDialog() {
        val message = if (noteIsNew) {
            getString(R.string.note_saved_message)
        } else {
            getString(R.string.note_updated_message)
        }
        val dialog = MaterialAlertDialogBuilder(requireContext())
        dialog.setTitle(getString(R.string.discard_))
            .setMessage(message)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                if (noteIsNew) saveNote() else updateNote()
            }
            .setNegativeButton(R.string.discard) { _, _ ->
                findNavController().navigateUp()
            }
            .show()
    }
}