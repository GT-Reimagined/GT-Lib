package org.gtreimagined.gtlib.gui;

import brachy.modularui.api.drawable.IDrawable;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.gui.slot.ISlotProvider;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.mui.GTGuiTextures;
import org.gtreimagined.gtlib.registration.IGTObject;
import org.gtreimagined.gtlib.util.int2;
import org.gtreimagined.gtlib.util.int4;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

@Accessors(chain = true)
public class GuiProperties {

    @Getter
    protected ResourceLocation loc;

    @Accessors(fluent = true)
    @Getter
    private boolean hasGTIcon = true;
    @Getter
    @Setter
    private int2 gtIconPos = new int2(153, 64);
    private final Map<String, IDrawable> gtIcons = new Object2ObjectOpenHashMap<>();
    @Accessors(fluent = true)
    @Getter
    protected boolean enablePlayerSlots = true;
    @Getter
    protected int4 area = new int4(3, 3, 170, 80);

    @Getter
    protected MachineWidgetData machineData = new MachineWidgetData(this);

    @Setter
    private ISlotProvider<?> slots;
    @Getter
    private Map<String, String> themeMap = new HashMap<>();
    @Getter
    @Setter
    private int xSize = 176, ySize = 166;
    @Getter
    @Setter
    private boolean titleDrawingAllowed = true;

    public GuiProperties(String domain, String id) {
        this.loc = new ResourceLocation(domain, id);
        gtIcons.put("", GTGuiTextures.GT_LOGO);
    }

    public GuiProperties(IGTObject type) {
        this(type.getDomain(), type.getId());
    }

    public ISlotProvider<?> getSlots() {
        if (slots == null) throw new IllegalStateException("Called GuiData::getSlots without setting it first");
        return slots;
    }

    public String getTheme(Tier tier) {
        if (tier != null && themeMap.containsKey(tier.getId())) return themeMap.get(tier.getId());
        return themeMap.get("");
    }

    public GuiProperties setEnablePlayerSlots(boolean enablePlayerSlots) {
        this.enablePlayerSlots = enablePlayerSlots;
        return this;
    }

    public GuiProperties setHasGTIcon(boolean hasGTIcon) {
        this.hasGTIcon = hasGTIcon;
        return this;
    }

    public GuiProperties setGTIcon(IDrawable icon){
        gtIcons.put("", icon);
        return this;
    }

    public GuiProperties setGTIcon(Tier tier, IDrawable icon){
        gtIcons.put(tier.getId(), icon);
        return this;
    }

    public IDrawable getGTIcon(Tier tier){
        if (tier != null && gtIcons.containsKey(tier.getId())) return gtIcons.get(tier.getId());
        return gtIcons.get("");
    }

    public GuiProperties setArea(int x, int y, int z, int w) {
        area.set(x, y, z, w);
        return this;
    }

    public GuiProperties setTheme(String theme){
        this.themeMap.put("", theme);
        return this;
    }

    public GuiProperties setTheme(Tier tier, String theme){
        this.themeMap.put(tier.getId(), theme);
        return this;
    }
}
