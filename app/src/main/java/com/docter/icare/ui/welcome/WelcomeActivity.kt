package com.docter.icare.ui.welcome

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.docter.icare.ui.main.MainActivity
import com.docter.icare.R
import com.docter.icare.databinding.ActivityWelcomeBinding
import com.docter.icare.ui.base.BaseActivity
import com.docter.icare.ui.start.StartActivity
import com.docter.icare.utils.Coroutines.main
import com.docter.icare.utils.clearStack
import com.docter.icare.view.dialog.CustomAlertDialog
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import io.reactivex.rxjava3.core.Observable.timer
import java.util.concurrent.TimeUnit

class WelcomeActivity : BaseActivity() {

    override val di by closestDI()
    private val binding: ActivityWelcomeBinding by lazy { DataBindingUtil.setContentView(this, R.layout.activity_welcome) }

    private val factory: WelcomeViewModelFactory by instance()
    private val viewModel: WelcomeViewModel by lazy { ViewModelProvider(this, factory)[WelcomeViewModel::class.java] }

    private val askPermissionAgainDialog: CustomAlertDialog by lazy {

        CustomAlertDialog(this)
            .setTitle(R.string.ask_permission_again_title)
            .setMessage(R.string.ask_permission_again_message)
            .setPositiveButton(R.string.ok) { requestPermissions( viewModel.permissionArray, 0) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarColor(R.color.welcome_status_bar)

        with(binding) {
            lifecycleOwner = this@WelcomeActivity
        }

    }

    override fun onStart() {
        super.onStart()

        when {

            //If permission is not granted, request permission.
            viewModel.isNeedAskPermission() -> requestPermissions(
                viewModel.permissionArray,
                0
            )

            else -> chooseNext()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when {
            viewModel.isNeedAskPermission(grantResults) -> if (!askPermissionAgainDialog.isShowing()) askPermissionAgainDialog.show()
            else -> chooseNext()
        }
    }

    private fun chooseNext(){
        when {

            viewModel.isLoggedIn -> {
                main {
                    val intent = Intent()
                    intent.setClass(this, MainActivity::class.java).clearStack()
                    timer(3000L, TimeUnit.MILLISECONDS).subscribe {
                        startActivity(intent)
                    }
                }
            }
            else -> {
                main { startActivity(Intent(this, StartActivity::class.java)) }
            }
        }
    }

}