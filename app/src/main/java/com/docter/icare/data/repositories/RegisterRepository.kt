package com.docter.icare.data.repositories

import com.docter.icare.data.network.SafeApiRequest
import com.docter.icare.data.network.api.ApiService
import com.docter.icare.data.preferences.PreferenceProvider
import com.docter.icare.data.resource.ResourceProvider

class RegisterRepository (
    private val resource: ResourceProvider,
    private val preference: PreferenceProvider,
    private val api: ApiService
) : SafeApiRequest(resource) {
}