package org.gtreimagined.gtlib;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.gtreimagined.gtlib.registration.ICreativeTabProvider;
import org.gtreimagined.gtlib.util.Utils;

public class GTCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Ref.ID);


    public static final RegistryObject<CreativeModeTab> ITEMS = TABS.register("items", () -> CreativeModeTab.builder()
            .title(Utils.translatable("itemGroup." + Ref.ID + ".items"))
            .icon(() -> new ItemStack(Data.DEBUG_SCANNER)).build());
    public static final RegistryObject<CreativeModeTab> TOOLS = TABS.register( "tools", () -> CreativeModeTab.builder()
            .withTabsBefore(ITEMS.getKey())
            .title(Utils.translatable("itemGroup." + Ref.ID + ".tools"))
            .icon( () -> new ItemStack(Data.DEBUG_SCANNER)).build());
    public static final RegistryObject<CreativeModeTab> BLOCKS = TABS.register( "blocks", () -> CreativeModeTab.builder()
            .withTabsBefore(TOOLS.getKey())
            .title(Utils.translatable("itemGroup." + Ref.ID + ".blocks"))
            .icon( () -> new ItemStack(Data.DEBUG_SCANNER)).build());
    public static final RegistryObject<CreativeModeTab> MATERIALS = TABS.register( "materials", () -> CreativeModeTab.builder()
            .withTabsBefore(BLOCKS.getKey())
            .title(Utils.translatable("itemGroup." + Ref.ID + ".materials"))
            .icon( () -> new ItemStack(Data.DEBUG_SCANNER)).build());
    public static final RegistryObject<CreativeModeTab> MACHINES = TABS.register( "machines", () -> CreativeModeTab.builder()
            .withTabsBefore(MATERIALS.getKey())
            .title(Utils.translatable("itemGroup." + Ref.ID + ".machines"))
            .icon( () -> new ItemStack(Data.DEBUG_SCANNER)).build());

    public static void init(){
        TABS.register(FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(GTCreativeTabs::buildContentsEvent);
    }

    public static void buildContentsEvent(BuildCreativeModeTabContentsEvent event){
        NonNullList<ItemStack> items = NonNullList.create();
        addItems(event.getTabKey(), items);
        if (!items.isEmpty()){
            event.acceptAll(items);
        }
    }

    private static void addItems(ResourceKey<CreativeModeTab> tab, NonNullList<ItemStack> items) {
        for (Item item : GTAPI.all(Item.class)) {
            if (item instanceof ICreativeTabProvider provider){
                provider.fillItemCategory(tab, items);
            }
        }
    }
}
