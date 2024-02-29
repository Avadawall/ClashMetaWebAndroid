package com.github.kr328.clash.service.clash.module

import android.app.Service
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.kr328.clash.common.compat.getColorCompat
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.service.R
import kotlinx.coroutines.channels.Channel

class UiNotificationModule(service: Service) : Module<Unit>(service) {
    private val builder = NotificationCompat.Builder(service, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_logo_service)
        .setOngoing(true)
        .setColor(service.getColorCompat(R.color.color_clash))
        .setOnlyAlertOnce(true)
        .setShowWhen(false)
        .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)


    override suspend fun run() {
        val keeper = receiveBroadcast(capacity = Channel.CONFLATED) {
            addAction(Intents.ACTION_WUI_SERVICE_STARTED)
        }
        while (true) {
            keeper.receive()
            val notification = builder
                .setContentTitle("clash ui")
                .setContentText(service.getText(R.string.running))
                .build()
            service.startForeground(R.id.nf_clash_status, notification)
        }
    }

    companion object {
        const val CHANNEL_ID = "clash_status_channel"

        fun createNotificationChannel(service: Service) {
            NotificationManagerCompat.from(service).createNotificationChannel(
                NotificationChannelCompat.Builder(
                    CHANNEL_ID,
                    NotificationManagerCompat.IMPORTANCE_LOW
                ).setName(service.getText(R.string.clash_service_status_channel)).build()
            )
        }

        fun notifyLoadingNotification(service: Service) {
            val notification =
                NotificationCompat.Builder(service, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_logo_service)
                    .setOngoing(true)
                    .setColor(service.getColorCompat(R.color.color_clash))
                    .setOnlyAlertOnce(true)
                    .setShowWhen(false)
                    .setContentTitle(service.getText(R.string.loading))
                    .build()

            service.startForeground(R.id.nf_clash_status, notification)
        }
    }

}