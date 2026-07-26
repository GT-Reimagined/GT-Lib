package org.gtreimagined.gtlib.machine.types;

import brachy.modularui.drawable.UITexture;
import brachy.modularui.drawable.progress.CompositeProgress;
import brachy.modularui.value.sync.DoubleSyncValue;
import lombok.Getter;
import org.gtreimagined.gtlib.block.BlockBasic;
import org.gtreimagined.gtlib.blockentity.multi.BlockEntityBasicMultiMachine;
import org.gtreimagined.gtlib.cover.ICover;
import org.gtreimagined.gtlib.integration.recipeviewer.GTLibRecipeViewerPlugin;
import org.gtreimagined.gtlib.machine.BlockMultiMachine;
import org.gtreimagined.gtlib.machine.ITooltipArgs;
import org.gtreimagined.gtlib.machine.MachineState;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.mui.BarDir;
import org.gtreimagined.gtlib.mui.widgets.GTProgressWidget;
import org.gtreimagined.gtlib.structure.Pattern;
import org.gtreimagined.gtlib.structure.PatternBuilder;
import org.gtreimagined.gtlib.integration.ponder.PonderUtils;
import org.gtreimagined.gtlib.texture.Texture;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.gtreimagined.gtlib.util.int2;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.gtreimagined.gtlib.machine.MachineFlag.*;

public class BasicMultiMachine<T extends BasicMultiMachine<T>> extends Machine<T> {

    @Getter
    Function<Tier, BlockBasic> textureBlock;

    public BasicMultiMachine(String domain, String name) {
        super(domain, name);
        setTile(BlockEntityBasicMultiMachine::new);
        setBlock(BlockMultiMachine::new);
        setItemBlockClass(() -> BlockMultiMachine.class);
        addFlags(MULTI, COVERABLE);
        setClientTicking();
        setOutputCover(ICover.emptyFactory);
        addTooltipInfo((machine, stack, world, tooltip, flag) -> {
            if (machine.getType().getStructure(machine.getTier()) != null) {
                tooltip.add(Utils.translatable("machine.structure.form"));
            }
        });
        this.setBaseTexture((type, tier, state) -> type.getTiers().size() > 1 ? new Texture[]{new Texture(domain, "block/machine/base/" + type.getId() + "_" + tier.getId())} : new Texture[]{new Texture(domain, "block/machine/base/" + type.getId())});
     }

    @Override
    protected void setupGui() {
        super.setupGui();
        if (!(this instanceof MultiMachine)) {
            guiFunctions.add(((modularPanel, machine, guiData1, syncManager, settings) -> {
                if (has(RECIPE)) {
                    int2 size = guiProperties.getMachineData().getMachineStateSize();
                    modularPanel.child(new org.gtreimagined.gtlib.mui.widgets.MachineStateWidget(machine.getMachineTier(), this.has(RECIPE), machine::getMachineState,
                            guiProperties.getMachineData().getMachineStateTexture(machine.getMachineTier()))
                            .pos(guiProperties.getMachineData().getMachineStatePos().x, guiProperties.getMachineData().getMachineStatePos().y)
                            .size(size.x, size.y));

                    syncManager.syncValue("progress", new DoubleSyncValue(() -> machine.recipeHandler.map(r -> guiProperties.getMachineData().getProgressPercentFunction().apply(r.getCurrentProgress(), r.getMaxProgress())).orElse(0f)));
                    BarDir direction = guiProperties.getMachineData().getDir();
                    UITexture texture = guiProperties.getMachineData().getProgressTexture(machine.getMachineTier());
                    brachy.modularui.widgets.ProgressWidget progressWidget = new GTProgressWidget(machine.getMachineType(), machine.getMachineTier())
                            .syncHandler("progress")
                            .pos(guiProperties.getMachineData().getProgressPos().x + 6, guiProperties.getMachineData().getProgressPos().y + 6);
                    modularPanel.child(progressWidget);
                    if (!direction.isCircular()) {
                        progressWidget.texture(texture, direction.toRegularDirection());
                    } else {
                        progressWidget.progress(CompositeProgress.circularLike4Slice(
                                texture.getSubArea(0.0f, 0.0f, 1f, 0.5f),
                                texture.getSubArea(0f, 0.5f,1f, 1f),
                                direction.toCircularDirection()
                        ));
                    }
                }
            }));
        }
    }

    @Override
    public List<Texture> getTextures() {
        List<Texture> textures = super.getTextures();
        getTiers().forEach(t -> textures.addAll(Arrays.asList(getBaseTexture(t, MachineState.INVALID_STRUCTURE))));
        for (int i = 0; i < overlayLayers; i++) {
            int finalI = i;
            getTiers().forEach(t -> textures.addAll(Arrays.asList(getOverlayTextures(MachineState.INVALID_STRUCTURE, t, finalI))));
        }

        return textures;
    }

    public final void setStructurePattern(Function<PatternBuilder, Pattern> patterns) {
        setStructurePattern(patterns.apply(new PatternBuilder()));
    }
    
    public final void setStructurePattern(Pattern... patterns) {
        if (FMLEnvironment.dist.isClient()) {
            if (patterns.length == 0) return;
            GTLibRecipeViewerPlugin.registerPatternForJei(this, Arrays.stream(patterns).collect(Collectors.toList()));
            this.tiers.forEach(t -> {
                PonderUtils.registerMultiblock(this, t, Arrays.asList(patterns));
            });
        }
    }

    public final void setStructurePattern(Tier tier, Function<PatternBuilder, Pattern> patterns) {
        setStructurePattern(tier, patterns.apply(new PatternBuilder()));
    }

    public final void setStructurePattern(Tier tier,  Pattern... patterns) {
        if (FMLEnvironment.dist.isClient()) {
            if (patterns.length == 0) return;
            GTLibRecipeViewerPlugin.registerPatternForJei(this, tier, Arrays.stream(patterns).collect(Collectors.toList()));
            PonderUtils.registerMultiblock(this, tier, Arrays.asList(patterns));
        }
    }

    public T setTextureBlock(BlockBasic textureBlock){
        this.textureBlock = t -> textureBlock;
        return (T) this;
    }

    public T setTextureBlock(Function<Tier, BlockBasic> textureBlock){
        this.textureBlock = textureBlock;
        return (T) this;
    }

    public T addStructureTooltip(Tier tier, int tooltips){
        return addTooltipInfo((machine, stack, world, tooltip, flag) -> {
            if (machine.getTier() == tier){
                for (int i = 0; i < tooltips; i++) {
                    tooltip.add(Utils.translatable("tooltip." + getId() + "." + tier.getId() + "." + i));
                }
            }
        });
    }

    public T addStructureTooltip(int tooltips){
        return addTooltipInfo((machine, stack, world, tooltip, flag) -> {
            for (int i = 0; i < tooltips; i++) {
                tooltip.add(Utils.translatable("tooltip." + getId() + "." + i));
            }
        });
    }

    public T addStructureTooltip(int tooltips, ITooltipArgs args){
        return addTooltipInfo((machine, stack, world, tooltip, flag) -> {
            for (int i = 0; i < tooltips; i++) {
                tooltip.add(Utils.translatable("tooltip." + getId() + "." + i, args.getTooltipArgs(machine, stack, world, flag, i)));
            }
        });
    }
}
