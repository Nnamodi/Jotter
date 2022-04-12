package com.roland.android.jotter.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.roland.android.jotter.model.Note

@Dao
interface NoteDao {
    @Query("SELECT * FROM note ORDER BY id DESC")
    fun getNotes(): LiveData<List<Note>>

    @Insert
    suspend fun addNote(note: Note)
}