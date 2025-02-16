package muramasa.antimatter.util;

import carbonconfiglib.config.Config;
import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.config.ConfigSettings;
import com.mojang.math.Matrix4f;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.common.fluid.utils.FluidHooks;
import muramasa.antimatter.event.CraftingEvent;
import muramasa.antimatter.event.ProvidersEvent;
import muramasa.antimatter.event.WorldGenEvent;
import muramasa.antimatter.machine.types.BasicMultiMachine;
import muramasa.antimatter.recipe.loader.IRecipeRegistrate;
import muramasa.antimatter.registration.IAntimatterRegistrar;
import muramasa.antimatter.registration.Side;
import muramasa.antimatter.structure.Pattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;


public interface AntimatterPlatformUtils {
    AntimatterPlatformUtils INSTANCE = ImplLoader.load(AntimatterPlatformUtils.class);

    default FluidHolder fromTag(CompoundTag tag){
        if (tag == null) {
            return FluidHooks.emptyFluid();
        }
        if (!tag.contains("FluidName", Tag.TAG_STRING)) {
            return FluidHooks.fluidFromCompound(tag);
        }

        ResourceLocation fluidName = new ResourceLocation(tag.getString("FluidName"));
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidName);
        if (fluid == null) {
            return FluidHooks.emptyFluid();
        }
        FluidHolder stack = FluidHooks.newFluidHolder(fluid, tag.getInt("Amount"), null);
        if (tag.contains("Tag", Tag.TAG_COMPOUND)) {
            stack.setCompound(tag.getCompound("Tag"));
        }
        return stack;
    }

    CraftingEvent postCraftingEvent(IAntimatterRegistrar registrar);

    void postLoaderEvent(IAntimatterRegistrar registrar, IRecipeRegistrate reg);

    ProvidersEvent postProviderEvent(Side side, IAntimatterRegistrar registrar);

    WorldGenEvent postWorldEvent(IAntimatterRegistrar registrar);

    void addMultiMachineInfo(BasicMultiMachine<?> machine, List<Pattern> patterns);

    Item.Properties getToolProperties(CreativeModeTab group, boolean repairable);
}
