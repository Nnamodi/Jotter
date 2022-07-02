package com.roland.android.jotter.view.archive.viewholder

import android.view.View
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.roland.android.jotter.databinding.JotterItemBinding
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.view.archive.ArchiveFragmentDirections
import java.text.SimpleDateFormat
import java.util.*

class ArchiveHolder(private val binding: JotterItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(note: Note) {
        binding.apply {
            jotTitle.text = note.title
            jotBody.text = note.body
            dateText.text = SimpleDateFormat("d|M|yy", Locale.getDefault()).format(note.date)
            starredIcon.visibility = if (note.starred) View.VISIBLE else View.GONE

            root.setOnClickListener {
                val action = ArchiveFragmentDirections.archiveToJot(note)
                findNavController(binding.root).navigate(action)
            }
        }
    }
}