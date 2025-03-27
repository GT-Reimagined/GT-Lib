package org.gtreimagined.gtlib.proxy;

import org.gtreimagined.gtlib.AntimatterAPI;
import org.gtreimagined.gtlib.ore.StoneType;
import org.gtreimagined.gtlib.worldgen.AntimatterConfiguredFeatures;
import org.gtreimagined.gtlib.worldgen.AntimatterWorldGenerator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class CommonHandler implements IProxyHandler {

    public CommonHandler() {
    }

    @SuppressWarnings("unused")
    public static void setup() {
        AntimatterConfiguredFeatures.init();
        AntimatterAPI.all(StoneType.class, StoneType::initSuppliedState);
        AntimatterWorldGenerator.setup();
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
