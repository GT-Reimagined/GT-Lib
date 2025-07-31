package org.gtreimagined.gtlib.machine.types;

import lombok.Getter;
import org.gtreimagined.gtlib.Data;
import org.gtreimagined.gtlib.block.BlockBasic;
import org.gtreimagined.gtlib.blockentity.multi.BlockEntityBasicMultiMachine;
import org.gtreimagined.gtlib.cover.ICover;
import org.gtreimagined.gtlib.gui.widget.ProgressWidget;
import org.gtreimagined.gtlib.integration.xei.GTLibXEIPlugin;
import org.gtreimagined.gtlib.machine.BlockMultiMachine;
import org.gtreimagined.gtlib.machine.ITooltipArgs;
import org.gtreimagined.gtlib.machine.MachineState;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.structure.Pattern;
import org.gtreimagined.gtlib.structure.PatternBuilder;
import org.gtreimagined.gtlib.integration.ponder.PonderUtils;
import org.gtreimagined.gtlib.texture.Texture;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraftforge.fml.loading.FMLEnvironment;

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
        setGUI(Data.BASIC_MENU_HANDLER);
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
            addGuiCallback(t -> {
                if (this.has(RECIPE)) {
                    t.addWidget(ProgressWidget.build());
                }
            });
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
            GTLibXEIPlugin.registerPatternForJei(this, Arrays.stream(patterns).collect(Collectors.toList()));
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
            GTLibXEIPlugin.registerPatternForJei(this, tier, Arrays.stream(patterns).collect(Collectors.toList()));
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
