package com.roland.android.jotter.view.main.viewholder

import android.view.View
import android.widget.Toast
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.roland.android.jotter.databinding.JotterItemBinding
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.view.main.JotterFragmentDirections
import java.text.SimpleDateFormat
import java.util.*

class JotterHolder(private val binding: JotterItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(note: Note) {
        binding.apply {
            jotTitle.text = note.title
            jotBody.text = note.body
            dateText.text = SimpleDateFormat("d|M|yy", Locale.getDefault()).format(note.date)
            starredIcon.visibility = if (note.starred) View.VISIBLE else View.GONE

            root.apply {
                setOnClickListener {
                    val action = JotterFragmentDirections.moveToJot(note)
                    findNavController(binding.root).navigate(action)
                }

                setOnLongClickListener {
                    Toast.makeText(
                        binding.root.context,
                        "Select feature coming soon...",
                        Toast.LENGTH_SHORT
                    ).show()
                    true
                }
            }
        }
    }
}