package com.docter.icare.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers.IO

object Coroutines {
    fun main(work: suspend (() -> Unit)) = CoroutineScope(Main).launch { work() }

    fun io(work: suspend (() -> Unit)) = CoroutineScope(IO).launch { work() }
}