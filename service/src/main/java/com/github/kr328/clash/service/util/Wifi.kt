package com.github.kr328.clash.service.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.service.store.ServiceStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.Inet6Address

fun sendWifiBundle(context: Context) {

    CoroutineScope(Dispatchers.IO).launch {
        val port = ServiceStore(context).externalPort
//        val secret = ServiceStore(context).externalSecret
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val bundle = Bundle()
        try {
            connMgr.registerNetworkCallback(NetworkRequest.Builder().apply {
                addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            }.build(), object: ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {

                    if (connMgr.getNetworkCapabilities(network)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                        connMgr.getLinkProperties(network)?.let {
                            it.linkAddresses.forEach { linkAddr ->
                                when (linkAddr.address) {
                                    is Inet4Address -> {
                                        val text = linkAddr.address.hostAddress
                                        if (text != null) {
                                            val ipv4UrlText = "$text:$port/ui"
                                            bundle.putString("wifiState", "on")
                                            bundle.putString("ipv4UrlText", ipv4UrlText)
                                        }
                                    }

//                                    is Inet6Address -> {
//                                        val text = linkAddr.address.hostAddress
//                                        if (text != null) {
//                                            val ipv6Text = concatenateURL("[$text]:$port/ui/", mapOf("host" to text, "secret" to secret))
//                                            bundle.putString("wifiState", "on")
//                                            bundle.putString("ipv6Text", ipv6Text)
//                                        }
//                                    }

                                }
                            }
                        }
                    }

                    context.sendWifiChanged(bundle)
                }

                override fun onLost(network: Network) {
                    bundle.putString("wifiState", "off")
                    bundle.putString("ipv4UrlText", "")
                    bundle.putString("secretText", "")
                    context.sendWifiChanged(bundle)
                }
            })
        } catch (e: Exception) {
            Log.w("wifiBundler network failed: $e", e)
        }
    }

}

fun concatenateURL(baseURL: String, params: Map<String, String>): String {
    val sb = StringBuilder(baseURL)
    if (params.isNotEmpty()) {
        sb.append("?")
        params.forEach {
            sb.append("${it.key}=${it.value}&")
        }
        sb.deleteCharAt(sb.length - 1)
    }
    return sb.toString()
}