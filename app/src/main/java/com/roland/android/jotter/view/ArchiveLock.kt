package com.roland.android.jotter.view

import android.content.Context
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.roland.android.jotter.R

class ArchiveLock : Fragment() {
    private lateinit var password: EditText
    private lateinit var incorrectPinText: TextView
    private lateinit var nextButton: Button

    @Suppress("DEPRECATION")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_archive_lock, container, false)
        val vibrate: () -> Unit = {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    (context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
                        .vibrate(
                            VibrationEffect.createOneShot(
                                200, VibrationEffect.DEFAULT_AMPLITUDE
                            )
                        )
                }
                Build.VERSION.SDK_INT == Build.VERSION_CODES.S -> {
                    (context?.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager)
                        .vibrate(
                            CombinedVibration.createParallel(
                                VibrationEffect.createOneShot(
                                    200, VibrationEffect.DEFAULT_AMPLITUDE
                                )
                            )
                        )
                }
                else -> {
                    (context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
                        .vibrate(200)
                }
            }
        }
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {
                // Intentionally forgotten
            }
            override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, after: Int) {
                if (incorrectPinText.isVisible) incorrectPinText.visibility = View.GONE
                nextButton.isEnabled = sequence?.count()!! >= 4
            }
            override fun afterTextChanged(sequence: Editable?) {
                // Intentionally forgotten
            }
        }
        password = view.findViewById(R.id.archive_password)
        password.addTextChangedListener(textWatcher)
        password.requestFocus()
        incorrectPinText = view.findViewById(R.id.incorrect_pin)
        nextButton = view.findViewById(R.id.next_button)
        nextButton.setOnClickListener {
            if (password.text.toString() == "9570") {
                findNavController().navigate(R.id.archiveFragment)
            } else {
                password.text.clear()
                incorrectPinText.visibility = View.VISIBLE
                vibrate()
            }
        }
        imm.showSoftInput(getView(), InputMethodManager.SHOW_IMPLICIT)
        return view
    }
}