package com.github.kr328.clash.service.controller

//import com.github.kr328.clash.common.log.Log
//import com.github.kr328.clash.service.clash.module.Module
//import kotlinx.coroutines.*
//import kotlinx.coroutines.sync.Mutex
//
//
//private val globalLock = Mutex()
//
//interface ControllerRuntimeScope {
//    fun <E, T: Module<E>> install(module: T): T
//}
//
//interface ControllerRuntime {
//    fun launch()
//
//    fun requestGC()
//}
//
//fun CoroutineScope.controllerRuntime(block: suspend ControllerRuntimeScope.() -> Unit): ControllerRuntime {
//    return object: ControllerRuntime {
//        override fun launch() {
//            launch(Dispatchers.IO) {
//                Log.d("ControllerRuntime: initialize")
//
//
//                try {
//                    val modules = mutableListOf<Module<*>>()
//
//                    val scope = object: ControllerRuntimeScope {
//                        override fun <E, T : Module<E>> install(module: T): T {
//                            launch {
//                                modules.add(module)
//
//                                module.execute()
//                            }
//                            return module
//                            TODO("Not yet implemented")
//                        }
//                    }
//                    scope.block()
//                    cancel()
//                } finally {
//                    withContext(NonCancellable) {
//                        // reset
//                        // clearOverride
//                        Log.d("ControllerRuntime: destroyed")
//                    }
//                }
//            }
//
//        }
//
//        override fun requestGC() {
//            // forceGc()
//            TODO("Not yet implemented")
//        }
//
//    }
//}