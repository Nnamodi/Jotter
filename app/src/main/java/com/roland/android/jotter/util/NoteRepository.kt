package com.roland.android.jotter.util

import androidx.lifecycle.LiveData
import com.roland.android.jotter.database.NoteDao
import com.roland.android.jotter.model.Note

class NoteRepository(private val noteDao: NoteDao) {
    val getNotes: LiveData<List<Note>> = noteDao.getNotes(archive = false, trash = false)

    val getArchivedNotes: LiveData<List<Note>> = noteDao.getNotes(archive = true, trash = false)

    val getTrashedNotes: LiveData<List<Note>> = noteDao.getNotes(archive = false, trash = true)

    suspend fun addNote(note: Note) {
        noteDao.addNote(note)
    }

    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }

    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }
}