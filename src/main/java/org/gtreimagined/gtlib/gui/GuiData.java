package org.gtreimagined.gtlib.gui;

import brachy.modularui.api.drawable.IDrawable;
import com.google.common.collect.ImmutableMap;
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
public class GuiData {

    @Getter
    protected ResourceLocation loc;

    @Getter
    protected MenuHandler<?> menuHandler;

    protected Map<String, ResourceLocation> backgroundTextures = new Object2ObjectOpenHashMap<>();

    @Accessors(fluent = true)
    @Getter
    private boolean hasGTIcon = true;
    @Getter
    @Setter
    private int2 gtIconPos = new int2(153, 64);
    @Getter
    @Setter
    private IDrawable gtIcon = GTGuiTextures.GT_LOGO;
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
    @Deprecated
    private int playerYOffset = 0, playerXOffset = 0;
    @Getter
    @Setter
    private int xSize = 176, ySize = 166;
    @Getter
    @Setter
    @Deprecated
    private int textureXSize = 256, textureYSize = 256;

    @Getter
    @Setter
    private boolean titleDrawingAllowed = true;

    public GuiData(String domain, String id) {
        this.loc = new ResourceLocation(domain, id);
        this.backgroundTextures.put("", new ResourceLocation(Ref.ID, "textures/gui/background/machine_basic.png"));
    }

    public GuiData(String domain, String id, MenuHandler menuHandler) {
        this(domain, id);
        this.menuHandler = menuHandler;
    }

    public GuiData(IGTObject type, MenuHandler menuHandler) {
        this(type.getDomain(), type.getId());
        this.menuHandler = menuHandler;
    }

    public ISlotProvider<?> getSlots() {
        if (slots == null) throw new IllegalStateException("Called GuiData::getSlots without setting it first");
        return slots;
    }

    public ResourceLocation getTexture(Tier tier, String type) {
       if (backgroundTextures.containsKey(tier.getId())) return backgroundTextures.get(tier.getId());
       return backgroundTextures.get("");
    }

    public boolean enablePlayerSlots() {
        return enablePlayerSlots;
    }

    public GuiData setEnablePlayerSlots(boolean enablePlayerSlots) {
        this.enablePlayerSlots = enablePlayerSlots;
        return this;
    }

    public GuiData setHasGTIcon(boolean hasGTIcon) {
        this.hasGTIcon = hasGTIcon;
        return this;
    }

    public GuiData setArea(int x, int y, int z, int w) {
        area.set(x, y, z, w);
        return this;
    }

    @Deprecated
    public GuiData setBackgroundTexture(String textureName){
        this.backgroundTextures.put("", new ResourceLocation(loc.getNamespace(), "textures/gui/background/" + textureName + ".png"));
        return this;
    }
    @Deprecated
    public GuiData setBackgroundTexture(Tier tier, String textureName){
        this.backgroundTextures.put(tier.getId(), new ResourceLocation(loc.getNamespace(), "textures/gui/background/" + textureName + ".png"));
        return this;
    }
    @Deprecated
    public GuiData setBackgroundTexture(ResourceLocation textureName){
        this.backgroundTextures.put("", new ResourceLocation(textureName.getNamespace(), "textures/gui/background/" + textureName.getPath() + ".png"));
        return this;
    }
    @Deprecated
    public GuiData setBackgroundTexture(Tier tier, ResourceLocation textureName){
        this.backgroundTextures.put(tier.getId(), new ResourceLocation(textureName.getNamespace(), "textures/gui/background/" + textureName.getPath() + ".png"));
        return this;
    }
}
