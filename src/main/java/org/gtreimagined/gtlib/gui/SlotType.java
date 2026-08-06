package org.gtreimagined.gtlib.gui;

import brachy.modularui.drawable.UITexture;
import brachy.modularui.widgets.slot.ModularSlot;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.capability.IGuiHandler;
import org.gtreimagined.gtlib.capability.fluid.FluidTanks;
import org.gtreimagined.gtlib.machine.event.IMachineEvent;
import org.gtreimagined.gtlib.mui.GTGuiTextures;
import org.gtreimagined.gtlib.registration.IGTObject;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

public final class SlotType<T extends ModularSlot> implements IGTObject, IMachineEvent {

    @Getter
    private final String id;
    @Getter
    private final ISlotSupplier<T> slotSupplier;
    @Getter
    private final Function<IGuiHandler, FluidTanks> fluidHandlerSupplier;
    @Accessors(fluent = true)
    @Getter
    private final boolean mayPickup;
    @Accessors(fluent = true)
    @Getter
    private final boolean mayPlace;
    @Accessors(fluent = true)
    @Getter
    private final boolean allowExternalOutput;
    @Accessors(fluent = true)
    @Getter
    private final boolean allowExternalInput;
    @Getter
    private final boolean phantom;
    @Getter
    private final boolean slotGroup;
    @Getter
    private final UITexture texture;
    @Getter
    private final UITexture overlay;
    @Getter
    private final BiPredicate<IGuiHandler, ItemStack> tester;

    private SlotType(String id, ISlotSupplier<T> slotSupplier, Function<IGuiHandler, FluidTanks> fluidHandlerSupplier,
                    boolean mayPickup, boolean mayPlace, boolean allowExternalOutput, boolean allowExternalInput,
                    boolean phantom, boolean slotGroup, UITexture texture, UITexture overlay, BiPredicate<IGuiHandler, ItemStack> tester) {
        this.id = id;
        this.slotSupplier = slotSupplier;
        this.fluidHandlerSupplier = fluidHandlerSupplier;
        this.mayPickup = mayPickup;
        this.mayPlace = mayPlace;
        this.allowExternalOutput = allowExternalOutput;
        this.allowExternalInput = allowExternalInput;
        this.phantom = phantom;
        this.slotGroup = slotGroup;
        this.texture = texture;
        this.overlay = overlay;
        this.tester = tester;
    }

    public interface ISlotSupplier<T extends ModularSlot> {
        T get(SlotType<T> type, IGuiHandler tile, Map<SlotType<?>, IItemHandler> slots, int index, SlotData<T> data);
    }

    public static <T extends ModularSlot> SlotType<T> create(Consumer<SlotTypeBuilder<T>> consumer){
        SlotTypeBuilder<T> b = new SlotTypeBuilder<T>();
        consumer.accept(b);
        if (b.id == null) {
            throw new IllegalArgumentException("Missing id for slot type");
        }
        if (b.slotSupplier == null && b.fluidHandlerSupplier == null) {
            throw new IllegalArgumentException("Slot Type must have either a fluid handler supplier or item slot supplier!");
        }
        SlotType<T> slotType = b.build();
        GTAPI.register(SlotType.class, slotType);
        return slotType;
    }

    public static class SlotTypeBuilder<T extends ModularSlot> {
        @Setter private String id;
        @Setter private ISlotSupplier<T> slotSupplier;
        @Setter private Function<IGuiHandler, FluidTanks> fluidHandlerSupplier;
        @Setter private boolean mayPickup = true;
        @Setter private boolean mayPlace = true;
        @Setter private boolean allowExternalOutput = true;
        @Setter private boolean allowExternalInput = true;
        @Setter private boolean phantom = false;
        @Setter private boolean slotGroup = true;
        @Setter private UITexture texture = GTGuiTextures.ITEM_SLOT;
        @Setter private UITexture overlay;
        @Setter private BiPredicate<IGuiHandler, ItemStack> tester = (g, i) -> true;

        private SlotType<T> build(){
            return new SlotType<>(
                    id,
                    slotSupplier,
                    fluidHandlerSupplier,
                    mayPickup,
                    mayPlace,
                    allowExternalOutput,
                    allowExternalInput,
                    phantom,
                    slotGroup,
                    texture,
                    overlay,
                    tester
            );
        }
    }


}
