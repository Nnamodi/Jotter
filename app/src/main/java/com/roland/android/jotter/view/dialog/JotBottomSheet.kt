package com.roland.android.jotter.view.dialog

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.roland.android.jotter.R
import com.roland.android.jotter.databinding.JotBottomSheetBinding
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.util.Preference
import com.roland.android.jotter.viewModel.JotterViewModel
import kotlin.properties.Delegates

@RequiresApi(Build.VERSION_CODES.M)
class JotBottomSheet : BottomSheetDialogFragment() {
    private lateinit var viewModel: JotterViewModel
    private var _binding: JotBottomSheetBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<JotBottomSheetArgs>()
    private var originalTextColor by Delegates.notNull<Int>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = JotBottomSheetBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this) [JotterViewModel::class.java]
        originalTextColor = if (Preference.getDarkMode(requireContext()))
        { resources.getColor(R.color.primaryTextColor, resources.newTheme()) }
        else { resources.getColor(R.color.black, resources.newTheme()) }
        binding.apply {
            trashNote.setOnClickListener {
                trashNote(args.utils)
            }
            shareNote.setOnClickListener {
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        getString(R.string.text, args.utils.title, args.utils.body)
                    )
                    putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject_text))
                }.also { intent ->
                    val chooserIntent =
                        Intent.createChooser(intent, getString(R.string.chooser_title))
                    startActivity(chooserIntent)
                }
            }
            textSetting.setOnClickListener {
                settingDialog(container)
            }
            archiveNote.setOnClickListener {
                viewModel.archiveNote(args.utils, true)
                findNavController().apply {
                    previousBackStackEntry?.savedStateHandle?.set("archive", args.utils)
                    navigateUp()
                }
            }
            unarchiveNote.setOnClickListener {
                viewModel.archiveNote(args.utils, false)
                findNavController().apply {
                    previousBackStackEntry?.savedStateHandle?.set("unarchive", args.utils)
                    popBackStack(R.id.jotFragment, true)
                }
            }
            if (args.utils.archived) {
                archiveNote.visibility = View.GONE
                unarchiveNote.visibility = View.VISIBLE
            } else {
                archiveNote.visibility = View.VISIBLE
                unarchiveNote.visibility = View.GONE
            }
        }
        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) { _binding = null }
        })
        return binding.root
    }

    private fun trashNote(note: Note) {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(getString(R.string.move_to_trash_))
            .setMessage(getString(R.string.delete_this_note, note.title))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.trashNote(note, archive = false, trash = true)
                findNavController().apply {
                    previousBackStackEntry?.savedStateHandle?.set("trashed", args.utils)
                    popBackStack(R.id.jotFragment, true)
                }
            }
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .show()
    }

    private fun settingDialog(container: ViewGroup?) {
        val view = layoutInflater.inflate(R.layout.jot_text_setting, container)
        val seekBar = view.findViewById<SeekBar>(R.id.seekBar)
        val smallerText = view.findViewById<TextView>(R.id.smaller)
        val largerText = view.findViewById<TextView>(R.id.larger)
        val greyed = resources.getColor(R.color.grey, resources.newTheme())
        seekBar.progress = when (Preference.getSize(requireContext())) {
            16 -> { smallerText.setTextColor(greyed); 0 }
            18 -> { 1 }
            20 -> { 2 }
            22 -> { 3 }
            24 -> { 4 }
            else -> { largerText.setTextColor(greyed); 5 }
        }
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val textSize = when (progress) {
                    0 -> { 16 }
                    1 -> { 18 }
                    2 -> { 20 }
                    3 -> { 22 }
                    4 -> { 24 }
                    else -> { 26 }
                }
                smallerText.setTextColor(if (progress == 0) greyed else originalTextColor)
                largerText.setTextColor(if (progress == 5) greyed else originalTextColor)
                findNavController().previousBackStackEntry?.savedStateHandle?.set("text_size", textSize.toFloat())
                Preference.setSize(requireContext(), textSize)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        val dialog = MaterialAlertDialogBuilder(requireContext())
        dialog.setView(view)
            .setPositiveButton(getString(R.string.close)) { _, _ -> }
            .setCancelable(false)
            .show()
    }
}