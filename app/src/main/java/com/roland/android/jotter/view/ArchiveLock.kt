package com.roland.android.jotter.view

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import com.roland.android.jotter.R

class ArchiveLock(val context: Context, val view: View) {
    fun enterPassword() {
        val editText = EditText(context)
        editText.setSingleLine()
        val dialog = AlertDialog.Builder(context)
        dialog.setTitle(context.getString(R.string.input_password))
        dialog.setView(editText)
        dialog.setPositiveButton(context.getString(R.string.ok)) { _, _ ->
            if (editText.text.toString() == "1234") {
                Navigation.findNavController(view).navigate(R.id.archiveFragment)
            } else {
                val toast = Toast.makeText(context, context.getString(R.string.incorrect_password), Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }
        }
        dialog.setNegativeButton(context.getString(android.R.string.cancel)) { _, _ -> }
        dialog.setCancelable(false)
        dialog.create().show()
    }
}