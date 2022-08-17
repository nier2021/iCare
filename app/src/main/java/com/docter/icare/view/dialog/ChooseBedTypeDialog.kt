package com.docter.icare.view.dialog

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.docter.icare.R
import com.docter.icare.databinding.DialogChooseBedTypeBinding
import com.docter.icare.utils.Coroutines
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

class ChooseBedTypeDialog(context: Context) {

    private val activity = context as Activity

    private var binding: DialogChooseBedTypeBinding
    private val view = View.inflate(context, R.layout.dialog_choose_bed_type, null)

    private var popupWindow: PopupWindow

    private var chooseBedType: Int = 1

    init {

        binding = DataBindingUtil.bind(view)!!

        popupWindow = PopupWindow(
            view,
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            false
        ).apply {
            isClippingEnabled = false
        }
    }

    fun setClickListener(
        clickListener: OnClickListener
    ) {
        with(binding) {
            btnCancel.setOnClickListener {
                Observable.timer(100, TimeUnit.MILLISECONDS)
                    .subscribe { Coroutines.main { popupWindow.dismiss() } }
                clickListener.onConfirmClick(false,chooseBedType)
            }

            btnConfirm.setOnClickListener {
                Observable.timer(100, TimeUnit.MILLISECONDS)
                    .subscribe { Coroutines.main { popupWindow.dismiss() } }
               clickListener.onConfirmClick(true,chooseBedType)
            }


            rgBedType.setOnCheckedChangeListener { group, checkedId ->
                Log.i("ChooseBedTypeDialog","id=>$checkedId")
                chooseBedType =  when (checkedId) {
                    R.id.cb_bed_type_2 -> 2
                    R.id.cb_bed_type_3 -> 3
                    R.id.cb_bed_type_4 -> 4
                    else -> 1
                }

            }
        }
    }

    fun saveBedType(saveBedType: Int){
        chooseBedType = saveBedType
        when (saveBedType) {
            2 -> binding.cbBedType2.isChecked = true
            3 -> binding.cbBedType3.isChecked = true
            4 -> binding.cbBedType4.isChecked = true
            else -> binding.cbBedType1.isChecked = true
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

    interface OnClickListener {
        fun onConfirmClick(changeData:Boolean, chooseBedType: Int)
    }

}