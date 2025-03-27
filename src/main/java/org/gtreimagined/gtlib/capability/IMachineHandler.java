package org.gtreimagined.gtlib.capability;

import org.gtreimagined.gtlib.machine.event.IMachineEvent;

public interface IMachineHandler {

    default void init() {

    }

    default void onMachineEvent(IMachineEvent event, Object... data) {
        //NOOP
    }
}
