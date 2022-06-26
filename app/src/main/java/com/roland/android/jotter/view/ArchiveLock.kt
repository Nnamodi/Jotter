package com.roland.android.jotter.view

import android.content.Context
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.roland.android.jotter.R
import com.roland.android.jotter.util.Preference
import com.roland.android.jotter.viewModel.ArchiveViewModel

class ArchiveLock : Fragment() {
    private lateinit var viewModel: ArchiveViewModel
    private lateinit var password: EditText
    private lateinit var incorrectPinText: TextView
    private lateinit var lockText: TextView
    private lateinit var pinTip: TextView
    private lateinit var nextButton: Button
    private lateinit var lockImage: ImageView

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
        val unlock: () -> Unit = {
            if (password.text.toString() == pin) {
                lockImage.setImageResource(R.drawable.unlock_icon)
                Handler(Looper.getMainLooper()).postDelayed({
                    findNavController().navigate(R.id.archiveFragment)
                }, 500)
            } else {
                password.text.clear()
                incorrectPinText.visibility = View.VISIBLE
                vibrate()
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
        viewModel = ViewModelProvider(this) [ArchiveViewModel::class.java]
        password = view.findViewById(R.id.archive_password)
        password.addTextChangedListener(textWatcher)
        password.requestFocus()
        incorrectPinText = view.findViewById(R.id.incorrect_pin)
        lockImage = view.findViewById(R.id.lock_image)
        lockText = view.findViewById(R.id.lock_text)
        pinTip = view.findViewById(R.id.pin_tip)
        nextButton = view.findViewById(R.id.next_button)
        when (args.changePassword) {
            "change" -> {
                lockText.text = getString(R.string.old_pin)
                nextButton.setOnClickListener {
                    changePIN(pin) { vibrate() }
                }
                configureSoftKeyboard { changePIN(pin) { vibrate() } }
            }
            "set" -> {
                pinTip.visibility = View.VISIBLE
                nextButton.setOnClickListener {
                    setPIN { vibrate() }
                }
                configureSoftKeyboard { setPIN(vibrate) }
            }
            else -> {
                nextButton.setOnClickListener {
                    unlock()
                }
                configureSoftKeyboard(unlock)
            }
        }
        imm.showSoftInput(getView(), InputMethodManager.SHOW_IMPLICIT)
        return view
    }

    private fun changePIN(pin: String, vibrate: () -> Unit) {
        val confirmPIN = {
            if (password.text.toString() == viewModel.inputPIN) {
                Preference.setPIN(requireContext(), password.text.toString())
                findNavController().apply {
                    previousBackStackEntry?.savedStateHandle?.set("PIN", "change")
                    navigateUp()
                }
            } else {
                password.text.clear()
                incorrectPinText.text = getString(R.string.pin_does_not_match)
                incorrectPinText.visibility = View.VISIBLE
                vibrate()
            }
        }
        val newPIN = {
            viewModel.inputPIN = password.text.toString()
            lockText.text = getString(R.string.confirm_pin)
            pinTip.visibility = View.GONE
            password.text.clear()
            nextButton.apply {
                text = getString(R.string.set_button)
                setOnClickListener {
                    confirmPIN()
                }
            }
            configureSoftKeyboard { confirmPIN() }
        }
        if (password.text.toString() == pin) {
            password.text.clear()
            pinTip.visibility = View.VISIBLE
            viewModel.inputPIN = password.text.toString()
            lockText.text = getString(R.string.new_pin)
            nextButton.setOnClickListener {
                newPIN()
            }
            configureSoftKeyboard { newPIN() }
        } else {
            password.text.clear()
            incorrectPinText.visibility = View.VISIBLE
            vibrate()
        }
    }

    private fun setPIN(vibrate: () -> Unit) {
        val confirmPIN = {
            if (password.text.toString() == viewModel.inputPIN) {
                Preference.setPIN(requireContext(), password.text.toString())
                findNavController().apply {
                    previousBackStackEntry?.savedStateHandle?.set("PIN", "set")
                    navigateUp()
                }
            } else {
                password.text.clear()
                incorrectPinText.text = getString(R.string.pin_does_not_match)
                incorrectPinText.visibility = View.VISIBLE
                vibrate()
            }
        }
        viewModel.inputPIN = password.text.toString()
        lockText.text = getString(R.string.confirm_pin)
        pinTip.visibility = View.GONE
        password.text.clear()
        nextButton.apply {
            text = getString(R.string.set_button)
            setOnClickListener {
                confirmPIN()
            }
        }
        configureSoftKeyboard { confirmPIN() }
    }

    // To configure the `next button` on the soft keyboard to function in unison with the view's next button
    private fun configureSoftKeyboard(input: () -> Unit) {
        password.setOnEditorActionListener { _, id, _ ->
            var handled = false
            if (id == EditorInfo.IME_ACTION_NEXT && password.text.count() >= 4) {
                input()
                handled = true
            } else if (password.text.count() < 4) {
                val toast = Toast.makeText(requireContext(), getString(R.string.pin_tip_toast), Toast.LENGTH_SHORT)
                toast.setGravity(0, 0, 200)
                toast.show()
                handled = true
            }
            handled
        }
    }
}