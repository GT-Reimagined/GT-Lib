package org.gtreimagined.gtlib.integration.recipeviewer;

import org.gtreimagined.gtlib.machine.types.BasicMultiMachine;
import org.gtreimagined.gtlib.structure.Pattern;

import java.util.List;

public record MultiMachineInfoPage(BasicMultiMachine<?> multiMachine, List<Pattern> patterns) {
}
