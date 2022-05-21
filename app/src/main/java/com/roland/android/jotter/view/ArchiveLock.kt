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
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.roland.android.jotter.R
import com.roland.android.jotter.util.Preference

class ArchiveLock : Fragment() {
    private lateinit var password: EditText
    private lateinit var incorrectPinText: TextView
    private lateinit var lockText: TextView
    private lateinit var pinTip: TextView
    private lateinit var nextButton: Button

    @Suppress("DEPRECATION")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_archive_lock, container, false)
        val pin = Preference.getPIN(requireContext())
        val args by navArgs<ArchiveLockArgs>()
        val vibrate: () -> Unit = {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    (context?.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager)
                        .vibrate(
                            CombinedVibration.createParallel(
                                VibrationEffect.createOneShot(
                                    200, VibrationEffect.DEFAULT_AMPLITUDE
                                )
                            )
                        )
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    (context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
                        .vibrate(
                            VibrationEffect.createOneShot(
                                200, VibrationEffect.DEFAULT_AMPLITUDE
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
        lockText = view.findViewById(R.id.lock_text)
        pinTip = view.findViewById(R.id.pin_tip)
        nextButton = view.findViewById(R.id.next_button)
        if (!args.changePassword) {
            nextButton.setOnClickListener {
                if (password.text.toString() == pin) {
                    findNavController().navigate(R.id.archiveFragment)
                } else {
                    password.text.clear()
                    incorrectPinText.visibility = View.VISIBLE
                    vibrate()
                }
            }
        } else {
            lockText.text = getString(R.string.old_pin)
            nextButton.setOnClickListener {
                if (password.text.toString() == pin) {
                    password.text.clear()
                    pinTip.visibility = View.VISIBLE
                    lockText.text = getString(R.string.new_pin)
                    nextButton.text = getString(R.string.set_button)
                    nextButton.setOnClickListener {
                        Preference.setPIN(requireContext(), password.text.toString())
                        pinTip.visibility = View.GONE
                        Snackbar.make(requireContext(), requireView(), getString(R.string.pin_changed_text), Snackbar.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                } else {
                    password.text.clear()
                    incorrectPinText.visibility = View.VISIBLE
                    vibrate()
                }
            }
        }
        imm.showSoftInput(getView(), InputMethodManager.SHOW_IMPLICIT)
        return view
    }
}