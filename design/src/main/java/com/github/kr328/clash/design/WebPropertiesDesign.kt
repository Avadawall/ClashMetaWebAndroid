package com.github.kr328.clash.design

import android.content.Context
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.github.kr328.clash.design.databinding.DesignWebPropertiesBinding
//import com.github.kr328.clash.design.model.ListenerViewModel
import com.github.kr328.clash.design.util.*
import com.github.kr328.clash.service.model.Profile
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class WebPropertiesDesign(context: Context) : Design<WebPropertiesDesign.Request>(context) {
    sealed class Request {
//        object Commit : Request()
//        object BrowseFiles : Request()
    }

    private val binding = DesignWebPropertiesBinding
        .inflate(context.layoutInflater, context.root, false)

    override val root: View
        get() = binding.root

    var profile: Profile
        get() = binding.profile!!
        set(value) {
            binding.profile = value
        }

    suspend fun setWifiState(state: String) {
        withContext(Dispatchers.Main) {
            binding.wifiState = state
        }
    }
    suspend fun setIpv4UrlText(ipv4UrlText: String) {
        withContext(Dispatchers.Main) {
            binding.ipv4UrlText = ipv4UrlText
        }
    }
    suspend fun setSecretText(secretText: String) {
        withContext(Dispatchers.Main) {
            binding.secretText = secretText
        }
    }

    init {
        binding.self = this

        binding.activityBarLayout.applyFrom(context)

        binding.tips.text = context.getHtml(R.string.tips_web_properties)

        binding.scrollRoot.bindAppBarElevation(binding.activityBarLayout)
    }


    suspend fun requestExitWithoutSaving(): Boolean {
        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { ctx ->
                val dialog = MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.exit_without_save)
                    .setMessage(R.string.exit_without_save_warning)
                    .setCancelable(true)
                    .setPositiveButton(R.string.ok) { _, _ -> ctx.resume(true) }
                    .setNegativeButton(R.string.cancel) { _, _ -> }
                    .setOnDismissListener { if (!ctx.isCompleted) ctx.resume(false) }
                    .show()

                ctx.invokeOnCancellation { dialog.dismiss() }
            }
        }
    }

}