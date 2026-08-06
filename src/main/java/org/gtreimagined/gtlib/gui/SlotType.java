package org.gtreimagined.gtlib.gui;

import brachy.modularui.drawable.UITexture;
import brachy.modularui.widgets.slot.ModularSlot;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
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
import java.util.function.Function;

@Builder
public class SlotType<T extends ModularSlot> implements IGTObject, IMachineEvent {

    @Getter
    private String id;
    @Getter
    private ISlotSupplier<T> slotSupplier;
    @Getter
    private Function<IGuiHandler, FluidTanks> fluidHandlerSupplier;
    @Accessors(fluent = true)
    @Default
    @Getter
    private boolean mayPickup = true;
    @Accessors(fluent = true)
    @Default
    @Getter
    private boolean mayPlace = true;
    @Accessors(fluent = true)
    @Default
    @Getter
    private boolean allowExternalOutput = true;
    @Accessors(fluent = true)
    @Default
    @Getter
    private boolean allowExternalInput = true;
    @Default
    @Getter
    private boolean phantom = false;
    @Default
    @Getter
    private boolean slotGroup = true;
    @Default
    @Getter
    private UITexture texture = GTGuiTextures.ITEM_SLOT;
    @Getter
    private UITexture overlay;
    @Getter
    @Default
    private BiPredicate<IGuiHandler, ItemStack> tester = (g, i) -> true;

    public interface ISlotSupplier<T extends ModularSlot> {
        T get(SlotType<T> type, IGuiHandler tile, Map<SlotType<?>, IItemHandler> slots, int index, SlotData<T> data);
    }

    public static class ItIn implements BiPredicate<IGuiHandler, ItemStack> {

        @Override
        public boolean test(IGuiHandler iGuiHandler, ItemStack stack) {
            if (iGuiHandler instanceof BlockEntityMachine) {
                return (((BlockEntityMachine<?>) iGuiHandler).recipeHandler.map(rh -> rh.accepts(stack)).orElse(true));
            }
            return true;
        }
    }

    public static <T extends ModularSlot> SlotTypeBuilder<T> builder(){
        return new CustomSlotTypeBuilder<>();
    }

    public static class CustomSlotTypeBuilder<T extends ModularSlot> extends SlotTypeBuilder<T> {

        @Override
        public SlotType<T> build() {
            SlotType<T> build = super.build();
            if (build.id == null) {
                throw new IllegalArgumentException("Missing id for slot type");
            }
            if (build.slotSupplier == null && build.fluidHandlerSupplier == null) {
                throw new IllegalArgumentException("Slot Type must have either a fluid handler supplier or item slot supplier!");
            }
            GTAPI.register(SlotType.class, build);
            return build;
        }
    }

}
