package com.github.kr328.clash.util

import android.content.Context
import android.content.Intent
import android.net.VpnService
import com.github.kr328.clash.common.compat.startForegroundServiceCompat
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.design.store.UiStore
import com.github.kr328.clash.service.ClashService
import com.github.kr328.clash.service.TunService
import com.github.kr328.clash.service.WriteUiService
import com.github.kr328.clash.service.util.sendBroadcastSelf

fun Context.startClashService(): Intent? {
    val startTun = UiStore(this).enableVpn

    if (startTun) {
        val vpnRequest = VpnService.prepare(this)
        if (vpnRequest != null)
            return vpnRequest

        startForegroundServiceCompat(TunService::class.intent)
    } else {
        startForegroundServiceCompat(ClashService::class.intent)
    }

    return null
}

fun Context.stopClashService() {
    sendBroadcastSelf(Intent(Intents.ACTION_CLASH_REQUEST_STOP))
}

fun Context.startWriteUiService(uuid: String, name : String, pType: Int, source : String,
                                interval: Long): Intent? {
    startForegroundServiceCompat(WriteUiService::class.intent.putExtra(Intents.EXTRA_UUID, uuid).
                        putExtra(Intents.EXTRA_NAME, name).putExtra(Intents.EXTRA_PTYPE, pType)
            .putExtra(Intents.EXTRA_SOURCE, source).putExtra(Intents.EXTRA_INTERVAL, interval))
    return null
}

fun Context.stopWriteUiService() {
    sendBroadcastSelf(Intent(Intents.ACTION_WUI_SERVICE_STOPPED))
}