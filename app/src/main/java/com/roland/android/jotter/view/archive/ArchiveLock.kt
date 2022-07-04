package com.roland.android.jotter.view.archive

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.roland.android.jotter.R
import com.roland.android.jotter.databinding.FragmentArchiveLockBinding
import com.roland.android.jotter.util.Preference
import com.roland.android.jotter.viewModel.ArchiveViewModel

class ArchiveLock : Fragment() {
    private lateinit var viewModel: ArchiveViewModel
    private var _binding: FragmentArchiveLockBinding? = null
    private val binding get() = _binding!!
    private val password = binding.archivePassword
    private val incorrectPinText = binding.incorrectPin
    private val lockText = binding.lockText
    private val pinTip = binding.pinTip
    private val nextButton = binding.nextButton
    private val lockImage = binding.lockImage

    @Suppress("DEPRECATION")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentArchiveLockBinding.inflate(inflater, container, false)
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
        password.addTextChangedListener(textWatcher)
        password.requestFocus()
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
        imm.showSoftInput(binding.root, InputMethodManager.SHOW_IMPLICIT)
        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) { _binding = null }
        })
        return binding.root
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