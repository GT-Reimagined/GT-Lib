package org.gtreimagined.gtlib.proxy;

import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.ore.StoneType;
import org.gtreimagined.gtlib.worldgen.GTLibConfiguredFeatures;
import org.gtreimagined.gtlib.worldgen.GTLibWorldGenerator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class CommonHandler implements IProxyHandler {

    public CommonHandler() {
    }

    @SuppressWarnings("unused")
    public static void setup() {
        GTLibConfiguredFeatures.init();
        GTAPI.all(StoneType.class, StoneType::initSuppliedState);
        GTLibWorldGenerator.setup();
    }

    @Override
    public Level getClientWorld() {
        return null;
    }

    @Override
    public Player getClientPlayer() {
        return null;
    }
}
