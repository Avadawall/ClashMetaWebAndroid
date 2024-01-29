package com.github.kr328.clash.service.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.constants.Permissions
import java.util.*

fun Context.sendBroadcastSelf(intent: Intent) {
    sendBroadcast(
        intent.setPackage(this.packageName),
        Permissions.RECEIVE_SELF_BROADCASTS
    )
}

fun Context.sendProfileChanged(uuid: UUID) {
    val intent = Intent(Intents.ACTION_PROFILE_CHANGED)
        .putExtra(Intents.EXTRA_UUID, uuid.toString())

    sendBroadcastSelf(intent)
}

fun Context.sendProfileLoaded(uuid: UUID) {
    val intent = Intent(Intents.ACTION_PROFILE_LOADED)
        .putExtra(Intents.EXTRA_UUID, uuid.toString())

    sendBroadcastSelf(intent)
}

fun Context.sendOverrideChanged() {
    val intent = Intent(Intents.ACTION_OVERRIDE_CHANGED)

    sendBroadcastSelf(intent)
}

fun Context.sendServiceRecreated() {
    sendBroadcastSelf(Intent(Intents.ACTION_SERVICE_RECREATED))
}

fun Context.sendClashStarted() {
    sendBroadcastSelf(Intent(Intents.ACTION_CLASH_STARTED))
}

fun Context.sendClashStopped(reason: String?) {
    sendBroadcastSelf(
        Intent(Intents.ACTION_CLASH_STOPPED).putExtra(
            Intents.EXTRA_STOP_REASON,
            reason
        )
    )
}

fun Context.sendWifiChanged(bundle: Bundle) {
    sendBroadcastSelf(
        Intent(Intents.ACTION_WIFI_CHANGED).putExtra(
            Intents.EXTRA_WIFI_BUNDLE,
            bundle
        )
    )
}

fun Context.sendWriteUiStarted(uuid: String) {
    sendBroadcastSelf(Intent(Intents.ACTION_WUI_SERVICE_STARTED).putExtra("uuid", uuid))
}

fun Context.sendWriteUiStopped() {
    sendBroadcastSelf(Intent(Intents.ACTION_WUI_SERVICE_STOPPED))
}