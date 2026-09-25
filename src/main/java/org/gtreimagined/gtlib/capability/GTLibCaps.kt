@file:JvmName("GTLibCaps")
package org.gtreimagined.gtlib.capability

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import net.minecraftforge.common.capabilities.Capability

val CAP_MAP: BiMap<Class<*>, Capability<*>> = HashBiMap.create()