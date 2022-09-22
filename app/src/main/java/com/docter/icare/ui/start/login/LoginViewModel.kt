package com.docter.icare.ui.start.login

import androidx.lifecycle.ViewModel
import com.docter.icare.data.entities.view.LoginEntity
import com.docter.icare.data.network.api.response.LoginResponse2
import com.docter.icare.data.repositories.LoginRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginViewModel (
    private val loginRepository: LoginRepository
): ViewModel() {
    val entity = LoginEntity()

    fun checkInput() = loginRepository.checkLoginInput(entity)

    suspend fun login() = withContext(Dispatchers.IO) { loginRepository.login(entity) }

    fun save(data: LoginResponse2) = loginRepository.save(entity,data)
}