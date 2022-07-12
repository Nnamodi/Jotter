package com.roland.android.jotter.util

import android.app.Application
import android.content.Intent
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.roland.android.jotter.R
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.viewModel.JotterViewModel

private lateinit var actionMode: ActionMode
private val viewModel = JotterViewModel(Application())
private val selectedCards = mutableListOf<MaterialCardView>()
private val selectedNotes = mutableListOf<Note>()
var actionEnabled = MutableLiveData(false)
private var manySelected = false

fun callBack(card: MaterialCardView, note: Note, binding: ViewDataBinding): ActionMode.Callback =
    object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            val menuInflater = mode.menuInflater
            menuInflater.inflate(R.menu.menu_item_selected, menu)
            selectedNotes.clear()
            selectedCards.clear()
            manySelected = false
            actionMode = mode
            actionEnabled.value = true
            card.isChecked = true
            selectedNotes.add(note)
            selectedCards.add(card)
            viewModel.selectNote(note, true)
            mode.title = "${selectedNotes.size}"
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            menu.findItem(R.id.share_note).isVisible = !manySelected
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.star_note -> {
                    Toast.makeText(binding.root.context, "Coming soon...", Toast.LENGTH_SHORT)
                        .show()
                    mode.finish()
                }
                R.id.trash_note -> {
                    val text = if (selectedNotes.size == 1) {
                        binding.root.context.getString(R.string.delete_this_note, note.title)
                    } else {
                        binding.root.context.getString(
                            R.string.delete_multiple_note,
                            selectedNotes.size
                        )
                    }
                    val builder = MaterialAlertDialogBuilder(binding.root.context)
                    builder.setTitle(R.string.move_to_trash_)
                        .setMessage(text)
                        .setPositiveButton(R.string.continue_action) { _, _ ->
                            selectedNotes.forEach { note ->
                                viewModel.apply {
                                    trashNote(note, archive = false, trash = true)
                                    selectNote(note, false)
                                }
                            }
                            mode.finish()
                            Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG)
                                .setAction(R.string.undo) {
                                    selectedNotes.forEach { note ->
                                        viewModel.trashNote(note, archive = false, trash = false)
                                    }
                                }.show()
                        }
                        .setNegativeButton(R.string.close) { _, _ -> }
                        .show()
                }
                R.id.share_note -> {
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT, binding.root.context.getString(
                                R.string.text, note.title, note.body
                            )
                        )
                        putExtra(Intent.EXTRA_SUBJECT, R.string.subject_text)
                    }.also { intent ->
                        val chooserIntent = Intent.createChooser(
                            intent,
                            binding.root.context.getString(R.string.chooser_title)
                        )
                        binding.root.context.startActivity(chooserIntent)
                    }
                }
                R.id.archive_note -> {
                    val text = if (selectedNotes.size == 1) {
                        R.string.archived
                    } else {
                        R.string.note_archived
                    }
                    selectedNotes.forEach { note ->
                        viewModel.apply {
                            archiveNote(note, true)
                            selectNote(note, false)
                        }
                    }
                    Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo) {
                            selectedNotes.forEach { note ->
                                viewModel.archiveNote(note, false)
                            }
                        }.show()
                    mode.finish()
                }
            }
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            selectedCards.forEach { it.isChecked = false }
            selectedNotes.forEach { viewModel.selectNote(it, false) }
            actionEnabled.value = false
            manySelected = false
            mode.finish()
        }
    }

fun select(card: MaterialCardView, note: Note) {
    if (!card.isChecked) {
        card.isChecked = true
        selectedCards.add(card)
        selectedNotes.add(note)
        viewModel.selectNote(note, true)
    } else {
        card.isChecked = false
        selectedCards.remove(card)
        selectedNotes.remove(note)
        viewModel.selectNote(note, false)
    }
    if (selectedNotes.size == 0) {
        actionMode.finish()
    }
    manySelected = !(selectedNotes.size == 1 || selectedNotes.size == 0)
    actionMode.title = "${selectedNotes.size}"
    actionMode.invalidate()
}