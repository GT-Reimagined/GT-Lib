package org.gtreimagined.gtlib;

import org.gtreimagined.gtlib.client.dynamic.DynamicTexturer;
import org.gtreimagined.gtlib.cover.ICover;
import org.gtreimagined.gtlib.machine.MachineState;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.machine.types.Machine;
import org.gtreimagined.gtlib.texture.Texture;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class GTLibProperties {
    public static class MachineProperties {
        public final ICover[] covers;
        public final MachineState state;
        public final Function<Direction, Texture> machTexture;
        public final Machine<?> type;
        public final Tier tier;
        @Nullable
        public final Function<Direction, DynamicTexturer<ICover, ICover.DynamicKey>> coverTexturer;
        /**
         * @param covers
         * @param state
         * @param machTexture
         * @param coverTexturer
         */
        public MachineProperties(Machine<?> type, Tier tier, ICover[] covers, MachineState state, Function<Direction, Texture> machTexture,
                Function<Direction, DynamicTexturer<ICover, ICover.DynamicKey>> coverTexturer) {
            this.covers = covers;
            this.state = state;
            this.machTexture = machTexture;
            this.tier = tier;
            this.coverTexturer = coverTexturer;
            this.type = type;
        }
    }
}
