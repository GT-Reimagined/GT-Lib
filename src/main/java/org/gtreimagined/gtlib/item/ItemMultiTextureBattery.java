package org.gtreimagined.gtlib.item;

import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.client.RenderHelper;
import org.gtreimagined.gtlib.datagen.builder.GTItemModelBuilder;
import org.gtreimagined.gtlib.datagen.providers.GTItemModelProvider;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.texture.Texture;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

public class ItemMultiTextureBattery extends ItemBattery {

    public ItemMultiTextureBattery(String domain, String id, Tier tier, long cap, boolean reusable) {
        this(domain, id, tier, cap, 1, reusable);
    }

    public ItemMultiTextureBattery(String domain, String id, Tier tier, long cap, int amps, boolean reusable) {
        super(domain, id, tier, cap, amps, reusable);
        Utils.unsafeRunForDistVoid(() -> () -> RenderHelper.registerBatteryPropertyOverrides(this), () -> () -> {});
    }

    @Override
    public void onItemModelBuild(ItemLike item, GTItemModelProvider prov) {
        String id = this.getId();
        GTItemModelBuilder[] builders = new GTItemModelBuilder[8];
        for (int i = 0; i < 8; i++) {
            GTItemModelBuilder builder = prov.getBuilder(id + i);
            builder.parent(new ResourceLocation("minecraft", "item/handheld"));
            builder.texture("layer0", new Texture(getDomain(), "item/basic/" + getId() + "/" + i));
            builders[i] = builder;
        }

        prov.tex(item, "minecraft:item/handheld", new Texture(getDomain(), "item/basic/" + getId() + "/1")).override().predicate(new ResourceLocation(Ref.ID, "battery"), 0.0F).model(new ResourceLocation(getDomain(), "item/" + id + "0")).end().override().predicate(new ResourceLocation(Ref.ID, "battery"), 0.01F).model(new ResourceLocation(getDomain(), "item/" + id + "1")).end().override().predicate(new ResourceLocation(Ref.ID, "battery"), 0.173F).model(new ResourceLocation(getDomain(), "item/" + id + "2")).end().override().predicate(new ResourceLocation(Ref.ID, "battery"), 0.336F).model(new ResourceLocation(getDomain(), "item/" + id + "3")).end().override().predicate(new ResourceLocation(Ref.ID, "battery"), 0.499F).model(new ResourceLocation(getDomain(), "item/" + id + "4")).end().override().predicate(new ResourceLocation(Ref.ID, "battery"), 0.662F).model(new ResourceLocation(getDomain(), "item/" + id + "5")).end().override().predicate(new ResourceLocation(Ref.ID, "battery"), 0.825F).model(new ResourceLocation(getDomain(), "item/" + id + "6")).end().override().predicate(new ResourceLocation(Ref.ID, "battery"), .99F).model(new ResourceLocation(getDomain(), "item/" + id + "7"));
    }
}
