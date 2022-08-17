package com.docter.icare.ui.main.uam

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.docter.icare.R
import com.docter.icare.data.entities.view.BedsideMonitorEntity
import com.docter.icare.data.entities.webSocket.SocketUpdate
import com.docter.icare.data.repositories.RadarRepository
import com.docter.icare.utils.toRadarShowDate
import kotlinx.coroutines.channels.Channel

class ActivityMonitoringViewModel (
    private val radarRepository: RadarRepository,
) : ViewModel(){
    val entity = BedsideMonitorEntity()
    fun getDeviceAccountId() = radarRepository.getDeviceAccountId()

    fun startSocket(context: Context ,accountId: String): Channel<SocketUpdate> =
        radarRepository.startSocket(context,accountId)

    fun stopSocket() = radarRepository.closeSocket()
}