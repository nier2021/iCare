package com.docter.icare.ui.base

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsCompat.Type.ime
import org.kodein.di.DIAware

abstract class BaseActivity : AppCompatActivity(), DIAware, View.OnClickListener {
    fun setStatusBarColor(color:Int){
        window.statusBarColor =  ContextCompat.getColor(this,color)
    }
    override fun finish() {

        WindowInsetsControllerCompat(window, findViewById(android.R.id.content)).hide(ime())

        super.finish()
    }

    override fun onClick(view: View) {

        WindowInsetsControllerCompat(window, findViewById(android.R.id.content)).hide(ime())
    }
}