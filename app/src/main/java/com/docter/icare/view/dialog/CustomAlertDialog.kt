package com.docter.icare.view.dialog

import com.docter.icare.databinding.DialogAlertBinding
import android.app.Activity
import android.view.Gravity.CENTER
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.VISIBLE
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_PARENT
import com.docter.icare.R


@Suppress("unused")
class CustomAlertDialog(
    private val activity: Activity
) {

    private val binding: DialogAlertBinding by lazy {
        DialogAlertBinding.inflate(LayoutInflater.from(activity))
    }
    private val popupWindow: PopupWindow by lazy {

        PopupWindow(binding.root, MATCH_PARENT, MATCH_PARENT, false).apply {
            animationStyle = R.style.AlertDialogAnimation
            isClippingEnabled = false
        }
    }

    fun setTitle(
        title: Int
    ) = apply { with(binding.title) { setText(title) } }

    fun setTitle(
        title: String
    ) = apply { with(binding.title) { text = title } }

    fun setTitle(
        title: Int,
        color: Int
    ) = apply {

        with(binding.title) {

            setText(title)

            setTextColor(color)
        }
    }

    fun setMessage(
        message: Int
    ) = apply { with(binding.message) { setText(message) } }

    fun setMessage(
        message: String
    ) = apply { with(binding.message) { text = message } }

    fun setPositiveButton(
        text: Int
    ) = apply {

        with(binding.positive) {

            setText(text)

            setOnClickListener { view -> view.postDelayed({ popupWindow.dismiss() }, 100) }
        }
    }

    fun setPositiveButton(
        text: Int,
        listener: OnClickListener
    ) = apply {

        with(binding.positive) {

            setText(text)

            setOnClickListener { view ->

                view.postDelayed({

                    popupWindow.dismiss()

                    listener.onClick(view)

                }, 100)
            }
        }
    }

    fun setNegativeButton(
        text: Int
    ) = apply {

        with(binding.negative) {

            visibility = VISIBLE

            setText(text)

            setOnClickListener { view -> view.postDelayed({ popupWindow.dismiss() }, 100) }
        }
    }

    fun setNegativeButton(
        text: Int,
        listener: OnClickListener
    ) = apply {

        with(binding.negative) {

            visibility = VISIBLE

            setText(text)

            setOnClickListener { view ->

                view.postDelayed({

                    popupWindow.dismiss()

                    listener.onClick(view)

                }, 100)
            }
        }
    }

    fun show() {

        if (!popupWindow.isShowing && !activity.isFinishing) {
            activity.findViewById<View>(android.R.id.content)?.let { it.post { popupWindow.showAtLocation(it, CENTER, 0, 0) } }
        }
    }

    fun isShowing() = popupWindow.isShowing

    fun dismiss() {

        if (popupWindow.isShowing && !activity.isFinishing) {
            activity.findViewById<View>(android.R.id.content).post { popupWindow.dismiss() }
        }
    }
}