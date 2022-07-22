package com.roland.android.jotter.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.roland.android.jotter.database.NoteDatabase
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrashViewModel(val app: Application) : AndroidViewModel(app) {
    private val repository: NoteRepository
    val getTrashedNotes: LiveData<List<Note>>
    var actionWasEnabled = false
    var manyWereSelected = false
    var allWereSelected = false

    init {
        val noteDao = NoteDatabase.getDatabase(app).noteDao()
        repository = NoteRepository(noteDao)
        getTrashedNotes = repository.getTrashedNotes
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNote(note)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNote(note)
        }
    }

    fun trashNote(note: Note, archive: Boolean, trash: Boolean) {
        note.trashed = trash
        note.archived = archive
        updateNote(note)
    }
}