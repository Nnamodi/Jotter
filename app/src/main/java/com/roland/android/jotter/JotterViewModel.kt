package com.roland.android.jotter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
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
}