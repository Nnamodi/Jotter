package com.roland.android.jotter.view.trash.viewholder

import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.roland.android.jotter.databinding.JotterItemBinding
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.view.trash.TrashFragmentDirections
import java.text.SimpleDateFormat
import java.util.*

class TrashHolder(private val binding: JotterItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(note: Note) {
        binding.apply {
            jotTitle.text = note.title
            jotBody.text = note.body
            dateText.text = SimpleDateFormat("d|M|yy", Locale.getDefault()).format(note.date)

            root.setOnClickListener {
                val action = TrashFragmentDirections.trashToJot(note)
                findNavController(binding.root).navigate(action)
            }
        }
    }
}