package com.roland.android.jotter.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.roland.android.jotter.R
import com.roland.android.jotter.model.Note
import com.roland.android.jotter.util.Preference
import com.roland.android.jotter.viewModel.JotterViewModel
import kotlin.properties.Delegates

@RequiresApi(Build.VERSION_CODES.M)
class JotBottomSheet : BottomSheetDialogFragment() {
    private lateinit var deleteNote: View
    private lateinit var shareNote: View
    private lateinit var textSetting: View
    private lateinit var archiveNote: View
    private lateinit var unarchiveNote: View
    private lateinit var viewModel: JotterViewModel
    private val args by navArgs<JotBottomSheetArgs>()
    private var originalTextColor by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        originalTextColor = if (Preference.getDarkMode(requireContext()))
        { resources.getColor(R.color.primaryTextColor, resources.newTheme()) }
        else { resources.getColor(R.color.black, resources.newTheme()) }
        viewModel = ViewModelProvider(this) [JotterViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.jot_bottom_sheet, container, false)
        deleteNote = view.findViewById(R.id.delete_note)
        deleteNote.setOnClickListener {
            deleteNote(args.utils)
        }
        shareNote = view.findViewById(R.id.share_note)
        shareNote.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getString(R.string.text, args.utils.title, args.utils.body))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject_text))
            }.also { intent ->
                val chooserIntent = Intent.createChooser(intent, getString(R.string.chooser_title))
                startActivity(chooserIntent)
            }
        }
        textSetting = view.findViewById(R.id.text_setting)
        textSetting.setOnClickListener {
            settingDialog(container)
        }
        archiveNote = view.findViewById(R.id.archive_note)
        archiveNote.setOnClickListener {
            viewModel.archiveNote(args.utils, true)
            findNavController().apply {
                previousBackStackEntry?.savedStateHandle?.set("archive", args.utils)
                navigateUp()
            }
        }
        unarchiveNote = view.findViewById(R.id.unarchive_note)
        unarchiveNote.setOnClickListener {
            viewModel.archiveNote(args.utils, false)
            findNavController().popBackStack(R.id.jotFragment, true)
            Toast.makeText(context, getString(R.string.jot_unarchived, args.utils.title), Toast.LENGTH_SHORT).show()
        }
        if (args.utils.archived) {
            archiveNote.visibility = View.GONE
            unarchiveNote.visibility = View.VISIBLE
        } else {
            archiveNote.visibility = View.VISIBLE
            unarchiveNote.visibility = View.GONE
        }
        return view
    }

    private fun deleteNote(note: Note) {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setMessage(getString(R.string.delete_message, note.title))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deleteNote(note)
                findNavController().popBackStack(R.id.jotFragment, true)
                Toast.makeText(context, getString(R.string.note_deleted_text), Toast.LENGTH_SHORT).show()
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