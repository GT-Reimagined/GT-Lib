package org.gtreimagined.gtlib.machine.types;

import brachy.modularui.drawable.UITexture;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.gtreimagined.gtlib.blockentity.multi.BlockEntityMultiMachine;
import org.gtreimagined.gtlib.cover.ICover;
import org.gtreimagined.gtlib.machine.IPanelFunction;
import org.gtreimagined.gtlib.mui.IInfoRenderer;
import org.gtreimagined.gtlib.mui.widgets.GTInfoRenderWidget;

@Accessors(chain = true)
public class MultiMachine extends BasicMultiMachine<MultiMachine> {


    public MultiMachine(String domain, String name) {
        super(domain, name);
        setTile(BlockEntityMultiMachine::new);
        setOutputCover(ICover.emptyFactory);
    }

    @Override
    protected void setupGui() {
        super.setupGui();
        guiFunctions.add(((modularPanel, machine, guiData, syncManager, settings) -> {
            if (machine instanceof IInfoRenderer renderer){
                renderer.registerSyncHandlers(syncManager);
                modularPanel.child(new GTInfoRenderWidget(renderer)
                        .pos(renderer.getPos().x, renderer.getPos().y)
                        .size(renderer.getSize().x, renderer.getSize().y));
            }
        }));
    }
}
