package com.roland.android.jotter.view.archive.viewholder

import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.roland.android.jotter.databinding.JotterItemBinding
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.view.archive.ArchiveFragmentDirections
import java.text.SimpleDateFormat
import java.util.*

class ArchiveHolder(private val binding: JotterItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(note: Note) {
        binding.jotTitle.text = note.title
        binding.jotBody.text = note.body
        binding.dateText.text = SimpleDateFormat("d|M|yy", Locale.getDefault()).format(note.date)

        binding.root.setOnClickListener {
            val action = ArchiveFragmentDirections.archiveToJot(note)
            findNavController(binding.root).navigate(action)
        }
    }
}