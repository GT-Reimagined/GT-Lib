package org.gtreimagined.gtlib.gui.container;

import org.gtreimagined.gtlib.gui.GuiInstance;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

public interface IGTContainer {
    GuiInstance source();
    Set<ServerPlayer> listeners();
}
