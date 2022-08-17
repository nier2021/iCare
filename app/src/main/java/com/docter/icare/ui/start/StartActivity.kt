package com.docter.icare.ui.start

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.docter.icare.R
import com.docter.icare.databinding.ActivityStartBinding
import com.docter.icare.ui.base.BaseActivity
import com.docter.icare.utils.snackbar
import com.docter.icare.utils.toast
import com.docter.icare.view.dialog.CustomProgressDialog
import kotlinx.coroutines.launch
import org.kodein.di.android.closestDI
import org.kodein.di.instance

class StartActivity : BaseActivity() {

    override val di by closestDI()
    private val binding: ActivityStartBinding by lazy { DataBindingUtil.setContentView(this, R.layout.activity_start) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarColor(R.color.welcome_status_bar)

        with(binding){
            lifecycleOwner = this@StartActivity
        }
    }

}

