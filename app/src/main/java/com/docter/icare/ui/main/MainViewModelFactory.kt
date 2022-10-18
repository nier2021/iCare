package com.docter.icare.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.docter.icare.data.network.api.webSocket.WebServices
import com.docter.icare.data.repositories.DeviceRepository
import com.docter.icare.data.repositories.MainRepository
import com.docter.icare.data.repositories.RadarRepository

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory(
    private val mainRepository: MainRepository,
    private val radarRepository: RadarRepository,
    private val webServices: WebServices,
    private val deviceRepository: DeviceRepository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel(mainRepository,radarRepository,webServices,deviceRepository) as T
}