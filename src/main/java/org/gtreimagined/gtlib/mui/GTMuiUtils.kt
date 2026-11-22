@file:JvmName("GTMuiUtils")
package org.gtreimagined.gtlib.mui

import brachy.modularui.api.value.IValue
import brachy.modularui.value.sync.ModularSyncManager
import brachy.modularui.value.sync.SyncHandler
import java.util.Optional

fun <T: Any> getSyncedValue(id: String, tClass: Class<T>, syncManager: ModularSyncManager): Optional<T> {
    val handler: SyncHandler<*> = syncManager.findSyncHandler(id)
    val value: IValue<T>? = handler.castValueNullable<T>(tClass)
    return if (value == null) Optional.empty<T>() else Optional.of<T>(value.getValue())
}

inline fun <reified T: Any> getSyncedValue(id: String, syncManager: ModularSyncManager): Optional<T> {
    return getSyncedValue(id, T::class.java, syncManager)
}