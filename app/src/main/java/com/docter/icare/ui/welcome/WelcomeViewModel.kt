package com.docter.icare.ui.welcome

import androidx.lifecycle.ViewModel
import com.docter.icare.data.repositories.WelcomeRepository
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers.IO

class WelcomeViewModel(
    private val welcomeRepository: WelcomeRepository
) : ViewModel() {
    //Permission
    val permissionArray = welcomeRepository.permissionArray
    fun isNeedAskPermission() = welcomeRepository.isNeedAskPermission()
    fun isNeedAskPermission(
        grantResults: IntArray
    ) = welcomeRepository.isNeedAskPermission(grantResults)

    //Search data
    val isLoggedIn = welcomeRepository.isLoggedIn()


}