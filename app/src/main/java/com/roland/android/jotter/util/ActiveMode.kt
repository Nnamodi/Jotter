package com.roland.android.jotter.util

import android.app.Application
import android.content.Intent
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.roland.android.jotter.R
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.viewModel.JotterViewModel
import com.roland.android.jotter.viewModel.TrashViewModel

private lateinit var actionMode: ActionMode
private val viewModel = JotterViewModel(Application())
private val selectedCards = mutableListOf<MaterialCardView>()
private val selectedNotes = mutableListOf<Note>()
var actionEnabled = MutableLiveData(false)
private var noteArchived = false
private var manySelected = false

fun callBack(card: MaterialCardView, note: Note, binding: ViewDataBinding, view: View): ActionMode.Callback =
    object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            val menuInflater = mode.menuInflater
            menuInflater.inflate(R.menu.menu_item_selected, menu)
            menu.apply {
                if (note.trashed) {
                    findItem(R.id.star_note).isVisible = false
                    findItem(R.id.archive_note).isVisible = false
                    findItem(R.id.unarchive_note).isVisible = false
                    findItem(R.id.trash_note).isVisible = false
                } else {
                    findItem(R.id.restore_note).isVisible = false
                    findItem(R.id.delete_permanently).isVisible = false
                }
                if (note.archived) {
                    noteArchived = true
                    findItem(R.id.archive_note).isVisible = false
                } else {
                    noteArchived = false
                    findItem(R.id.unarchive_note).isVisible = false
                }
            }
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
            menu.findItem(R.id.share_note).isVisible = !manySelected && !note.trashed
            selectedNotes.forEach { note ->
                if (note.starred) {
                    menu.findItem(R.id.star_note).apply {
                        setIcon(R.drawable.menu_unstar)
                        setTitle(R.string.unstar)
                    }
                } else {
                    menu.findItem(R.id.star_note).apply {
                        setIcon(R.drawable.menu_star)
                        setTitle(R.string.star)
                    }
                }
            }
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val context = binding.root.context
            when (item.itemId) {
                R.id.star_note -> {
                    selectedNotes.forEach {
                        if (it.starred) { viewModel.starNote(it, false) }
                        else { viewModel.starNote(it, true) }
                    }
                    mode.finish()
                }
                R.id.trash_note -> {
                    val text = if (selectedNotes.size == 1) { context.getString(R.string.delete_this_note, note.title) }
                                else { context.getString(R.string.delete_multiple_note, selectedNotes.size) }
                    deleteDialog(view, text, binding, true)
                }
                R.id.share_note -> {
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT,
                            context.getString(R.string.text, note.title, note.body)
                        )
                        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.subject_text))
                    }.also { intent ->
                        val chooserIntent = Intent.createChooser(
                            intent, context.getString(R.string.chooser_title)
                        )
                        context.startActivity(chooserIntent)
                    }
                }
                R.id.archive_note -> {
                    val text = if (selectedNotes.size == 1) { context.getString(R.string.archived) }
                                else { context.getString(R.string.note_archived) }
                    archiveNote(view, text, archive = true)
                }
                R.id.unarchive_note -> {
                    val text = if (selectedNotes.size == 1) { context.getString(R.string.unarchived) }
                                else { context.getString(R.string.note_unarchived) }
                    archiveNote(view, text, archive = false)
                }
                R.id.restore_note -> {
                    selectedNotes.forEach {
                        viewModel.trashNote(it, archive = false, trash = false)
                    }
                    mode.finish()
                    Toast.makeText(binding.root.context, binding.root.context
                        .getString(R.string.restored), Toast.LENGTH_SHORT).show()
                }
                R.id.delete_permanently -> {
                    val text = if (selectedNotes.size == 1) { context.getString(R.string.delete_permanently_dialog) }
                                else { context.getString(R.string.delete_multiple_permanently_dialog) }
                    deleteDialog(view, text, binding, isTrashed = false)
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

private fun archiveNote(view: View, text: String, archive: Boolean) {
    selectedNotes.forEach { note ->
        viewModel.apply {
            archiveNote(note, archive)
            selectNote(note, false)
        }
    }
    Snackbar.make(view, text, Snackbar.LENGTH_LONG)
        .setAction(R.string.undo) {
            selectedNotes.forEach { viewModel.archiveNote(it, !archive) }
        }.show()
    actionMode.finish()
}

private fun deleteDialog(view: View, text: String, binding: ViewDataBinding, isTrashed: Boolean) {
    val trashViewModel = TrashViewModel(Application())
    val snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG)
        .setAction(R.string.undo) {
            selectedNotes.forEach { note ->
                viewModel.trashNote(note, archive = noteArchived, trash = false)
            }
        }
    val title = if (isTrashed) { R.string.move_to_trash_ } else { R.string.delete_permanently_ }
    val builder = MaterialAlertDialogBuilder(binding.root.context)
    if (!isTrashed) { builder.setIcon(R.drawable.dialog_delete_icon) }
    builder.setTitle(title)
        .setMessage(text)
        .setPositiveButton(R.string.continue_action) { _, _ ->
            selectedNotes.forEach { note ->
                if (isTrashed) {
                    viewModel.apply {
                        trashNote(note, archive = false, trash = true)
                        selectNote(note, false)
                    }
                } else {
                    trashViewModel.deleteNote(note)
                }
            }
            if (isTrashed) { snackbar.show() }
            else {
                Toast.makeText(binding.root.context, binding.root.context
                        .getString(R.string.deleted), Toast.LENGTH_SHORT
                ).show()
            }
            actionMode.finish()
        }
        .setNegativeButton(R.string.close) { _, _ -> }
        .show()
}