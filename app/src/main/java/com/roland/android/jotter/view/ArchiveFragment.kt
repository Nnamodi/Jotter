package com.roland.android.jotter.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.roland.android.jotter.R

class ArchiveFragment : Fragment() {
    private lateinit var archiveRecyclerView: RecyclerView
    private lateinit var archiveEmptyText: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_archive, container, false)
        archiveRecyclerView = view.findViewById(R.id.archive_recycler_view)
        archiveEmptyText = view.findViewById(R.id.archive_empty_text)
        return view
    }
}