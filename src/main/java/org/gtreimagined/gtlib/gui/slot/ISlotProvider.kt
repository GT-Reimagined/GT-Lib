package org.gtreimagined.gtlib.gui.slot

import brachy.modularui.widgets.slot.ModularSlot
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import org.gtreimagined.gtlib.GTAPI.get
import org.gtreimagined.gtlib.gui.SlotData
import org.gtreimagined.gtlib.gui.SlotType
import org.gtreimagined.gtlib.gui.SlotTypes
import org.gtreimagined.gtlib.machine.Tier
import java.util.function.Consumer


interface ISlotProvider<T : ISlotProvider<T>> {
    val countLookup: MutableMap<String, Object2IntOpenHashMap<SlotType<*>>>

    val slotLookup: MutableMap<String, MutableList<SlotData<*>>>

    /**
     * Adds a slot for ANY
     */
    fun <U : ModularSlot> add(type: SlotType<U>, x: Int, y: Int): T {
        return add(type) { b: SlotData.SlotDataBuilder<U> -> b.x(x).y(y) }
    }


    /**
     * Adds a slot for the given Tier
     */
    fun <U : ModularSlot> add(tier: Tier, type: SlotType<U>, x: Int, y: Int): T {
        return add(tier, type) { b -> b.x(x).y(y) }
    }

    /**
     * Adds a slot for ANY using builder
     */
    fun <U : ModularSlot> add(type: SlotType<U>, slotConsumer: Consumer<SlotData.SlotDataBuilder<U>>): T {
        return add("", SlotData.create { b -> slotConsumer.accept(b.type(type)) })
    }

    /**
     * Adds a slot for ANY using builder
     */
    fun <U : ModularSlot> add(tier: Tier, type: SlotType<U>, slotConsumer: Consumer<SlotData.SlotDataBuilder<U>>): T {
        return add(
            tier.id,
            SlotData.create { b -> slotConsumer.accept(b.type(type)) }
        )
    }

    /**
     * Copies ALL slots from an existing Machine
     */
    fun add(provider: ISlotProvider<*>): T {
        val list = provider.anySlots
        for (slot in list) {
            add("", slot)
        }
        return this as T
    }

    /**
     * Copies ALL slots from type into toTier slots
     */
    fun add(toTier: Tier, provider: ISlotProvider<*>): T {
        val list = provider.anySlots
        for (slot in list) {
            add(toTier.id, slot)
        }
        return this as T
    }

    /**
     * Copies fromTier slots from type into toTier slots
     */
    fun add(toTier: Tier, type: ISlotProvider<*>, fromTier: Tier): T {
        val list = type.getSlots(fromTier)
        for (slot in list) {
            add(toTier.id, slot)
        }
        return this as T
    }

    fun add(key: String, slot: SlotData<*>): T {
        val tier = get<Tier>(key)
        //if (tier != null && tier.getVoltage() > h.getVoltage()) highestTier = tier;
        val count =
            this.countLookup
        val slotLookup =
            this.slotLookup
        if (!count.containsKey(key)) count[key] = Object2IntOpenHashMap<SlotType<*>>()

        count[key]!!.addTo(slot.type, 1)
        if (slotLookup.containsKey(key)) {
            slotLookup[key]!!.add(slot)
        } else {
            val list: MutableList<SlotData<*>> = ObjectArrayList()
            list.add(slot)
            slotLookup[key] = list
        }
        return this as T
    }

    fun hasType(type: SlotType<*>): Boolean {
        return getCount(null, type) > 0
    }

    fun getCount(tier: Tier?, type: SlotType<*>): Int {
        val id = if (tier == null || !this.countLookup.containsKey(tier.id)) "" else tier.id
        val map = countLookup[id] ?: return 0
        return map.getInt(type)
    }

    fun hasSlots(): Boolean {
        val slots = this.slotLookup[""]
        return !slots.isNullOrEmpty()
    }

    fun hasSlots(tier: Tier): Boolean {
        val slots = this.slotLookup[tier.id]
        return !slots.isNullOrEmpty()
    }

    val anySlots: MutableList<SlotData<*>>
        get() {
            val slots =
                this.slotLookup[""]
            return slots ?: ObjectArrayList()
        }

    fun getSlots(tier: Tier?): MutableList<SlotData<*>> {
        var slots = if (tier == null) this.anySlots else this.slotLookup[tier.id]
        if (slots == null) slots = this.slotLookup[""]
        return slots ?: ObjectArrayList()
    }

    fun getRecipeSlots(tier: Tier?): MutableList<SlotData<*>> {
        var slots = if (tier == null) this.anySlots else this.slotLookup[tier.id]
        if (slots == null) slots = this.slotLookup[""]
        return if (slots != null) slots.stream()
            .filter { s: SlotData<*>? -> s!!.type === SlotTypes.FL_IN || s.type === SlotTypes.FL_OUT || s.type === SlotTypes.IT_OUT || s.type === SlotTypes.IT_IN }
            .toList() else ObjectArrayList()
    }

    fun getSlots(type: SlotType<*>, tier: Tier?): MutableList<SlotData<*>> {
        if (tier == null) return getSlots(type)
        val types: MutableList<SlotData<*>> = ObjectArrayList()
        var slots = this.slotLookup[tier.id]
        if (slots == null) slots = this.slotLookup[""]
        if (slots == null) return types //No slots found

        for (slot in slots) {
            if (slot.type === type) types.add(slot)
        }
        return types
    }

    fun getSlots(type: SlotType<*>): MutableList<SlotData<*>> {
        val types: MutableList<SlotData<*>> = ObjectArrayList()
        val slots = this.slotLookup[""] ?: return types
        //No slots found

        for (slot in slots) {
            if (slot.type === type) types.add(slot)
        }
        return types
    }

    class Provider : ISlotProvider<Provider> {
        override val countLookup: MutableMap<String, Object2IntOpenHashMap<SlotType<*>>> =
            Object2ObjectOpenHashMap()
        override val slotLookup: MutableMap<String, MutableList<SlotData<*>>> =
            Object2ObjectOpenHashMap()

    }

    companion object {
        @JvmStatic
        fun DEFAULT(): ISlotProvider<*> {
            return Provider()
        }
    }
}
