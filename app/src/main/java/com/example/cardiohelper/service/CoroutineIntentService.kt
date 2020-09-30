package com.example.cardiohelper.service

import android.app.IntentService
import android.app.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class CoroutineIntentService : IntentService("IntentService"), CoroutineScope {

    private var coroutineJob = Job()

    override val coroutineContext: CoroutineContext
        get() = coroutineJob + Dispatchers.IO


    override fun onDestroy() {
        coroutineJob.cancel()
    }
}