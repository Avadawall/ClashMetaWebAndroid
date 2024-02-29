package com.github.kr328.clash.service.clash

import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.service.clash.module.Module
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val globalLock = Mutex()

interface UiRuntimeScope {
    fun <E, T: Module<E>> install(module: T): T
}

interface UiRuntime {
    fun launch()

    fun requestGc()
}

fun CoroutineScope.uiRuntime(block: suspend UiRuntimeScope.() -> Unit) : UiRuntime {
    return object: UiRuntime {
        override fun launch() {
            launch(Dispatchers.IO) {
                globalLock.withLock {
                    Log.d("UiRuntime: initialized")
                    try {
                        val modules = mutableListOf<Module<*>>()

                        val scope = object : UiRuntimeScope {
                            override fun <E, T : Module<E>> install(module: T): T {
                                launch {

                                    modules.add(module)
                                    module.execute()
                                }
                                return module
                            }
                        }

                        scope.block()
                        cancel()

                    } finally {
                        withContext(NonCancellable) {
                            Log.d("UiRuntime: destroyed")
                        }
                    }
                }
            }
        }

        override fun requestGc() {
        }
    }
}