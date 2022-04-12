package com.roland.android.jotter.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
@Entity
data class Note(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var date: Date = Date(),
    var time: Date = Date(),
    var title: String = "",
    var body: String = ""
) : Parcelable