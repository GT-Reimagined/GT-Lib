package org.gtreimagined.gtlib.structure;

import org.gtreimagined.gtlib.capability.IComponentHandler;

import java.util.Optional;

public interface IComponent {

    Optional<? extends IComponentHandler> getComponentHandler();
}
