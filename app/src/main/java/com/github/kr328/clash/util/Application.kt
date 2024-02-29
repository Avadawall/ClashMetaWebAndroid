package com.github.kr328.clash.util

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import java.io.File
import java.util.zip.ZipFile

object ApplicationObserver {
    private val activities: MutableSet<Activity> = mutableSetOf()

    private var visibleChanged: (Boolean) -> Unit = {}

    private var appVisible = false
        private set(value) {
            if (field != value) {
                field = value

                visibleChanged(value)
            }
        }

    val createdActivities: Set<Activity>
        get() = activities

    private val activityObserver = object : Application.ActivityLifecycleCallbacks {
        @Synchronized
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            activities.add(activity)

            appVisible = true
        }

        @Synchronized
        override fun onActivityDestroyed(activity: Activity) {
            activities.remove(activity)

            appVisible = activities.isNotEmpty()
        }

        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityResumed(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    }

    fun onVisibleChanged(visibleChanged: (Boolean) -> Unit) {
        this.visibleChanged = visibleChanged
    }

    fun attach(application: Application) {
        application.registerActivityLifecycleCallbacks(activityObserver)
    }
}

fun Context.verifyApk(): Boolean {
    return try {
        val info = applicationInfo
        val sources = info.splitSourceDirs ?: arrayOf(info.sourceDir) ?: return false

        val regexNativeLibrary = Regex("lib/(\\S+)/libclash.so")
        val availableAbi = Build.SUPPORTED_ABIS.toSet()
        val apkAbi = sources
            .asSequence()
            .filter { File(it).exists() }
            .flatMap { ZipFile(it).entries().asSequence() }
            .mapNotNull { regexNativeLibrary.matchEntire(it.name) }
            .mapNotNull { it.groups[1]?.value }
            .toSet()

        availableAbi.intersect(apkAbi).isNotEmpty()
    } catch (e: Exception) {
        false
    }
}