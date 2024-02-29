package com.github.kr328.clash

import androidx.lifecycle.ViewModelProvider
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.common.util.uuid
import com.github.kr328.clash.design.WebPropertiesDesign
//import com.github.kr328.clash.design.model.ListenerViewModel
//import com.github.kr328.clash.design.model.ModelFactory
//import com.github.kr328.clash.remote.WifiObserver
import com.github.kr328.clash.service.model.Profile
import com.github.kr328.clash.service.store.ServiceStore
import com.github.kr328.clash.util.startWriteUiService
import com.github.kr328.clash.util.stopClashService
import com.github.kr328.clash.util.stopWriteUiService
import com.github.kr328.clash.util.withProfile
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.nio.channels.WritePendingException
import java.util.*


class WebPropertiesActivity : BaseActivity<WebPropertiesDesign>() {
    override suspend fun main() {
        setResult(RESULT_CANCELED)
        val uuid = intent.uuid ?: return finish()
        val design = WebPropertiesDesign(this)

        val original = withProfile { queryByUUID(uuid) } ?: return finish()

        design.profile = original
        setContentDesign(design)
        defer {
            if (uiSrvRunning) {
                stopWriteUiService()
            }
        }
        if (clashRunning) {
            stopClashService()
        }

        if (!uiSrvRunning) {
            startWriteUiService(original.uuid.toString(), original.name, original.type.ordinal,
                original.source, original.interval)
        }

        design.fetch()

        while (isActive) {
            select {
                events.onReceive {
                    when (it) {
                        Event.WifiChanged -> design.fetch()
                        else -> Unit
                    }
                }
            }
        }
    }
//    private suspend fun WebPropertiesDesign.fetch(uuid: UUID) {
    private suspend fun WebPropertiesDesign.fetch() {

        setWifiState(wifiState)
        setIpv4UrlText(ipv4UrlText)
        setSecretText(srvStore.externalSecret)

    }
    override fun onBackPressed() {
        design?.apply {
            launch {
                stopWriteUiService()
                finish()
//                if (requestExitWithoutSaving()) {
//
//                }
            }
        } ?: super.onBackPressed()
    }


}