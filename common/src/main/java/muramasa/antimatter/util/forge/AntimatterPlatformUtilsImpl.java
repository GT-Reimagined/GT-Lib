package muramasa.antimatter.util.forge;

import muramasa.antimatter.Antimatter;
import muramasa.antimatter.event.CraftingEvent;
import muramasa.antimatter.event.ProvidersEvent;
import muramasa.antimatter.event.WorldGenEvent;
import muramasa.antimatter.event.forge.AntimatterCraftingEvent;
import muramasa.antimatter.event.forge.AntimatterLoaderEvent;
import muramasa.antimatter.event.forge.AntimatterProvidersEvent;
import muramasa.antimatter.event.forge.AntimatterWorldGenEvent;
import muramasa.antimatter.machine.types.BasicMultiMachine;
import muramasa.antimatter.recipe.loader.IRecipeRegistrate;
import muramasa.antimatter.registration.IAntimatterRegistrar;
import muramasa.antimatter.registration.Side;
import muramasa.antimatter.structure.Pattern;
import muramasa.antimatter.util.AntimatterPlatformUtils;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoader;

import java.util.List;

public class AntimatterPlatformUtilsImpl implements AntimatterPlatformUtils {

    @Override
    public CraftingEvent postCraftingEvent(IAntimatterRegistrar registrar){
        CraftingEvent event = new CraftingEvent();
        AntimatterCraftingEvent ev = new AntimatterCraftingEvent(registrar, event);
        ModLoader.get().postEvent(ev);
        return event;
    }

    @Override
    public void postLoaderEvent(IAntimatterRegistrar registrar, IRecipeRegistrate reg){
        MinecraftForge.EVENT_BUS.post(new AntimatterLoaderEvent(registrar, reg));
    }

    @Override
    public ProvidersEvent postProviderEvent(Side side, IAntimatterRegistrar registrar){
        ProvidersEvent providerEvent = new ProvidersEvent(side);
        AntimatterProvidersEvent ev = new AntimatterProvidersEvent(providerEvent, registrar);
        ModLoader.get().postEvent(ev);
        //boolean bool = MinecraftForge.EVENT_BUS.post(ev);
        return providerEvent;
    }

    @Override
    public WorldGenEvent postWorldEvent(IAntimatterRegistrar registrar){
        WorldGenEvent event = new WorldGenEvent();
        AntimatterWorldGenEvent ev = new AntimatterWorldGenEvent(Antimatter.INSTANCE, event);
        MinecraftForge.EVENT_BUS.post(ev);
        return event;
    }

    @Override
    public void addMultiMachineInfo(BasicMultiMachine<?> machine, List<Pattern> patterns){
        /*if (AntimatterAPI.isModLoaded(Ref.MOD_JEI)){
            MultiMachineInfoCategory.addMultiMachine(new MultiMachineInfoPage(machine, patterns));
        }*/
    }

    @Override
    public Item.Properties getToolProperties(CreativeModeTab group, boolean repairable){
        Item.Properties properties = new Item.Properties().tab(group);
        if (!repairable) properties.setNoRepair();
        return properties;
    }
}
