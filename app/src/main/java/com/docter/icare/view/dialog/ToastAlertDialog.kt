package com.docter.icare.view.dialog

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.View.inflate
import android.view.View
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.docter.icare.R
import com.docter.icare.databinding.DialogToastAlertBinding
import com.docter.icare.utils.Coroutines

class ToastAlertDialog(
    context: Context,
) {
    private val activity = context as Activity

     private var binding: DialogToastAlertBinding

     private val view = inflate(context, R.layout.dialog_toast_alert, null)

    private var popupWindow: PopupWindow
//    private val binding: DialogToastAlertBinding by lazy {
//        DialogToastAlertBinding.inflate(LayoutInflater.from(activity))
//    }

//    private val popupWindow: PopupWindow by lazy {
//
//        PopupWindow(binding.root, ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT, false).apply {
//            animationStyle = R.style.AlertDialogAnimation
//            isClippingEnabled = false
//        }
//    }


    init {
        binding = DataBindingUtil.bind(view)!!

        popupWindow = PopupWindow(view,
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT, false).apply {
            isClippingEnabled = false
        }

    }

    fun setMessage(
        message: String
    ) = apply { with(binding.tvMessage) { text = message } }


    fun setButton(
        listener: View.OnClickListener
    ) = apply {

        with(binding.btnOk) {

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
            activity.findViewById<View>(android.R.id.content)?.let { it.post { popupWindow.showAtLocation(it,
                Gravity.CENTER, 0, 0) } }
        }
    }

    fun dismiss() {
        Coroutines.main { if (popupWindow.isShowing) popupWindow.dismiss() }
    }

}