package org.gtreimagined.gtlib.common

import net.minecraft.core.Direction

interface EntityPlaceEventExtension {
    fun gtlib_getPlacedDirection() : Direction?

    fun gtlib_setPlacedDirection(direction: Direction?)
}