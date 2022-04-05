package com.roland.android.jotter

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NoteDao {
    @Query("SELECT * FROM note ORDER BY id ASC")
    fun getNotes(): LiveData<List<Note>>

    @Insert
    suspend fun addNote(note: Note)
}