package org.gtreimagined.gtlib.registration

import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.material.Fluid
import net.minecraftforge.common.crafting.CraftingHelper
import net.minecraftforge.common.crafting.IIngredientSerializer
import net.minecraftforge.fluids.FluidType
import net.minecraftforge.fml.ModContainer
import net.minecraftforge.fml.ModList
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegisterEvent
import org.gtreimagined.gtlib.Data
import org.gtreimagined.gtlib.GTAPI
import org.gtreimagined.gtlib.GTLib
import org.gtreimagined.gtlib.MaterialDataInit
import org.gtreimagined.gtlib.Ref
import org.gtreimagined.gtlib.block.GTItemBlock
import org.gtreimagined.gtlib.event.MaterialEvent
import org.gtreimagined.gtlib.integration.kubejs.GTLibKubeJS
import org.gtreimagined.gtlib.recipe.Recipe
import org.gtreimagined.gtlib.recipe.condition.ConfigCondition
import org.gtreimagined.gtlib.recipe.condition.TomlConfigCondition
import org.gtreimagined.gtlib.tool.GTToolType
import org.gtreimagined.gtlib.tool.armor.GTArmorType
import org.gtreimagined.gtlib.worldgen.feature.IGTFeature
import thedarkcolour.kotlinforforge.forge.LOADING_CONTEXT
import java.util.function.Consumer

@Suppress("removal", "DEPRECATION")
object GTRegistration {
    fun onRegister(e: RegisterEvent) {
        val domain = LOADING_CONTEXT.activeNamespace
        val list2 = GTAPI.all(IGTRegistrar::class.java).stream()
            .sorted { c1, c2 -> c2.priority.compareTo(c1.priority) }
            .toList()
        if (list2.size < 4) {
            GTLib.LOGGER.info("Mod ID: " + domain + " & event: " + e.registryKey.location())
        }
        onRegister(domain, e)
        onRegister(Ref.SHARED_ID, e)
        val list = GTAPI.all(IGTRegistrar::class.java).stream()
            .sorted { c1, c2 -> c2.priority.compareTo(c1.priority) }
            .toList()
        list.forEach { r ->
            onRegister(r.getId(), e)
        }
    }

    fun onRegister(domain: String, e: RegisterEvent) {
        val previous = LOADING_CONTEXT.activeContainer
        val newContainer: ModContainer? = ModList.get().getModContainerById(domain).orElse(null) ?: return
        if (domain != Ref.ID) {
            LOADING_CONTEXT.setActiveContainer(newContainer)
        }
        if (domain == Ref.ID) {
            val list = GTAPI.all(IGTRegistrar::class.java).stream()
                .sorted { c1, c2 ->
                    c2.priority.compareTo(c1.priority)
                }.filter { it.isEnabled }.toList()
            if (e.registryKey === ForgeRegistries.Keys.SOUND_EVENTS) {
                GTAPI.onRegistration(RegistrationEvent.DATA_INIT)
                val event: MaterialEvent<*> = MaterialEvent()
                MaterialDataInit.onMaterialEvent(event)
                list.forEach(Consumer { r -> r.onMaterialEvent(event) })
                if (GTAPI.isModLoaded(Ref.MOD_KJS)) {
                    GTLibKubeJS.loadMaterialEvent(event)
                }
                Data.postInit()
            }
            GTAPI.all(IRegistryEntryProvider::class.java, domain) { p ->
                p.onRegistryBuild(e.registryKey)
            }
            GTAPI.all(IRegistryEntryProvider::class.java, Ref.SHARED_ID) { p ->
                p.onRegistryBuild(e.registryKey)
            }
            list.forEach { r ->
                GTAPI.all(IRegistryEntryProvider::class.java, r.domain) { p ->
                    p.onRegistryBuild(e.registryKey)
                }
            }
        }
        if (e.registryKey === ForgeRegistries.Keys.BLOCKS) {
            GTAPI.all(Block::class.java, domain) { b, d, i ->
                if (b !is IItemBlockProvider || b.generateItemBlock()) {
                    GTAPI.register<Item>(Item::class.java, i, d,
                        if (b is IItemBlockProvider) b.itemBlock else GTItemBlock(b)
                    )
                }
                ForgeRegistries.BLOCKS.register(ResourceLocation(d, i), b)
            }
        } else if (e.registryKey === ForgeRegistries.Keys.ITEMS) {
            GTAPI.all(Item::class.java, domain) { it, d, i ->
                ForgeRegistries.ITEMS.register(
                    ResourceLocation(d, i), it
                )
            }
            registerTools(domain)
        } else if (e.registryKey === ForgeRegistries.Keys.BLOCK_ENTITY_TYPES) {
            GTAPI.all(BlockEntityType::class.java, domain) { t, d, i ->
                ForgeRegistries.BLOCK_ENTITY_TYPES.register(ResourceLocation(d, i), t)
            }
        } else if (e.registryKey === ForgeRegistries.Keys.FLUIDS) {
            GTAPI.all(Fluid::class.java, domain) { f, d, i ->
                ForgeRegistries.FLUIDS.register(ResourceLocation(d, i), f)
            }
        } else if (e.registryKey === ForgeRegistries.Keys.FLUID_TYPES) {
            GTAPI.all(FluidType::class.java, domain) { f, d, i ->
                ForgeRegistries.FLUID_TYPES.get().register(ResourceLocation(d, i), f)
            }
        } else if (e.registryKey === ForgeRegistries.Keys.MENU_TYPES) {
            GTAPI.all(MenuType::class.java, domain) { h, d, i ->
                ForgeRegistries.MENU_TYPES.register(ResourceLocation(d, i), h)
            }
        } else if (e.registryKey === ForgeRegistries.Keys.SOUND_EVENTS) {
            GTAPI.all(SoundEvent::class.java, domain) { t, d, i ->
                ForgeRegistries.SOUND_EVENTS.register(ResourceLocation(d, i), t)
            }
        } else if (e.registryKey === ForgeRegistries.Keys.RECIPE_SERIALIZERS) {
            //TODO better solution for this
            GTAPI.all(IIngredientSerializer::class.java, domain) { s, d, i ->
                CraftingHelper.register(ResourceLocation(d, i), s)
            }
            if (domain == Ref.ID) {
                CraftingHelper.register(ConfigCondition.Serializer.INSTANCE)
                CraftingHelper.register(TomlConfigCondition.Serializer.INSTANCE)
            }
            GTAPI.all(RecipeSerializer::class.java, domain) { r, d, i ->
                ForgeRegistries.RECIPE_SERIALIZERS.register(ResourceLocation(d, i), r)
            }
        } else if (e.registryKey === ForgeRegistries.Keys.FEATURES) {
            GTAPI.all(IGTFeature::class.java, domain) { t, d, i ->
                ForgeRegistries.FEATURES.register(ResourceLocation(d, i), t!!.asFeature())
            }
        } else if (e.registryKey === ForgeRegistries.Keys.ENCHANTMENTS) {
            GTAPI.all(Enchantment::class.java, domain) { en, d, i ->
                ForgeRegistries.ENCHANTMENTS.register(ResourceLocation(d, i), en)
            }
        }
        if (domain == Ref.ID) {
            if (e.registryKey === ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS) {
                ForgeRegistries.BIOME_MODIFIER_SERIALIZERS.get()
                    .register(ResourceLocation(Ref.ID, "modifier"), GTBiomeModifier.CODEC)
            } else if (e.registryKey === ForgeRegistries.Keys.RECIPE_TYPES) {
                ForgeRegistries.RECIPE_TYPES.register(ResourceLocation(Ref.ID, "machine"), Recipe.RECIPE_TYPE)
            }
        }
        if (domain != Ref.ID) {
            LOADING_CONTEXT.setActiveContainer(previous)
        }
    }

    fun registerTools(domain: String) {
        GTAPI.all(GTToolType::class.java, domain) { t ->
            val tools = if (t.isPowered) t.instantiatePoweredTools(domain) else t.instantiateTools(domain)
            for (i in tools) {
                ForgeRegistries.ITEMS.register(ResourceLocation(domain, i.getId()), i.item)
            }
        }
        GTAPI.all(GTArmorType::class.java, domain) { t ->
            val i = t.instantiateTools()
            i.forEach { a ->
                ForgeRegistries.ITEMS.register(ResourceLocation(domain, a.getId()), a.item)
            }
        }
    }
}
