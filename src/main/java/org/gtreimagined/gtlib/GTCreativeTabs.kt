package org.gtreimagined.gtlib

import net.minecraft.core.NonNullList
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.RegistryObject
import org.gtreimagined.gtlib.registration.ICreativeTabProvider
import org.gtreimagined.gtlib.util.Utils
import java.util.function.Consumer
import java.util.function.Supplier

object GTCreativeTabs {
    val TABS: DeferredRegister<CreativeModeTab?> =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Ref.ID)


    @JvmField
    val ITEMS: RegistryObject<CreativeModeTab?> = TABS.register("items") {
        CreativeModeTab.builder()
            .title(Utils.translatable("itemGroup." + Ref.ID + ".items"))
            .icon { ItemStack(Data.DEBUG_SCANNER) }.build()
    }

    @JvmField
    val TOOLS: RegistryObject<CreativeModeTab?> = TABS.register("tools",{
        CreativeModeTab.builder()
            .withTabsBefore(ITEMS.getKey())
            .title(Utils.translatable("itemGroup." + Ref.ID + ".tools"))
            .icon { ItemStack(Data.DEBUG_SCANNER) }.build()
    })
    @JvmField
    val BLOCKS: RegistryObject<CreativeModeTab?> = TABS.register("blocks",{
        CreativeModeTab.builder()
            .withTabsBefore(TOOLS.getKey())
            .title(Utils.translatable("itemGroup." + Ref.ID + ".blocks"))
            .icon { ItemStack(Data.DEBUG_SCANNER) }.build()
    })
    @JvmField
    val MATERIALS: RegistryObject<CreativeModeTab?> = TABS.register("materials",{
        CreativeModeTab.builder()
            .withTabsBefore(BLOCKS.getKey())
            .title(Utils.translatable("itemGroup." + Ref.ID + ".materials"))
            .icon { ItemStack(Data.DEBUG_SCANNER) }.build()
    })
    @JvmField
    val MACHINES: RegistryObject<CreativeModeTab?> = TABS.register("machines") {
        CreativeModeTab.builder()
            .withTabsBefore(MATERIALS.getKey())
            .title(Utils.translatable("itemGroup." + Ref.ID + ".machines"))
            .icon { ItemStack(Data.DEBUG_SCANNER) }.build()
    }

    fun init() {
        TABS.register(FMLJavaModLoadingContext.get().modEventBus)
        FMLJavaModLoadingContext.get().modEventBus
            .addListener(GTCreativeTabs::buildContentsEvent)
    }

    fun buildContentsEvent(event: BuildCreativeModeTabContentsEvent) {
        val items = NonNullList.create<ItemStack>()
        addItems(event.tabKey, items)
        if (!items.isEmpty()) {
            event.acceptAll(items)
        }
    }

    private fun addItems(tab: ResourceKey<CreativeModeTab>, items: NonNullList<ItemStack>) {
        for (item in GTAPI.all<Item>()) {
            if (item is ICreativeTabProvider) {
                item.fillItemCategory(tab, items)
            }
        }
    }
}
