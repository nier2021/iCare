package com.docter.icare.view.dialog

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import com.docter.icare.databinding.DialogProgressBinding
import com.docter.icare.utils.Coroutines.main
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_PARENT
import com.docter.icare.R
import android.view.Gravity.CENTER

class CustomProgressDialog (
    private val activity: Activity,
    private val message: Int
) {
    private val binding: DialogProgressBinding by lazy { DialogProgressBinding.inflate(
        LayoutInflater.from(activity)) }

    private val popupWindow: PopupWindow by lazy {

        PopupWindow(binding.root, MATCH_PARENT, MATCH_PARENT, false).apply {
            animationStyle = R.style.AlertDialogAnimation
            isClippingEnabled = false
        }
    }

    init { binding.content.setText(message) }

    fun setText(
        message: String
    ) = apply { with(binding.content) { text = message } }

    fun show() {

        if (!popupWindow.isShowing && !activity.isFinishing) {
            activity.findViewById<View>(android.R.id.content)?.let { it.post { popupWindow.showAtLocation(it, CENTER, 0, 0) } }
        }
    }

    fun dismiss() { main { if (popupWindow.isShowing) popupWindow.dismiss() } }

}