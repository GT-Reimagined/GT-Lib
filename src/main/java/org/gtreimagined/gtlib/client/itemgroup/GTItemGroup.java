package org.gtreimagined.gtlib.client.itemgroup;

import lombok.Getter;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.registration.ICreativeTabProvider;
import org.gtreimagined.gtlib.registration.IGTObject;
import org.gtreimagined.gtlib.util.Utils;

import java.util.function.Supplier;

public class GTItemGroup extends CreativeModeTab implements IGTObject {

    @Getter
    protected String domain, id;
    protected Supplier<ItemStack> iconSupplier;
    static boolean registered = false;


    public GTItemGroup(String domain, String id, Supplier<ItemStack> iconSupplier) {
        super(CreativeModeTab.builder().icon(iconSupplier).title(Utils.translatable("itemGroup." + domain + "." + id)));
        this.domain = domain;
        this.id = id;
        this.iconSupplier = iconSupplier;
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, new ResourceLocation(domain, id), this);
        if (!registered){
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::buildContentsEvent);
            registered = true;
        }
    }

    public void buildContentsEvent(BuildCreativeModeTabContentsEvent event){
        NonNullList<ItemStack> items = NonNullList.create();
        addItems(event.getTab(), items);
        if (!items.isEmpty()){
            event.acceptAll(items);
        }
    }

    private static void addItems(CreativeModeTab tab, NonNullList<ItemStack> items) {
        for (Item item : GTAPI.all(Item.class)) {
            if (item instanceof ICreativeTabProvider provider){
                provider.fillItemCategory(tab, items);
            }
        }
    }

}
