package com.docter.icare.ui.start.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.docter.icare.data.repositories.RegisterRepository


@Suppress("UNCHECKED_CAST")
class RegisterViewModelFactory (
    private val registerRepository: RegisterRepository
): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = RegisterViewModel(registerRepository) as T
}