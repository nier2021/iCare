package com.docter.icare.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.docter.icare.data.repositories.MainRepository
import com.docter.icare.data.repositories.RadarRepository

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory(
    private val mainRepository: MainRepository,
    private val radarRepository: RadarRepository,
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel(mainRepository,radarRepository) as T
}