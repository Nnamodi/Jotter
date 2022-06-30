package com.roland.android.jotter.util

import android.app.Activity
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.roland.android.jotter.R
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.viewModel.JotterViewModel

fun Activity.swipeCallback(view: View, recyclerView: RecyclerView, note: List<Note>, viewModel: JotterViewModel) {
    ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ) = true

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val index = viewHolder.bindingAdapterPosition
            viewModel.archiveNote(note[index], true)
            Snackbar.make(view, getString(R.string.archived), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.undo)) {
                    viewModel.archiveNote(note[index], false)
                }.show()
        }
    }).attachToRecyclerView(recyclerView)
    Log.d("ItemPosition", "Note increased = ${note.size == +1}")
}