package org.gtreimagined.gtlib.gui;

import brachy.modularui.drawable.UITexture;
import brachy.modularui.drawable.progress.ProgressDrawable.Direction;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.mui.BarDir;
import org.gtreimagined.gtlib.mui.GTGuiTextures;
import org.gtreimagined.gtlib.util.int2;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.function.BiFunction;

@Accessors(chain = true)
public class MachineWidgetData {
    @Getter
    @Setter
    public BarDir dir = BarDir.RIGHT;
    @Getter
    @Setter
    public boolean barFill = true;
    @Getter
    protected int2 progressSize = new int2(20, 18), progressPos = new int2(72, 18);
    @Getter
    @Setter
    protected BiFunction<Integer, Integer, Float> progressPercentFunction = (progress, maxProgress) -> (float)progress / (float)maxProgress;
    @Getter
    protected int2 ioPos = new int2(7, 62), machineStatePos = new int2(83, 43), machineStateSize = new int2(10, 11);
    protected Map<String, UITexture> machineStateTextures = new Object2ObjectOpenHashMap<>();
    protected Map<String, UITexture> progressTextures = new Object2ObjectOpenHashMap<>();

    @Getter
    private final GuiProperties parent;
    public MachineWidgetData(GuiProperties parent){
        this.parent = parent;
        this.machineStateTextures.put("", GTGuiTextures.MACHINE_STATE);
        this.progressTextures.put("", GTGuiTextures.DEFAULT_PROGRESS);
    }

    @Deprecated
    public MachineWidgetData setProgressLocation(String name){
        this.progressTextures.put("", UITexture.builder().location(new ResourceLocation(parent.loc.getNamespace(), "textures/gui/progress_bars/" + name + ".png")).imageSize(20, 36).build());
        return this;
    }

    @Deprecated
    public MachineWidgetData setProgressLocation(Tier tier, String name){
        this.progressTextures.put(tier.getId(), UITexture.builder().location(new ResourceLocation(parent.loc.getNamespace(), "textures/gui/progress_bars/" + name + ".png")).imageSize(20, 36).build());
        return this;
    }

    public MachineWidgetData setProgressLocation(UITexture texture){
        this.progressTextures.put("", texture);
        return this;
    }

    public MachineWidgetData setProgressLocation(Tier tier, UITexture texture){
        this.progressTextures.put(tier.getId(), texture);
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

    public MachineWidgetData setMachineStateLocation(UITexture texture){
        this.progressTextures.put("", texture);
        return this;
    }

    public MachineWidgetData setMachineStateLocation(Tier tier, UITexture texture){
        this.progressTextures.put(tier.getId(), texture);
        return this;
    }

    public UITexture getProgressTexture(Tier tier) {
        if (tier != null && progressTextures.containsKey(tier.getId())) return progressTextures.get(tier.getId());
        return progressTextures.get("");
    }

    public boolean doesBarFill() {
        return barFill;
    }

    public UITexture getMachineStateTexture(Tier tier) {
        if (tier != null && machineStateTextures.containsKey(tier.getId())) return machineStateTextures.get(tier.getId());
        return machineStateTextures.get("");
    }

}
