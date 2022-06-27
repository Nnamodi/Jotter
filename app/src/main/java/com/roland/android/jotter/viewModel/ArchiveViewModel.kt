package com.roland.android.jotter.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.roland.android.jotter.database.NoteDatabase
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.util.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ArchiveViewModel(app: Application) : AndroidViewModel(app) {
    private val repository: NoteRepository
    val getArchivedNotes: LiveData<List<Note>>
    var inputPIN = ""

    init {
        val noteDao = NoteDatabase.getDatabase(app).noteDao()
        repository = NoteRepository(noteDao)
        getArchivedNotes = repository.getArchivedNotes
    }

    private fun updateNote(note: Note) {
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