package com.example.shoper.utils

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.example.shoper.R

/**
 *  Klasa wspierająca tworzenie okienek informacyjnych
 */
class Popup(val context: Context) {

    /**
     *  Wyświetla okienko z pojedyńczym guzikiem "OK"
     */
    fun showMessageOK(message: Int, onYes: () -> Unit = {}) {
        val builder = AlertDialog.Builder(context).setMessage(message)
        builder.setPositiveButton(R.string.ok) { _, _ -> }
        builder.create().show()
    }

    /**
     *  Wyświetla okienka z możliwością wybrania "Yes" oraz "No"
     */
    fun showMessageYesOrNo(message: Int, yes: Int = R.string.yes, no: Int = R.string.no, onYes: () -> Unit = {}, onNo: () -> Unit = {}) {
        showMessageYesOrNo(
            context.getString(message),
            yes,
            no,
            onYes,
            onNo
        )
    }

    /**
     *  Wyświetla okienka z możliwością wybrania "Yes" oraz "No"
     */
    fun showMessageYesOrNo(message: String, yes: Int = R.string.yes, no: Int = R.string.no, onYes: () -> Unit = {}, onNo: () -> Unit = {}) {
        val builder = AlertDialog.Builder(context).setMessage(message)
        builder.setPositiveButton(yes) { _, _ -> onYes() }
        builder.setNegativeButton(no) { _, _ -> onNo() }
        builder.create().show()
    }
}

/**
 *  Podpięcie do aktywności obsługi popup'ów
 */
fun Activity.popup(): Popup {
    return Popup(this)
}