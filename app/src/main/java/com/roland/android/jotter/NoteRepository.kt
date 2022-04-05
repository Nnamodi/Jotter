package com.roland.android.jotter

import androidx.lifecycle.LiveData

class NoteRepository(private val noteDao: NoteDao) {
    val getNotes: LiveData<List<Note>> = noteDao.getNotes()

    suspend fun addNote(note: Note) {
        noteDao.addNote(note)
    }
}