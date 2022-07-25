package com.roland.android.jotter.util

import android.app.Application
import android.content.Intent
import android.os.Handler
import android.os.Looper
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
import com.roland.android.jotter.databinding.JotterItemBinding
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.viewModel.JotterViewModel
import com.roland.android.jotter.viewModel.TrashViewModel

var actionMode: ActionMode? = null
private val viewModel = JotterViewModel(Application())
private var handler = Handler(Looper.getMainLooper())
private val selectedCards = mutableListOf<MaterialCardView>()
private val selectedNotes = mutableListOf<Note>()
var allCards = mutableListOf<MaterialCardView>()
var allNotes = mutableListOf<Note>()
var actionEnabled = MutableLiveData(false)
private var noteArchived = false
var allIsSelected = false
var manySelected = false

fun callBack(note: Note, binding: JotterItemBinding, view: View, isActive: Boolean = false): ActionMode.Callback =
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
            val card = binding.root as MaterialCardView
            if (!isActive) {
                selectedNotes.clear(); selectedCards.clear()
                selectedNotes.add(note); selectedCards.add(card)
                card.isChecked = true
                allIsSelected = false
                manySelected = false
            }
            actionMode = mode
            actionEnabled.value = true
            selectedNotes.forEach { viewModel.selectNote(it, true) }
            mode.title = "${selectedNotes.size}"
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            menu.findItem(R.id.share_note).isVisible = !manySelected && !note.trashed
            if (allIsSelected) {
                menu.findItem(R.id.select_all).apply {
                    setIcon(R.drawable.menu_unselect_all)
                    setTitle(R.string.unselect_all)
                }
            } else {
                menu.findItem(R.id.select_all).apply {
                    setIcon(R.drawable.menu_select_all)
                    setTitle(R.string.select_all)
                }
            }
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
                    handler.postDelayed({
                        selectedNotes.forEach {
                            if (it.starred) { viewModel.starNote(it, false) }
                            else { viewModel.starNote(it, true) }
                        }
                    }, 300)
                    mode.finish()
                }
                R.id.trash_note -> {
                    var trashNote = Note()
                    selectedNotes.forEach { trashNote = it }
                    val noteTitle: String = trashNote.title.ifEmpty { context.getString(R.string.note) }
                    val text = if (selectedNotes.size == 1) { context.getString(R.string.delete_this_note, noteTitle) }
                                else { context.getString(R.string.delete_multiple_note, selectedNotes.size) }
                    deleteDialog(view, text, binding, true)
                }
                R.id.share_note -> {
                    var shareNote = Note()
                    Intent(Intent.ACTION_SEND).apply {
                        selectedNotes.forEach { shareNote = it }
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT,
                            context.getString(R.string.text, shareNote.title, shareNote.body)
                        )
                        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.subject_text))
                    }.also { intent ->
                        val chooserIntent = Intent.createChooser(
                            intent, context.getString(R.string.chooser_title)
                        )
                        context.startActivity(chooserIntent)
                    }
                    handler.postDelayed({ mode.finish() }, 300)
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
                    handler.postDelayed({
                        selectedNotes.forEach {
                            viewModel.trashNote(it, archive = false, trash = false)
                        }
                        Toast.makeText(binding.root.context, binding.root.context
                            .getString(R.string.restored), Toast.LENGTH_SHORT).show()
                    }, 300)
                    mode.finish()
                }
                R.id.delete_permanently -> {
                    var deleteNote = Note()
                    selectedNotes.forEach { deleteNote = it }
                    val noteTitle: String = deleteNote.title.ifEmpty { context.getString(R.string.note) }
                    val text = if (selectedNotes.size == 1) { context.getString(R.string.delete_permanently_dialog, noteTitle) }
                                else { context.getString(R.string.delete_multiple_permanently_dialog) }
                    deleteDialog(view, text, binding, isTrashed = false)
                }
                R.id.select_all -> {
                    if (!allIsSelected) {
                        selectedCards.clear(); selectedNotes.clear()
                        allIsSelected = true; manySelected = true
                        selectedNotes.addAll(allNotes)
                        selectedCards.addAll(allCards)
                        selectedCards.forEach { it.isChecked = true }
                        selectedNotes.forEach {
                            viewModel.selectNote(it, true)
                        }
                        mode.invalidate()
                    } else { mode.finish() }
                    mode.title = "${selectedNotes.size}"
                }
            }
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            selectedCards.forEach { it.isChecked = false }
            selectedNotes.forEach { viewModel.selectNote(it, false) }
            actionEnabled.value = false
            manySelected = false
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
        actionMode?.finish()
    }
    allIsSelected = selectedNotes.size == allNotes.size
    manySelected = selectedNotes.size != 1
    actionMode?.title = "${selectedNotes.size}"
    actionMode?.invalidate()
}

private fun archiveNote(view: View, text: String, archive: Boolean) {
    handler.postDelayed({
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
    }, 300)
    actionMode?.finish()
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
            handler.postDelayed({
                selectedNotes.forEach { note ->
                    if (isTrashed) {
                        viewModel.apply {
                            trashNote(note, archive = false, trash = true)
                            selectNote(note, false)
                        }
                    } else { trashViewModel.deleteNote(note) }
                }
                if (isTrashed) { snackbar.show() }
                else {
                    Toast.makeText(binding.root.context, binding.root.context
                        .getString(R.string.deleted), Toast.LENGTH_SHORT
                    ).show()
                }
            }, 300)
            actionMode?.finish()
        }
        .setNegativeButton(R.string.close) { _, _ -> }
        .show()
}