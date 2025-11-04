package org.gtreimagined.gtlib.registration;

import net.minecraft.world.item.CreativeModeTab;

public interface ICreativeTabProvider {
    boolean isInTab(CreativeModeTab tab);
}
