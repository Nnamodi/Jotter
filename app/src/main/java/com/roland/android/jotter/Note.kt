package com.roland.android.jotter

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Note(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var date: Date = Date(),
    var time: Date = Date(),
    var title: String = "",
    var body: String = ""
)