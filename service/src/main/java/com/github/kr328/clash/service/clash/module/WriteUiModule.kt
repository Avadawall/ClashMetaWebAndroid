package com.github.kr328.clash.service.clash.module

import android.app.Service
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.service.store.ServiceStore
import com.github.kr328.clash.service.util.processingDir
import kotlinx.coroutines.selects.select

class WriteUiModule(service: Service) : Module<WriteUiModule.LoadException>(service) {
    data class LoadException(val message: String)
    override suspend fun run() {

//        val broadcasts = receiveBroadcast {
//            addAction(Intents.ACTION_WUI_SERVICE_STARTED)
//        }
//
//        while (true) {
//            try {
//                Log.w("WriteModule before select ===>")
//                val newId = select {
//                    broadcasts.onReceive {
//                        Log.w("WriteModule action ${it.action}")
//                        if (it.action == Intents.ACTION_WUI_SERVICE_STARTED) {
//                            it.getStringExtra(Intents.EXTRA_UUID)
//                        } else {
//                            null
//                        }
//                    }
//                }
//                Log.w("WriteModule newId $newId")
//
//                val port = ServiceStore(service).externalPort
//                val secret = ServiceStore(service).externalSecret
//                Clash.startWriteUi(service.processingDir, port, secret)
//                Log.w("write Ui listener started with $port - $secret")
//            } catch (e: Exception) {
//                return enqueueEvent(LoadException(e.message ?: "unknown"))
//            }
//        }
    }
}