package muramasa.antimatter.machine;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import muramasa.antimatter.machine.types.Machine;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum MachineFlag {

    BASIC, //
    STEAM,
    MULTI, //Has structure
    HATCH,
    ITEM, //Can store items
    CELL,
    FLUID,
    EU, //Needs power
    RF, //Uses RF instead of EU
    HEAT,
    RECIPE, //Has a recipe map
    GUI,
    GENERATOR, //Has a recipe map and converts applicable recipes to power.
    COVERABLE,
    PARTIAL_AMPS,
    UNCULLED;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
