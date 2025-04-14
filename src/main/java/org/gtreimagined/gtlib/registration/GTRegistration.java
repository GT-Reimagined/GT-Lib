package org.gtreimagined.gtlib.registration;

import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.GTLib;
import org.gtreimagined.gtlib.Data;
import org.gtreimagined.gtlib.MaterialDataInit;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.block.GTItemBlock;
import org.gtreimagined.gtlib.event.MaterialEvent;
import org.gtreimagined.gtlib.fluid.GTFluid;
import org.gtreimagined.gtlib.integration.kubejs.GTLibKubeJS;
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
import net.minecraftforge.event.RegistryEvent;
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
    public static void onRegister(final RegistryEvent.Register<?> e) {
        final String domain = ModLoadingContext.get().getActiveNamespace();
        List<IGTRegistrar> list2 = GTAPI.all(IGTRegistrar.class).stream().sorted((c1, c2) -> Integer.compare(c2.getPriority(), c1.getPriority())).toList();
        if (list2.size() < 4) {
            GTLib.LOGGER.info("Mod ID: " + domain + " & event: " + e.getRegistry().getRegistryName());
        }
        onRegister(domain, e);
        onRegister(Ref.SHARED_ID, e);
        List<IGTRegistrar> list = GTAPI.all(IGTRegistrar.class).stream().sorted((c1, c2) -> Integer.compare(c2.getPriority(), c1.getPriority())).toList();
        list.forEach(r -> {
            onRegister(r.getId(), e);
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void onRegister(final String domain, final RegistryEvent.Register<?> e) {
        ModContainer previous = ModLoadingContext.get().getActiveContainer();
        ModContainer newContainer = ModList.get().getModContainerById(domain).orElse(null);
        if (newContainer == null) return;
        if (!domain.equals(Ref.ID)){
            ModLoadingContext.get().setActiveContainer(newContainer);
        }
        if (domain.equals(Ref.ID)) {
            List<IGTRegistrar> list = GTAPI.all(IGTRegistrar.class).stream().sorted((c1, c2) -> Integer.compare(c2.getPriority(), c1.getPriority())).filter(IGTRegistrar::isEnabled).toList();
            if (e.getRegistry() == ForgeRegistries.BLOCKS) {
                GTAPI.onRegistration(RegistrationEvent.DATA_INIT);
                GTAPI.all(SoundEvent.class, t -> {
                    if (t.getRegistryName() == null) t.setRegistryName(t.getLocation());
                });
                MaterialEvent event = new MaterialEvent();
                MaterialDataInit.onMaterialEvent(event);
                list.forEach(r -> r.onMaterialEvent(event));
                if (GTAPI.isModLoaded(Ref.MOD_KJS)) {
                    GTLibKubeJS.loadMaterialEvent(event);
                }
                Data.postInit();
            }
            GTAPI.all(IRegistryEntryProvider.class, domain, p -> p.onRegistryBuild(e.getRegistry()));
            GTAPI.all(IRegistryEntryProvider.class, Ref.SHARED_ID, p -> p.onRegistryBuild(e.getRegistry()));
            list.forEach(r -> GTAPI.all(IRegistryEntryProvider.class, r.getDomain(), p -> p.onRegistryBuild(e.getRegistry())));
        }
        if (e.getRegistry() == ForgeRegistries.BLOCKS) {
            GTAPI.all(Block.class, domain, (b, d, i) -> {
                if (b.getRegistryName() == null)
                    b.setRegistryName(d, i);
                if (!(b instanceof IItemBlockProvider pb) || pb.generateItemBlock()) {
                    GTAPI.register(Item.class, i, d, b instanceof IItemBlockProvider pb ? pb.getItemBlock() : new GTItemBlock(b));
                }
                ((IForgeRegistry) e.getRegistry()).register(b);
            });

        } else if (e.getRegistry() == ForgeRegistries.ITEMS) {
            GTAPI.all(Item.class, domain, (i, d, id) -> {
                if (i.getRegistryName() == null)
                    i.setRegistryName(d, id);
                ((IForgeRegistry) e.getRegistry()).register(i);
            });
            registerTools(domain, e.getRegistry());
        } else if (e.getRegistry() == ForgeRegistries.BLOCK_ENTITIES) {
            GTAPI.all(BlockEntityType.class, domain, (t, d, i) -> {
                if (t.getRegistryName() == null) t.setRegistryName(d, i);
                ((IForgeRegistry) e.getRegistry()).register(t);
            });
        } else if (e.getRegistry() == ForgeRegistries.FLUIDS) {
            GTAPI.all(GTFluid.class, domain, f -> {
                if (f.getFluid().getRegistryName() == null) f.getFluid().setRegistryName(domain, f.getId());
                if (f.getFlowingFluid().getRegistryName() == null) f.getFlowingFluid().setRegistryName(domain, "flowing_".concat(f.getId()));
                ((IForgeRegistry) e.getRegistry()).registerAll(f.getFluid(), f.getFlowingFluid());
            });
        } else if (e.getRegistry() == ForgeRegistries.CONTAINERS) {
            GTAPI.all(MenuType.class, domain, (h, d, i) -> {
                if (h.getRegistryName() == null) h.setRegistryName(d, i);
                ((IForgeRegistry) e.getRegistry()).register(h);
            });
        } else if (e.getRegistry() == ForgeRegistries.SOUND_EVENTS) {
            GTAPI.all(SoundEvent.class, domain, (t, d, i) -> {
                if (t.getRegistryName() == null) t.setRegistryName(d, i);
                ((IForgeRegistry) e.getRegistry()).register(t);
            });
        } else if (e.getRegistry() == ForgeRegistries.RECIPE_SERIALIZERS) {
            //TODO better solution for this
            GTAPI.all(IIngredientSerializer.class, domain, (s, d, i) -> {
                CraftingHelper.register(new ResourceLocation(d, i), s);
            });
            if (domain.equals(Ref.ID)) {
                CraftingHelper.register(ConfigCondition.Serializer.INSTANCE);
                CraftingHelper.register(TomlConfigCondition.Serializer.INSTANCE);
            }
            GTAPI.all(RecipeSerializer.class, domain, (r, d, i) -> {
                if (r.getRegistryName() == null){
                    r.setRegistryName(new ResourceLocation(d, i));
                }
                ((IForgeRegistry) e.getRegistry()).register(r);
            });
        } else if (e.getRegistry() == ForgeRegistries.FEATURES) {
            GTAPI.all(IGTFeature.class, domain,(t, d, i) -> {
                if (t.asFeature().getRegistryName() == null) t.asFeature().setRegistryName(d, i);
                ((IForgeRegistry) e.getRegistry()).register(t.asFeature());
            });
        } else if (e.getRegistry() == ForgeRegistries.ENCHANTMENTS){
            GTAPI.all(Enchantment.class, domain, (en, d, i) -> {
                if (en.getRegistryName() == null) en.setRegistryName(d, i);
                ((IForgeRegistry) e.getRegistry()).register(en);
            });
        }
        if (!domain.equals(Ref.ID)){
            ModLoadingContext.get().setActiveContainer(previous);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void registerTools(String domain, IForgeRegistry registry) {
        GTAPI.all(GTToolType.class, domain, t -> {
            List<IGTTool> tools = t.isPowered() ? t.instantiatePoweredTools(domain) : t.instantiateTools(domain);
            for (IGTTool i : tools) {
                if (i.getItem().getRegistryName() == null) i.getItem().setRegistryName(domain, i.getId());
                registry.register(i.getItem());
            }
        });
        GTAPI.all(GTArmorType.class, domain, t -> {
            List<IGTArmor> i = t.instantiateTools();
            i.forEach(a -> {
                if (a.getItem().getRegistryName() == null) a.getItem().setRegistryName(domain, a.getId());
                registry.register(a.getItem());
            });

        });
    }
}
