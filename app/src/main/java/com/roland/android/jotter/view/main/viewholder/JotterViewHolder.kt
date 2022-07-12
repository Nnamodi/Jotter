package com.roland.android.jotter.view.main.viewholder

import android.view.View
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.roland.android.jotter.databinding.JotterItemBinding
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.util.actionEnabled
import com.roland.android.jotter.util.callBack
import com.roland.android.jotter.util.select
import com.roland.android.jotter.view.main.JotterFragmentDirections
import java.text.SimpleDateFormat
import java.util.*

class JotterHolder(private val binding: JotterItemBinding) : RecyclerView.ViewHolder(binding.root) {
    private val card = binding.root as MaterialCardView

    fun bind(note: Note) {
        binding.apply {
            jotTitle.text = note.title
            jotBody.text = note.body
            dateText.text = SimpleDateFormat("d|M|yy", Locale.getDefault()).format(note.date)
            starredIcon.visibility = if (note.starred) View.VISIBLE else View.GONE
            card.isChecked = note.selected

            root.apply {
                setOnClickListener {
                    if (actionEnabled.value == true) {
                        select(card, note)
                    } else {
                        val action = JotterFragmentDirections.moveToJot(note)
                        findNavController(binding.root).navigate(action)
                    }
                }

                setOnLongClickListener {
                    if (actionEnabled.value == false) {
                        startActionMode(callBack(card, note, binding))
                    } else {
                        select(card, note)
                    }
                    true
                }
            }
        }
    }
}