package org.gtreimagined.gtlib.integration.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RegisterRecipeHandlersEvent;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.event.MaterialEvent;
import net.minecraft.resources.ResourceLocation;

public class GTLibKubeJS extends KubeJSPlugin {

    @Override
    public void addBindings(BindingsEvent event) {
        event.add(Ref.ID, new KubeJSBindings());
    }

    @Override
    public void addRecipes(RegisterRecipeHandlersEvent event) {
        event.register(new ResourceLocation(Ref.ID, "machine"), KubeJSRecipe::new);
    }

    public static void loadStartup(){
        GTCreationEvent.init();
        new GTCreationEvent().post(ScriptType.STARTUP, "gtlib.creation");
    }

    public static void loadMaterialEvent(MaterialEvent event){
        new GTMaterialEvent(event).post(ScriptType.STARTUP, "gtlib.material_event");
    }
}
