package com.docter.icare.ui.personInfo.content

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.docter.icare.R
import com.docter.icare.databinding.ActivityPersonInfoContentBinding
import com.docter.icare.ui.base.BaseActivity
import org.kodein.di.android.closestDI

class PersonInfoContentActivity : BaseActivity() {

    override val di by closestDI()
    private val binding: ActivityPersonInfoContentBinding by lazy { DataBindingUtil.setContentView(this, R.layout.activity_person_info_content) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarColor(R.color.welcome_status_bar)

        with(binding){
            lifecycleOwner = this@PersonInfoContentActivity
        }
    }
}