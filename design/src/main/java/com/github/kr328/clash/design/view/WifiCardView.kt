package com.github.kr328.clash.design.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import com.github.kr328.clash.design.R
import com.github.kr328.clash.design.databinding.ComponentWifiCardBinding
import com.google.android.material.card.MaterialCardView

class WifiCardView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    ): MaterialCardView(context, attributeSet, defStyleAttr) {
        private val binding = ComponentWifiCardBinding.inflate(LayoutInflater.from(context), this, true)

        var connectText: CharSequence?
            get() = binding.connectText.text
            set(value) {
                val text = if (value == "on") {
                    resources.getString(R.string.wifi_connected)
                } else {
                    resources.getString(R.string.wifi_lost)
                }

                 if (value == "on") {
                     binding.connectText.setTextColor(ContextCompat.getColor(context, R.color.color_clash_dark))
                     binding.ipv4Container.visibility = VISIBLE
                     binding.secretContainer.visibility = VISIBLE
                } else {
                     binding.connectText.setTextColor(ContextCompat.getColor(context, R.color.color_error))
                     binding.ipv4Container.visibility = GONE
                     binding.secretContainer.visibility = GONE
                }
                binding.connectText.text = text
            }

        var ipv4UrlText: CharSequence?
            get() = binding.ipv4UrlText.text
            set(value) {
               binding.ipv4UrlText.text = value
            }

        var secretText: CharSequence?
            get() = binding.secretText.text
            set(value) {
                binding.secretText.text = value
            }
//        var ipv6Text: CharSequence?
//            get() = binding.ipv6Text.text
//            set(value) {
//                binding.ipv6Text.text = value
//                if (value == "") {
//                    binding.ipv6Text.visibility = View.GONE
//                } else {
//                    binding.ipv6Text.visibility = View.VISIBLE
//                }
//            }

        init {
            context.theme.obtainStyledAttributes(
                attributeSet,
                R.styleable.WifiCardView,
                defStyleAttr,
                0
            ).apply {
                try {
                    connectText = getString(R.styleable.WifiCardView_connectText)
                    ipv4UrlText = getString(R.styleable.WifiCardView_ipv4UrlText)
                    secretText = getString(R.styleable.WifiCardView_secretText)
                } finally {
                    recycle()
                }
            }
            radius = resources.getDimensionPixelSize(R.dimen.large_action_card_radius).toFloat()
            elevation = resources.getDimensionPixelSize(R.dimen.large_action_card_elevation).toFloat()

        }
    }