package org.gtreimagined.gtlib.gui

import brachy.modularui.api.drawable.IDrawable
import brachy.modularui.widgets.slot.ModularSlot
import java.util.function.Consumer

@JvmRecord
data class SlotData<T: ModularSlot>(
    val type: SlotType<T>,
    val x: Int,
    val y: Int,
    val jeiX: Int,
    val jeiY: Int,
    val background: IDrawable,
    val overlay: IDrawable,
    val slotGroup: Boolean,
    val data: Int
) {


    companion object {
        @JvmStatic
        fun <T: ModularSlot> create(consumer: Consumer<SlotDataBuilder<T>>): SlotData<T>{
            val builder = SlotDataBuilder<T>()
            consumer.accept(builder)
            val type = requireNotNull(builder.type) { "Must Specify a SlotType for SlotData"}
            val x = requireNotNull(builder.x) { "X must be set"}
            val y = requireNotNull(builder.y) { "Y must be set"}
            val jeiX = builder.jeiX ?: x
            val jeiY = builder.jeiY ?: y
            val baseTexture = builder.background ?: type.background
            val overlay = builder.overlay ?: type.overlay
            val slotGroup = builder.slotGroup ?: type.slotGroup
            return SlotData(type, x, y, jeiX, jeiY, baseTexture, overlay, slotGroup, builder.data)
        }
    }

    class SlotDataBuilder<T: ModularSlot> {
        var type: SlotType<T>? = null
        var x: Int? = null
        var y: Int? = null
        var jeiX: Int? = null
        var jeiY: Int? = null
        var background: IDrawable? = null
        var overlay: IDrawable? = null
        var slotGroup: Boolean? = null
        var data: Int = -1

        fun type(type: SlotType<T>): SlotDataBuilder<T>  = apply { this.type = type }
        fun x(x: Int): SlotDataBuilder<T> = apply { this.x = x }
        fun y(y: Int): SlotDataBuilder<T> = apply { this.y = y }
        fun jeiX(jeiX: Int): SlotDataBuilder<T> = apply { this.jeiX = jeiX }
        fun jeiY(jeiY: Int): SlotDataBuilder<T> = apply {this.jeiY = jeiY }
        fun background(baseTexture: IDrawable): SlotDataBuilder<T> = apply { this.background = baseTexture }
        fun overlay(overlayTexture: IDrawable): SlotDataBuilder<T> = apply { this.overlay = overlayTexture }
        fun slotGroup(slotGroup: Boolean): SlotDataBuilder<T> = apply { this.slotGroup = slotGroup }
        fun data(data: Int): SlotDataBuilder<T> = apply { this.data = data }
    }
}