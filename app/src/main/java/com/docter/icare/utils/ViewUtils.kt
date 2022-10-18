package com.docter.icare.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.WindowInsetsCompat.Type.ime
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import com.docter.icare.R
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import java.net.ConnectException

fun Context.toast(message: Int) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.toast(t: Throwable) {

    when (t) {
        is NoInternetException, is ApiConnectFailException, is InputException, is SidException -> t.message?.let { toast(it) }
        is ConnectException -> toast(R.string.api_connect_fail)
    }
}

fun View.snackbar(message: Int) = Snackbar.make(this, message, LENGTH_SHORT).apply { setAction("OK") { dismiss() } }.show()

fun View.snackbar(message: String) = Snackbar.make(this, message, LENGTH_SHORT).apply { setAction("OK") { dismiss() } }.show()

fun View.snackbar(t: Throwable) {

    when (t) {
        is NoInternetException, is ApiConnectFailException, is InputException, is SidException -> t.message?.let { snackbar(it) }
        is ConnectException -> snackbar(R.string.api_connect_fail)
    }
}

fun Window.hideSoftKeyboard() = with(WindowInsetsControllerCompat(this, findViewById(android.R.id.content))) {
    hide(ime())
    systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
}

fun Intent.clearStack() {
    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
}


fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}