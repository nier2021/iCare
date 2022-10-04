package com.docter.icare.ui.main

import android.app.Activity

interface ToolbarClickListener {
    fun onBackClick(activity: Activity) = activity.onBackPressed()
    fun onExceptionClick() {}
    fun onEditClick() {}
    fun onDoneClick() {}
}