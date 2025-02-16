package muramasa.antimatter.util.forge;

import muramasa.antimatter.machine.types.BasicMultiMachine;
import muramasa.antimatter.structure.Pattern;
import muramasa.antimatter.util.AntimatterPlatformUtils;

import java.util.List;

public class AntimatterPlatformUtilsImpl implements AntimatterPlatformUtils {

    @Override
    public void addMultiMachineInfo(BasicMultiMachine<?> machine, List<Pattern> patterns){
        /*if (AntimatterAPI.isModLoaded(Ref.MOD_JEI)){
            MultiMachineInfoCategory.addMultiMachine(new MultiMachineInfoPage(machine, patterns));
        }*/
    }
}
