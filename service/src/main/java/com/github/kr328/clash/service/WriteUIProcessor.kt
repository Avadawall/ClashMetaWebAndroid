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
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.*

object WriteUIProcessor {
    private val profileLock = Mutex()
//    private lateinit  var snapshot: CommonProfile
    suspend fun fetch(context: Context, uuid: UUID)  {
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
                if (imported != null ) {
                    context.processingDir.deleteRecursively()
                    context.processingDir.mkdirs()

                    context.importedDir.resolve(imported.uuid.toString())
                        .copyRecursively(context.processingDir, overwrite = true)
                    return@withLock
                }
                throw  IllegalArgumentException("profile $uuid not found")
            }
        }
    }

    suspend fun update(context: Context, profile: CommonProfile) {
        profileLock.withLock {

            context.importedDir.resolve(profile.uuid)
                .deleteRecursively()
            context.processingDir
                .copyRecursively(context.importedDir.resolve(profile.uuid))

            val old = ImportedDao().queryByUUID(UUID.fromString(profile.uuid))
            Log.w("old profile= $old")
            val new = Imported(
                UUID.fromString(profile.uuid),
                profile.name,
                Profile.Type.values()[profile.ptype],
                profile.source,
                profile.interval,
                0,0,0,0,
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

    suspend fun release(context: Context, uuid: String) {
        profileLock.withLock {
            PendingDao().remove(UUID.fromString(uuid))
            context.pendingDir.resolve(uuid).deleteRecursively()
        }
    }
}