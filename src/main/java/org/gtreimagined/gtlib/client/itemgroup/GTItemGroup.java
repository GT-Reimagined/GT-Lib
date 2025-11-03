package org.gtreimagined.gtlib.client.itemgroup;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.gtreimagined.gtlib.util.Utils;

import java.util.function.Supplier;

public class GTItemGroup extends CreativeModeTab {

    protected String domain, id;
    protected Supplier<ItemStack> iconSupplier;


    public GTItemGroup(String domain, String id, Supplier<ItemStack> iconSupplier) {
        super(CreativeModeTab.builder().icon(iconSupplier).title(Utils.translatable("itemGroup." + domain + "." + id)));
        this.domain = domain;
        this.id = id;
        this.iconSupplier = iconSupplier;
    }

    public String getDomain() {
        return domain;
    }

    public String getGroupId() {
        return id;
    }

}
