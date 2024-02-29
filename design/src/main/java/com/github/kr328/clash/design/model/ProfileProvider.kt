package com.github.kr328.clash.design.model

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import com.github.kr328.clash.common.compat.getDrawableCompat
import com.github.kr328.clash.design.R

sealed class ProfileProvider {
    class File(private val context: Context) : ProfileProvider() {
        override val name: String
            get() = context.getString(R.string.file)
        override val summary: String
            get() = context.getString(R.string.import_from_file)
        override val icon: Drawable?
            get() = context.getDrawableCompat(R.drawable.ic_baseline_attach_file)
    }

    class Url(private val context: Context) : ProfileProvider() {
        override val name: String
            get() = context.getString(R.string.url)
        override val summary: String
            get() = context.getString(R.string.import_from_url)
        override val icon: Drawable?
            get() = context.getDrawableCompat(R.drawable.ic_baseline_cloud_download)
    }

    class WebUI(private val context: Context): ProfileProvider() {
        override val name: String
            get() = context.getString(R.string.web_ui)
        override val summary: String
            get() = context.getString(R.string.web_ui_modify)
        override val icon: Drawable?
            get() = context.getDrawable(R.drawable.ic_baseline_cloud_upload)

    }

    class External(
        override val name: String,
        override val summary: String,
        override val icon: Drawable?,
        val intent: Intent,
    ) : ProfileProvider()

    abstract val name: String
    abstract val summary: String
    abstract val icon: Drawable?
}
