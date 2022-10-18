package com.docter.icare.view.dialog

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.View
import com.docter.icare.databinding.DialogTemperatureSetBinding
import android.view.View.inflate
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import com.docter.icare.R
import com.docter.icare.utils.Coroutines.main
import com.docter.icare.utils.InputException
import com.docter.icare.utils.hideKeyboard
import com.docter.icare.utils.hideSoftKeyboard
import com.docter.icare.utils.toast
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

class TemperatureSetDialog (
    context: Context,
) {

    private val activity = context as Activity

    private var binding: DialogTemperatureSetBinding

    private val view = inflate(context, R.layout.dialog_temperature_set, null)

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

        setTextChangedListener()
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
                    clickListener.onConfirmClick(String.format("%.2f",etTemperature.text.toString().toFloat()))
//                    clickListener.onConfirmClick(etTemperature.text.toString().toFloat().toString())
                }.onFailure {
                    it.printStackTrace()
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
            binding.etTemperature.text?.isBlank() == true -> throw InputException(resource.getString(R.string.error_temperature_calibration_isBlank))
            binding.etTemperature.text!!.length > 5 -> throw InputException(resource.getString(R.string.temperature_calibration_hit))
            binding.etTemperature.text!!.toString().toFloat() !in 35f..42f  -> throw InputException(resource.getString(R.string.temperature_calibration_hit))
        }
    }

    private fun clean() = binding.etTemperature.text?.clear()

    private fun setTextChangedListener(){
        binding.etTemperature.doOnTextChanged { text, start, before, count ->
            if (text.toString().startsWith("0") && text.toString().trim().length > 1){
                //第一個值為0 第二個值不為. 不能輸入
                if (text.toString().substring(1,2) != "."){
                    binding.etTemperature.apply {
                        setText(text?.subSequence(0,1))
                        setSelection(1)
                        return@doOnTextChanged
                    }
                }
            }

            //第一個值為. 會直接顯示0.
            if (text.toString().startsWith(".")){
                binding.etTemperature.apply {
                    setText("0.")
                    setSelection(2)
                    return@doOnTextChanged
                }
            }

            //限制小數兩位
            val POINT_LENGTHG = 2
            if (text.toString().contains(".") && text.toString().isNotEmpty()){
                if (text!!.length - 1- text.toString().indexOf(".") > POINT_LENGTHG){
                    val showText = text.toString().subSequence(0,text.toString().indexOf(".") + POINT_LENGTHG + 1)
                    binding.etTemperature.apply {
                        setText(showText)
                        setSelection(showText.length)
                        return@doOnTextChanged
                    }
                }
            }


        }
    }

     private fun hideKeyboard() =  view?.let { activity.hideKeyboard(it) }

    interface OnClickListener {
        fun onConfirmClick(temperature: String)
        fun onCancelClick()
    }

}