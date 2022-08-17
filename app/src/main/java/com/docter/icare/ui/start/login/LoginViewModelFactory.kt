package com.docter.icare.ui.start.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.docter.icare.data.repositories.LoginRepository

@Suppress("UNCHECKED_CAST")
class LoginViewModelFactory  (
    private val loginRepository: LoginRepository
): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = LoginViewModel(loginRepository) as T
}