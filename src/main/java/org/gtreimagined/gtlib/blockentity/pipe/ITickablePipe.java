package org.gtreimagined.gtlib.blockentity.pipe;

import org.gtreimagined.gtlib.capability.CoverHandler;
import org.gtreimagined.gtlib.capability.pipe.PipeCoverHandler;

import java.util.Optional;

public interface ITickablePipe {
    Optional<PipeCoverHandler<?>> getCoverHandler();

    default void tick() {
        getCoverHandler().ifPresent(CoverHandler::onUpdate);
    }

}
