package com.github.kr328.clash.service

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.core.bridge.UiCallback
import com.github.kr328.clash.core.model.CommonProfile
import com.github.kr328.clash.service.clash.module.DestroyModule
import com.github.kr328.clash.service.clash.module.UiNotificationModule
import com.github.kr328.clash.service.clash.uiRuntime
import com.github.kr328.clash.service.store.ServiceStore
import com.github.kr328.clash.service.util.cancelAndJoinBlocking
import com.github.kr328.clash.service.util.processingDir
import com.github.kr328.clash.service.util.sendWifiBundle
import com.github.kr328.clash.service.util.sendWriteUiStopped
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.*

class WriteUiService : BaseService() {

    private val self: WriteUiService
        get() = this
    private lateinit var profile : CommonProfile
    private var isReported = false
    private val runtime = uiRuntime {
        val destroy = install(DestroyModule(self))
        install(UiNotificationModule(self))
        try {
            while(isActive) {
                val quit = select {
                    destroy.onEvent {
                        true
                    }
                }
                if (quit) break
            }
        } catch (e: Exception) {
            Log.e("create WriteUi runtime: ${e.message}", e)
        } finally {
            withContext(NonCancellable) {
                stopSelf()
            }
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return Binder()
    }

    override fun onCreate() {
        super.onCreate()
        UiNotificationModule.createNotificationChannel(this)
        UiNotificationModule.notifyLoadingNotification(this)
        sendWifiBundle(self)
        runtime.launch()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val uuid = intent?.getStringExtra(Intents.EXTRA_UUID) ?: throw IllegalArgumentException("empty uuid")
        val name = intent.getStringExtra(Intents.EXTRA_NAME) ?: ""
        val pType = intent.getIntExtra(Intents.EXTRA_PTYPE, 0)
        val source = intent.getStringExtra(Intents.EXTRA_SOURCE) ?: ""
        val interval = intent.getLongExtra(Intents.EXTRA_INTERVAL, 0)
        profile = CommonProfile(
            uuid, name, pType, source, interval, true, ""
        )

        try {
            launch {
                WriteUIProcessor.fetch(self, UUID.fromString(uuid))
                val port = ServiceStore(self).externalPort
                val secret = ServiceStore(self).externalSecret
                Clash.startWriteUi(self.processingDir, port, secret, uuid, name, pType, source, interval, object : UiCallback {
                    override fun report(profileJson: String) {
                        profile = Json.Default.decodeFromString(
                            CommonProfile.serializer(),
                            profileJson
                        )
                        isReported = true
                    }
                } )
            }
        } catch (e: Exception) {
            Log.e(e.message?: "unknown error")
        }
        return START_STICKY
    }

    override fun onDestroy() {

        Clash.stopWriteListener()

        runBlocking {
            if (isReported) {
                if (!profile.hasErr) {
                    WriteUIProcessor.update(self, profile)
                    Log.i("WriteUiService insert imported and destroyed successfully")
                } else {
                    Log.i("WriteUiService destroyed with error: ${profile.err}")
                }
                sendWriteUiStopped()
            } else {
                WriteUIProcessor.release(self, profile.uuid)
            }
        }
        cancelAndJoinBlocking()
        super.onDestroy()
    }
}