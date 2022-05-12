package com.roland.android.jotter.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.roland.android.jotter.model.Note

@Dao
interface NoteDao {
    @Query("SELECT * FROM note ORDER BY id DESC")
    fun getNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM note WHERE archived LIKE :value ORDER BY id DESC")
    fun getArchivedNotes(value: Boolean): LiveData<List<Note>>

    @Insert
    suspend fun addNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)
}