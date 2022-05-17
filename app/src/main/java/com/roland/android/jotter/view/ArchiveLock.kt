package com.roland.android.jotter.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.roland.android.jotter.R

class ArchiveLock : Fragment() {
    private lateinit var password: EditText
    private lateinit var incorrectPinText: TextView
    private lateinit var nextButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_archive_lock, container, false)
        password = view.findViewById(R.id.archive_password)
        incorrectPinText = view.findViewById(R.id.incorrect_pin)
        nextButton = view.findViewById(R.id.next_button)
        nextButton.setOnClickListener {
            if (password.text.toString() == "1234") {
                findNavController().navigate(R.id.archiveFragment)
            } else {
                incorrectPinText.visibility = View.VISIBLE
            }
        }
        return view
    }
}