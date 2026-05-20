package org.gtreimagined.gtlib.machine.types;

import org.gtreimagined.gtlib.Data;
import org.gtreimagined.gtlib.blockentity.multi.BlockEntityMultiMachine;
import org.gtreimagined.gtlib.cover.CoverFactory;
import org.gtreimagined.gtlib.cover.ICover;
import org.gtreimagined.gtlib.gui.widget.WidgetSupplier;
import org.gtreimagined.gtlib.integration.xei.renderer.IInfoRenderer;
import org.gtreimagined.gtlib.mui.widgets.GTInfoRenderWidget;

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
