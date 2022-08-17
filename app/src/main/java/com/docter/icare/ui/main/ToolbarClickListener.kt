package com.docter.icare.ui.main

import android.app.Activity

interface ToolbarClickListener {
    fun onBackClick(activity: Activity) = activity.onBackPressed()
    fun onAddClick() {}
    fun onEditClick() {}
    fun onDoneClick() {}
}