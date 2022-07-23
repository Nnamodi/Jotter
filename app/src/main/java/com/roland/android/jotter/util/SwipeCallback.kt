package com.roland.android.jotter.util

import android.app.Activity
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.roland.android.jotter.R
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.viewModel.JotterViewModel

var noteToSwipe = Note()

fun Activity.swipeCallback(view: View, recyclerView: RecyclerView, viewModel: JotterViewModel, lifecycle: LifecycleOwner) {
    ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ) = true

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            viewModel.archiveNote(noteToSwipe, true)
            Snackbar.make(view, getString(R.string.archived), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.undo)) {
                    viewModel.archiveNote(noteToSwipe, false)
                }.show()
        }

        // Disabled item swipe when actionMode is enabled.
        override fun isItemViewSwipeEnabled(): Boolean {
            var isSwipeEnabled = super.isItemViewSwipeEnabled()
            actionEnabled.observe(lifecycle) { enabled ->
                isSwipeEnabled = !enabled
            }
            return isSwipeEnabled
        }
    }).attachToRecyclerView(recyclerView)
}