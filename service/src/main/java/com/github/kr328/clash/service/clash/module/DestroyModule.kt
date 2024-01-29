package com.github.kr328.clash.service.clash.module

import android.app.Service
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.log.Log


class DestroyModule(service: Service) : Module<DestroyModule.RequestClose>(service) {

    object RequestClose

    override suspend fun run() {
        val broadcast = receiveBroadcast {
            addAction(Intents.ACTION_WUI_SERVICE_STOPPED)
        }
        broadcast.receive()
        Log.d("User request close")

        return enqueueEvent(RequestClose)
    }
}