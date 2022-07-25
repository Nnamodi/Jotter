package com.roland.android.jotter.view.main.viewholder

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.roland.android.jotter.databinding.JotterItemBinding
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.util.*
import com.roland.android.jotter.view.main.JotterFragmentDirections
import java.text.SimpleDateFormat
import java.util.*

class JotterHolder(val view: View, val binding: JotterItemBinding) : RecyclerView.ViewHolder(binding.root) {
    private val card = binding.root as MaterialCardView

    @SuppressLint("ClickableViewAccessibility")
    fun bind(note: Note) {
        allCards.add(card)
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
                        startActionMode(callBack(note, binding, view))
                    } else {
                        select(card, note)
                    }
                    true
                }

                // To get the particular note swiped.
                setOnTouchListener { _, event ->
                    var swiped = false
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            noteToSwipe = note
                            swiped = false
                        }
                        MotionEvent.ACTION_MOVE -> {
                            noteToSwipe = note
                            swiped = true
                        }
                    }
                    swiped
                }
            }
        }
    }
}