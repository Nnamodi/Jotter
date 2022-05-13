package com.roland.android.jotter.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.roland.android.jotter.util.NoteRepository
import com.roland.android.jotter.database.NoteDatabase
import com.roland.android.jotter.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JotterViewModel(app: Application) : AndroidViewModel(app) {
    private val repository: NoteRepository
    val getNotes: LiveData<List<Note>>

    init {
        val noteDao = NoteDatabase.getDatabase(app).noteDao()
        repository = NoteRepository(noteDao)
        getNotes = repository.getNotes
    }

    fun addNotes(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addNote(note)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNote(note)
        }
    }

    fun archiveNote(note: Note, archive: Boolean) {
        note.archived = archive
        updateNote(note)
    }
}