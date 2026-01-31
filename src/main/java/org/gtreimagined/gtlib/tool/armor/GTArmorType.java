package org.gtreimagined.gtlib.tool.armor;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ArmorItem;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.GTCreativeTabs;
import org.gtreimagined.gtlib.material.IMaterialTag;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialTags;
import org.gtreimagined.gtlib.registration.IGTObject;
import org.gtreimagined.gtlib.tool.IGTArmor;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Accessors(chain = true)
public class GTArmorType implements IGTObject {
    private final String domain, id;
    @Getter
    private final List<Component> tooltip = new ObjectArrayList<>();
    @Getter
    private final boolean repairable;
    @Getter
    private final int durabilityFactor, extraArmor;
    @Getter
    private final float extraToughness, extraKnockback;
    @Getter
    @Setter
    private ResourceKey<CreativeModeTab> itemGroup;
    @Getter
    @Setter
    private ArmorItem.Type armorType;
    @Getter
    private SoundEvent event;
    @Getter
    int overlayLayers;
    @Nullable
    private IMaterialTag materialRequirement;

    /**
     * Instantiates a GTArmorType with its basic values
     *
     * @param domain           unique identifier provided by the mod
     * @param id               unique identifier
     * @param durabilityFactor durability multiplier used to determine the durability of an armor piece, it is multiplied by these to determine the durability: 13(head), 15(chest), 16(legs), 11(feet)
     * @param extraArmor       extra armor protection that would be applied to item's attribute on top of material value
     * @param extraToughness   extra toughness that would be applied to item's attribute on top of material value
     * @param extraKnockback   extra knockback resistance that would be applied to the item's attributes on top of material value
     * @param armorType             armor slot the item goes in
     * @return a brand new GTArmorType for enjoyment
     */
    public GTArmorType(String domain, String id, int durabilityFactor, int extraArmor, float extraToughness, float extraKnockback, ArmorItem.Type armorType) {
        this.domain = domain;
        this.id = id;
        this.repairable = true;
        this.durabilityFactor = durabilityFactor;
        this.extraArmor = extraArmor;
        this.extraToughness = extraToughness;
        this.extraKnockback = extraKnockback;
        this.itemGroup = GTCreativeTabs.TOOLS.getKey();
        this.armorType = armorType;
        this.event = SoundEvents.ARMOR_EQUIP_IRON;
        this.overlayLayers = 0;
        GTAPI.register(GTArmorType.class, this);
    }

    public List<IGTArmor> instantiateTools() {
        List<IGTArmor> armors = new ArrayList<>();
        MaterialTags.ARMOR.all().forEach(m -> {
            armors.add(new MaterialArmor(domain, this, m, armorType, prepareInstantiation(domain)));
        });
        return armors;
    }

    public List<IGTArmor> instantiateTools(String domain, Supplier<Item.Properties> properties) {
        List<IGTArmor> armors = new ArrayList<>();
        MaterialTags.ARMOR.all().forEach(m -> {
            armors.add(new MaterialArmor(domain, this, m, armorType, properties.get()));
        });
        return armors;
    }

    private Item.Properties prepareInstantiation(String domain) {
        if (domain.isEmpty()) Utils.onInvalidData("An GTArmorType was instantiated with an empty domain name!");
        Item.Properties properties = new Item.Properties();
        if (!repairable) properties.setNoRepair();
        return properties;
    }

    public GTArmorType setOverlayLayers(int overlayLayers) {
        this.overlayLayers = overlayLayers;
        return this;
    }

    public GTArmorType setEvent(SoundEvent event) {
        this.event = event;
        return this;
    }

    public GTArmorType setToolTip(Component... tooltip) {
        this.tooltip.addAll(Arrays.asList(tooltip));
        return this;
    }

    public GTArmorType setPrimaryRequirement(IMaterialTag tag) {
        if (tag == null)
            Utils.onInvalidData(StringUtils.capitalize(id) + " GTArmorType was set to have no primary material requirement even when it was explicitly called!");
        this.materialRequirement = tag;
        return this;
    }

    public ItemStack getToolStack(Material primary) {
        IGTArmor armor = GTAPI.get(IGTArmor.class, primary.getId() + "_" + id, domain);
        return Objects.requireNonNull(armor).asItemStack();
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public String getId() {
        return id;
    }

}
