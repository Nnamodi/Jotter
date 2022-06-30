package com.roland.android.jotter.view.archive.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.roland.android.jotter.databinding.JotterItemBinding
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.view.archive.viewholder.ArchiveHolder

class ArchiveAdapter : ListAdapter<Note, ArchiveHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchiveHolder {
        val binding = JotterItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArchiveHolder(binding)
    }

    override fun onBindViewHolder(holder: ArchiveHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class DiffCallback : DiffUtil.ItemCallback<Note>()  {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
}