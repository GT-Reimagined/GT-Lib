package org.gtreimagined.gtlib.registration;

import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler.For;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries.Keys;
import net.minecraftforge.registries.RegisterEvent;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.GTLib;
import org.gtreimagined.gtlib.Data;
import org.gtreimagined.gtlib.MaterialDataInit;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.block.GTItemBlock;
import org.gtreimagined.gtlib.event.MaterialEvent;
import org.gtreimagined.gtlib.fluid.GTFluid;
import org.gtreimagined.gtlib.integration.kubejs.GTLibKubeJS;
import org.gtreimagined.gtlib.recipe.Recipe;
import org.gtreimagined.gtlib.recipe.condition.ConfigCondition;
import org.gtreimagined.gtlib.recipe.condition.TomlConfigCondition;
import org.gtreimagined.gtlib.tool.GTToolType;
import org.gtreimagined.gtlib.tool.IGTArmor;
import org.gtreimagined.gtlib.tool.IGTTool;
import org.gtreimagined.gtlib.tool.armor.GTArmorType;
import org.gtreimagined.gtlib.worldgen.feature.IGTFeature;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.List;

@Mod.EventBusSubscriber(modid = Ref.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class GTRegistration {

    @SubscribeEvent
    public static void onRegister(final RegisterEvent e) {
        final String domain = ModLoadingContext.get().getActiveNamespace();
        List<IGTRegistrar> list2 = GTAPI.all(IGTRegistrar.class).stream().sorted((c1, c2) -> Integer.compare(c2.getPriority(), c1.getPriority())).toList();
        if (list2.size() < 4) {
            GTLib.LOGGER.info("Mod ID: " + domain + " & event: " + e.getRegistryKey().location());
        }
        onRegister(domain, e);
        onRegister(Ref.SHARED_ID, e);
        List<IGTRegistrar> list = GTAPI.all(IGTRegistrar.class).stream().sorted((c1, c2) -> Integer.compare(c2.getPriority(), c1.getPriority())).toList();
        list.forEach(r -> {
            onRegister(r.getId(), e);
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void onRegister(final String domain, final RegisterEvent e) {
        ModContainer previous = ModLoadingContext.get().getActiveContainer();
        ModContainer newContainer = ModList.get().getModContainerById(domain).orElse(null);
        if (newContainer == null) return;
        if (!domain.equals(Ref.ID)){
            ModLoadingContext.get().setActiveContainer(newContainer);
        }
        if (domain.equals(Ref.ID)) {
            List<IGTRegistrar> list = GTAPI.all(IGTRegistrar.class).stream().sorted((c1, c2) -> Integer.compare(c2.getPriority(), c1.getPriority())).filter(IGTRegistrar::isEnabled).toList();
            if (e.getRegistryKey() == Keys.SOUND_EVENTS) {
                GTAPI.onRegistration(RegistrationEvent.DATA_INIT);
                MaterialEvent event = new MaterialEvent();
                MaterialDataInit.onMaterialEvent(event);
                list.forEach(r -> r.onMaterialEvent(event));
                if (GTAPI.isModLoaded(Ref.MOD_KJS)) {
                    GTLibKubeJS.loadMaterialEvent(event);
                }
                Data.postInit();
            }
            GTAPI.all(IRegistryEntryProvider.class, domain, p -> p.onRegistryBuild(e.getRegistryKey()));
            GTAPI.all(IRegistryEntryProvider.class, Ref.SHARED_ID, p -> p.onRegistryBuild(e.getRegistryKey()));
            list.forEach(r -> GTAPI.all(IRegistryEntryProvider.class, r.getDomain(), p -> p.onRegistryBuild(e.getRegistryKey())));
        }
        if (e.getRegistryKey() == Keys.BLOCKS) {
            GTAPI.all(Block.class, domain, (b, d, i) -> {
                if (!(b instanceof IItemBlockProvider pb) || pb.generateItemBlock()) {
                    GTAPI.register(Item.class, i, d, b instanceof IItemBlockProvider pb ? pb.getItemBlock() : new GTItemBlock(b));
                }
                ForgeRegistries.BLOCKS.register(new ResourceLocation(d, i), b);
            });

        } else if (e.getRegistryKey() == Keys.ITEMS) {
            GTAPI.all(Item.class, domain, (it, d, i) -> {
                ForgeRegistries.ITEMS.register(new ResourceLocation(d, i), it);
            });
            registerTools(domain);
        } else if (e.getRegistryKey() == Keys.BLOCK_ENTITY_TYPES) {
            GTAPI.all(BlockEntityType.class, domain, (t, d, i) -> {
                ForgeRegistries.BLOCK_ENTITY_TYPES.register(new ResourceLocation(d, i), t);
            });
        } else if (e.getRegistryKey() == Keys.FLUIDS) {
            GTAPI.all(Fluid.class, domain, (f, d, i) -> {
                ForgeRegistries.FLUIDS.register(new ResourceLocation(d, i), f);
            });
        } else if (e.getRegistryKey() == Keys.FLUID_TYPES) {
            GTAPI.all(FluidType.class, domain, (f, d, i) -> {
                ForgeRegistries.FLUID_TYPES.get().register(new ResourceLocation(d, i), f);
            });
        } else if (e.getRegistryKey() == Keys.MENU_TYPES) {
            GTAPI.all(MenuType.class, domain, (h, d, i) -> {
                ForgeRegistries.MENU_TYPES.register(new ResourceLocation(d, i), h);
            });
        } else if (e.getRegistryKey() == Keys.SOUND_EVENTS) {
            GTAPI.all(SoundEvent.class, domain, (t, d, i) -> {
                ForgeRegistries.SOUND_EVENTS.register(new ResourceLocation(d, i), t);
            });
        }  else if (e.getRegistryKey() == Keys.RECIPE_SERIALIZERS) {
            //TODO better solution for this
            GTAPI.all(IIngredientSerializer.class, domain, (s, d, i) -> {
                CraftingHelper.register(new ResourceLocation(d, i), s);
            });
            if (domain.equals(Ref.ID)) {
                CraftingHelper.register(ConfigCondition.Serializer.INSTANCE);
                CraftingHelper.register(TomlConfigCondition.Serializer.INSTANCE);
            }
            GTAPI.all(RecipeSerializer.class, domain, (r, d, i) -> {
                ForgeRegistries.RECIPE_SERIALIZERS.register(new ResourceLocation(d, i), r);
            });
        } else if (e.getRegistryKey() == Keys.FEATURES) {
            GTAPI.all(IGTFeature.class, domain,(t, d, i) -> {
                ForgeRegistries.FEATURES.register(new ResourceLocation(d, i), t.asFeature());
            });
        } else if (e.getRegistryKey() == Keys.ENCHANTMENTS){
            GTAPI.all(Enchantment.class, domain, (en, d, i) -> {
                ForgeRegistries.ENCHANTMENTS.register(new ResourceLocation(d, i), en);
            });
        }
        if (domain.equals(Ref.ID)){
            if (e.getRegistryKey() == ForgeRegistries.Keys.BIOME_MODIFIERS){
                e.getForgeRegistry().register(new ResourceLocation(Ref.ID, "modifier"), new GTBiomeModifier());
            } else if (e.getRegistryKey() == ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS){
                e.getForgeRegistry().register(new ResourceLocation(Ref.ID, "modifier"), GTBiomeModifier.CODEC);
            } else if (e.getRegistryKey() == ForgeRegistries.Keys.RECIPE_TYPES){
                e.getForgeRegistry().register(new ResourceLocation(Ref.ID, "machine"), Recipe.RECIPE_TYPE);
            }
        }
        if (!domain.equals(Ref.ID)){
            ModLoadingContext.get().setActiveContainer(previous);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void registerTools(String domain) {
        GTAPI.all(GTToolType.class, domain, t -> {
            List<IGTTool> tools = t.isPowered() ? t.instantiatePoweredTools(domain) : t.instantiateTools(domain);
            for (IGTTool i : tools) {
                ForgeRegistries.ITEMS.register(new ResourceLocation(domain, i.getId()), i.getItem());
            }
        });
        GTAPI.all(GTArmorType.class, domain, t -> {
            List<IGTArmor> i = t.instantiateTools();
            i.forEach(a -> {
                ForgeRegistries.ITEMS.register(new ResourceLocation(domain, a.getId()), a.getItem());
            });

        });
    }
}
