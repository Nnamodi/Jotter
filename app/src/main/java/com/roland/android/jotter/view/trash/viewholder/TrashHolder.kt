package com.roland.android.jotter.view.trash.viewholder

import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.roland.android.jotter.databinding.JotterItemBinding
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.util.actionEnabled
import com.roland.android.jotter.util.allCards
import com.roland.android.jotter.util.callBack
import com.roland.android.jotter.util.select
import com.roland.android.jotter.view.trash.TrashFragmentDirections
import java.text.SimpleDateFormat
import java.util.*

class TrashHolder(private val binding: JotterItemBinding) : RecyclerView.ViewHolder(binding.root) {
    private val view = binding.root
    private val card = view as MaterialCardView

    fun bind(note: Note) {
        allCards.add(card)
        binding.apply {
            jotTitle.text = note.title
            jotBody.text = note.body
            dateText.text = SimpleDateFormat("d|M|yy", Locale.getDefault()).format(note.date)
            card.isChecked = note.selected

            root.apply {
                setOnClickListener {
                    if (actionEnabled.value == true) {
                        select(card, note)
                    } else {
                        val action = TrashFragmentDirections.trashToJot(note)
                        findNavController(binding.root).navigate(action)
                    }
                }

                setOnLongClickListener {
                    if (actionEnabled.value == false) {
                        startActionMode(callBack(note, binding, view))
                    } else {
                        select(card, note)
                    }
                    true
                }
            }
        }
    }
}