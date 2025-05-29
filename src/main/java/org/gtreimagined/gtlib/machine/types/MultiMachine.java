package org.gtreimagined.gtlib.machine.types;

import org.gtreimagined.gtlib.Data;
import org.gtreimagined.gtlib.blockentity.multi.BlockEntityMultiMachine;
import org.gtreimagined.gtlib.cover.CoverFactory;
import org.gtreimagined.gtlib.cover.ICover;
import org.gtreimagined.gtlib.gui.widget.WidgetSupplier;

public class MultiMachine extends BasicMultiMachine<MultiMachine> {

    public MultiMachine(String domain, String name) {
        super(domain, name);
        setTile(BlockEntityMultiMachine::new);
        setGUI(Data.MULTI_MENU_HANDLER);
        setOutputCover(ICover.emptyFactory);
    }

    @Override
    protected void setupGui() {
        super.setupGui();
        addGuiCallback(t -> {
            BlockEntityMultiMachine<?> machine = (BlockEntityMultiMachine<?>) t.handler;
            WidgetSupplier wid = machine.getInfoWidget();
            if (wid != null) t.addWidget(wid);
        });
    }
}
