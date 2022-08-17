package com.docter.icare.ui.welcome

import androidx.lifecycle.ViewModelProvider
import com.docter.icare.data.repositories.WelcomeRepository
import androidx.lifecycle.ViewModel

@Suppress("UNCHECKED_CAST")
class WelcomeViewModelFactory (
    private val welcomeRepository: WelcomeRepository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = WelcomeViewModel(welcomeRepository) as T
}