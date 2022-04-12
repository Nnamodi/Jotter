package com.roland.android.jotter.util

import androidx.lifecycle.LiveData
import com.roland.android.jotter.database.NoteDao
import com.roland.android.jotter.model.Note

class NoteRepository(private val noteDao: NoteDao) {
    val getNotes: LiveData<List<Note>> = noteDao.getNotes()

    suspend fun addNote(note: Note) {
        noteDao.addNote(note)
    }
}