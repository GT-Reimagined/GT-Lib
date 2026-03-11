package org.gtreimagined.gtlib.gui;

import brachy.modularui.widgets.ProgressWidget.Direction;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.util.int2;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

@Accessors(chain = true)
public class MachineWidgetData {
    @Getter
    @Setter
    @Deprecated
    public BarDir dir = BarDir.LEFT;
    @Getter
    @Setter
    public Direction direction = Direction.LEFT;
    @Getter
    @Setter
    public boolean barFill = true;
    @Getter
    protected int2 progressSize = new int2(20, 18), progressPos = new int2(72, 18);
    @Getter
    protected int2 ioPos = new int2(7, 62), machineStatePos = new int2(83, 43), machineStateSize = new int2(10, 11);
    protected Map<String, ResourceLocation> machineStateTextures = new Object2ObjectOpenHashMap<>();
    protected Map<String, ResourceLocation> progressTextures = new Object2ObjectOpenHashMap<>();

    @Getter
    private final GuiData parent;
    public MachineWidgetData(GuiData parent){
        this.parent = parent;
        this.machineStateTextures.put("", new ResourceLocation(Ref.ID, "textures/gui/widgets/machine_state.png"));
        this.progressTextures.put("", new ResourceLocation(Ref.ID, "textures/gui/progress_bars/default.png"));
    }

    public MachineWidgetData setProgressLocation(String name){
        this.progressTextures.put("", new ResourceLocation(parent.loc.getNamespace(), "textures/gui/progress_bars/" + name + ".png"));
        return this;
    }

    public MachineWidgetData setProgressLocation(Tier tier, String name){
        this.progressTextures.put(tier.getId(), new ResourceLocation(parent.loc.getNamespace(), "textures/gui/progress_bars/" + name + ".png"));
        return this;
    }

    public MachineWidgetData setProgressSize(int width, int height){
        this.progressSize = new int2(width, height);
        return this;
    }

    public MachineWidgetData setProgressPos(int x, int y){
        this.progressPos = new int2(x, y);
        return this;
    }

    public MachineWidgetData setIoPos(int x, int y){
        this.ioPos = new int2(x, y);
        return this;
    }

    public MachineWidgetData setMachineStatePos(int x, int y){
        this.machineStatePos = new int2(x, y);
        return this;
    }

    public MachineWidgetData setMachineStateSize(int width, int height){
        this.machineStateSize = new int2(width, height);
        return this;
    }

    public MachineWidgetData setMachineStateLocation(String name){
        this.progressTextures.put("", new ResourceLocation(parent.loc.getNamespace(), "textures/gui/button/" + name + ".png"));
        return this;
    }

    public MachineWidgetData setMachineStateLocation(Tier tier, String name){
        this.progressTextures.put(tier.getId(), new ResourceLocation(parent.loc.getNamespace(), "textures/gui/button/" + name + ".png"));
        return this;
    }

    public ResourceLocation getProgressTexture(Tier tier) {
        if (tier != null && progressTextures.containsKey(tier.getId())) return progressTextures.get(tier.getId());
        return progressTextures.get("");
    }

    public boolean doesBarFill() {
        return barFill;
    }

    public ResourceLocation getMachineStateTexture(Tier tier) {
        if (tier != null && machineStateTextures.containsKey(tier.getId())) return machineStateTextures.get(tier.getId());
        return machineStateTextures.get("");
    }

}
