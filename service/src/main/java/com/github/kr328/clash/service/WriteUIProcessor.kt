package com.github.kr328.clash.service

import android.content.Context
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.model.CommonProfile
import com.github.kr328.clash.service.data.Imported
import com.github.kr328.clash.service.data.ImportedDao
import com.github.kr328.clash.service.data.PendingDao
import com.github.kr328.clash.service.model.Profile
import com.github.kr328.clash.service.util.importedDir
import com.github.kr328.clash.service.util.pendingDir
import com.github.kr328.clash.service.util.processingDir
import com.github.kr328.clash.service.util.sendProfileChanged
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.math.BigDecimal
import java.util.*

data class UserInfo(val uplaod: Long, val download: Long, val total: Long, val expire: Long)

object WriteUIProcessor {
    private val profileLock = Mutex()

    //    private lateinit  var snapshot: CommonProfile
    suspend fun fetch(context: Context, uuid: UUID) {
        withContext(NonCancellable) {
            profileLock.withLock {
                val pending = PendingDao().queryByUUID(uuid)
                val imported = ImportedDao().queryByUUID(uuid)
                if (pending != null) {
                    context.processingDir.deleteRecursively()
                    context.processingDir.mkdirs()
                    context.pendingDir.resolve(pending.uuid.toString())
                        .copyRecursively(context.processingDir, overwrite = true)
                    return@withLock
                }
                if (imported != null) {
                    context.processingDir.deleteRecursively()
                    context.processingDir.mkdirs()

                    context.importedDir.resolve(imported.uuid.toString())
                        .copyRecursively(context.processingDir, overwrite = true)
                    return@withLock
                }
                throw IllegalArgumentException("profile $uuid not found")
            }
        }
    }

    suspend fun update(context: Context, profile: CommonProfile) {
        withContext(NonCancellable) {
            profileLock.withLock {
                var userInfo = UserInfo(0, 0, 0, 0)
                context.importedDir.resolve(profile.uuid)
                    .deleteRecursively()
                context.processingDir
                    .copyRecursively(context.importedDir.resolve(profile.uuid))

                val old = ImportedDao().queryByUUID(UUID.fromString(profile.uuid))

                if (profile.ptype == Profile.Type.Url.ordinal) {
                    userInfo = fetchUserInfo(profile.source)
                }
                val new = Imported(
                    UUID.fromString(profile.uuid),
                    profile.name,
                    Profile.Type.values()[profile.ptype],
                    profile.source,
                    profile.interval,
                    userInfo.uplaod, userInfo.download, userInfo.total, userInfo.expire,
                    old?.createdAt ?: System.currentTimeMillis()
                )

                if (old != null) {
                    ImportedDao().update(new)
                } else {
                    ImportedDao().insert(new)
                }

                PendingDao().remove(UUID.fromString(profile.uuid))
                context.pendingDir.resolve(profile.uuid).deleteRecursively()
                context.sendProfileChanged(UUID.fromString(profile.uuid))

            }
        }

    }

    private suspend fun fetchUserInfo(source: String): UserInfo {

        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()

            try {
                val request = Request.Builder()
                    .url(source)
                    .header("User-Agent", "ClashforWindows/0.19.23")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful || response.headers["subscription-userinfo"] == null)
                        UserInfo(0, 0, 0, 0)

                    var upload: Long = 0
                    var download: Long = 0
                    var total: Long = 0
                    var expire: Long = 0

                    val userinfo = response.headers["subscription-userinfo"]
                    if (response.isSuccessful && userinfo != null) {

                        val flags = userinfo.split(";")
                        for (flag in flags) {
                            val info = flag.split("=")
                            when {
                                info[0].contains("upload") && info[1].isNotEmpty() -> upload =
                                    BigDecimal(info[1]).longValueExact()

                                info[0].contains("download") && info[1].isNotEmpty() -> download =
                                    BigDecimal(info[1]).longValueExact()

                                info[0].contains("total") && info[1].isNotEmpty() -> total =
                                    BigDecimal(info[1]).longValueExact()

                                info[0].contains("expire") && info[1].isNotEmpty() -> {
                                    if (info[1].isNotEmpty()) {
                                        expire = (info[1].toDouble() * 1000).toLong()
                                    }
                                }
                            }
                        }
                    }
                    UserInfo(upload, download, total, expire)
                }

            } catch (e: Exception) {
                Log.e("fetch user information with error $e")
                UserInfo(0, 0, 0, 0)
            }
        }
    }

    suspend fun release(context: Context, uuid: String) {
        profileLock.withLock {
            PendingDao().remove(UUID.fromString(uuid))
            context.pendingDir.resolve(uuid).deleteRecursively()
        }
    }
}