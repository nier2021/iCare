package com.docter.icare.data.entities.webSocket

import com.docter.icare.data.entities.view.BioRadarEntity

data class SocketUpdate(
    val exception: Throwable? = null,
    val error:  String? = null,
    val bioRadar: BioRadarEntity? = null
)
