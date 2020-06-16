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
    fun showMessageOK(message: Int) {
        val builder = AlertDialog.Builder(context).setMessage(message)
        builder.setPositiveButton(R.string.ok) { _, _ -> }
        builder.create().show()
    }
}

/**
 *  Podpięcie do aktywności obsługi popup'ów
 */
fun Activity.popup(): Popup {
    return Popup(this)
}