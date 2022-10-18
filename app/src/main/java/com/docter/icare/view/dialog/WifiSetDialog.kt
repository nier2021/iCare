package com.docter.icare.view.dialog

import android.app.Activity
import android.content.Context
import com.docter.icare.R
import com.docter.icare.databinding.DialogWifiSetBinding
import android.view.Gravity
import android.view.View.inflate
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.docter.icare.utils.Coroutines.main
import com.docter.icare.utils.InputException
import com.docter.icare.utils.hideKeyboard
import com.docter.icare.utils.isSpecialSymbols
import com.docter.icare.utils.toast
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

class WifiSetDialog(
    context: Context,
) {

    private val activity = context as Activity

    private var binding: DialogWifiSetBinding

    private val view = inflate(context, R.layout.dialog_wifi_set, null)

    private var popupWindow: PopupWindow

    private var resource = context.resources

    init {
        binding = DataBindingUtil.bind(view)!!

        popupWindow = PopupWindow(view,
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT, false).apply {
            isClippingEnabled = false
            isFocusable = true
            update()
        }

    }

    fun setClickListener(
        clickListener: OnClickListener
    ) {
        with(binding) {
            cancel.setOnClickListener {
                Observable.timer(100, TimeUnit.MILLISECONDS).subscribe {
                    main {
                        clean()
                        popupWindow.dismiss()
                        hideKeyboard()
                        clickListener.onCancelClick()
                    }
                }
            }



            confirm.setOnClickListener {
                runCatching {
                    checkInput()
                }.onSuccess {

                    Observable.timer(100, TimeUnit.MILLISECONDS)
                        .subscribe { main {
                            popupWindow.dismiss()
                            clean()
                        } }
                    hideKeyboard()
                    clickListener.onConfirmClick(etWifiAccount.text.toString(), etWifiPassword.text.toString())
                }.onFailure {
                    it.printStackTrace()
//                    binding.root.snackbar(it)
                    main{
                        activity.toast(it)
                    }
                }
            }
        }
    }

    fun show() {

        if (!popupWindow.isShowing) {

            if (!activity.isFinishing) {
                popupWindow.showAtLocation(view, Gravity.BOTTOM,0,0)
            }
        }
    }

    fun isShowing() = popupWindow.isShowing

    fun dismiss() = popupWindow.dismiss()

    private fun checkInput(){

        when {
            binding.etWifiAccount.text?.isBlank() == true -> throw InputException(resource.getString(R.string.error_wifi_account_empty))
            binding.etWifiAccount.text!!.length > 20 -> throw InputException(resource.getString(R.string.error_wifi_length))

            binding.etWifiPassword.text?.isBlank() == true -> throw InputException(resource.getString(R.string.error_wifi_password_empty))
            binding.etWifiPassword.text!!.length > 20 -> throw InputException(resource.getString(R.string.error_wifi_length))
            binding.etWifiAccount.text!!.toString().isSpecialSymbols() || binding.etWifiPassword.text!!.toString().isSpecialSymbols()-> throw InputException(resource.getString(R.string.error_wifi_special_symbols))
        }
    }


    private fun clean(){
        with(binding){
            etWifiAccount.setText("")
            etWifiPassword.setText("")
        }
    }

    private fun hideKeyboard() = view?.let { activity.hideKeyboard(it) }

    interface OnClickListener {
        fun onConfirmClick(wifiAccount: String, wifiPassword: String)
        fun onCancelClick()
    }
}