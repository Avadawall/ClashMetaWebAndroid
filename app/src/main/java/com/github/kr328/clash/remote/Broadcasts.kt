package com.github.kr328.clash.remote

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.log.Log

class Broadcasts(private val context: Application) {
    interface Observer {
        fun onServiceRecreated()
        fun onStarted()
        fun onStopped(cause: String?)
        fun onProfileChanged()
        fun onProfileLoaded()
//        fun onWifiChanged(state: Bundle?)
        fun onWifiChange(wifiState: String, ipv4UrlText: String)
    }

    var clashRunning: Boolean = false
    var uiSrvRunning: Boolean = false

    var wifiState: String = "off"
    var ipv4UrlText: String = ""

    private var registered = false
    private val receivers = mutableListOf<Observer>()
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.`package` != context?.packageName)
                return

            when (intent?.action) {
                Intents.ACTION_SERVICE_RECREATED -> {
                    clashRunning = false

                    receivers.forEach {
                        it.onServiceRecreated()
                    }
                }
                Intents.ACTION_CLASH_STARTED -> {
                    clashRunning = true

                    receivers.forEach {
                        it.onStarted()
                    }
                }
                Intents.ACTION_CLASH_STOPPED -> {
                    clashRunning = false

                    receivers.forEach {
                        it.onStopped(intent.getStringExtra(Intents.EXTRA_STOP_REASON))
                    }
                }
                Intents.ACTION_PROFILE_CHANGED ->
                    receivers.forEach {
                        it.onProfileChanged()
                    }
                Intents.ACTION_PROFILE_LOADED -> {
                    receivers.forEach {
                        it.onProfileLoaded()
                    }
                }

                Intents.ACTION_WIFI_CHANGED -> {
                    val obj = intent.getBundleExtra(Intents.EXTRA_WIFI_BUNDLE)
                    obj?.let {
                        wifiState = obj["wifiState"] as String
                        ipv4UrlText = obj["ipv4UrlText"] as String
                    }
                    receivers.forEach {

                        it.onWifiChange(wifiState, ipv4UrlText)
//                        it.onWifiChanged(intent.getBundleExtra(Intents.EXTRA_WIFI_BUNDLE))
                    }
                }

                Intents.ACTION_WUI_SERVICE_STARTED -> {
                    uiSrvRunning = true
                }

                Intents.ACTION_WUI_SERVICE_STOPPED -> {
                    uiSrvRunning = false
                }
            }
        }
    }

    fun addObserver(observer: Observer) {
        receivers.add(observer)
    }

    fun removeObserver(observer: Observer) {
        receivers.remove(observer)
    }

    fun register() {
        if (registered)
            return

        try {
            context.registerReceiver(broadcastReceiver, IntentFilter().apply {
                addAction(Intents.ACTION_SERVICE_RECREATED)
                addAction(Intents.ACTION_CLASH_STARTED)
                addAction(Intents.ACTION_CLASH_STOPPED)
                addAction(Intents.ACTION_PROFILE_CHANGED)
                addAction(Intents.ACTION_PROFILE_LOADED)
                addAction(Intents.ACTION_WIFI_CHANGED)
                addAction(Intents.ACTION_WUI_SERVICE_STARTED)
                addAction(Intents.ACTION_WUI_SERVICE_STOPPED)
            })

            clashRunning = StatusClient(context).currentProfile() != null
        } catch (e: Exception) {
            Log.w("Register global receiver: $e", e)
        }
    }

    fun unregister() {
        if (!registered)
            return

        try {
            context.unregisterReceiver(broadcastReceiver)

            clashRunning = false
        } catch (e: Exception) {
            Log.w("Unregister global receiver: $e", e)
        }
    }
}