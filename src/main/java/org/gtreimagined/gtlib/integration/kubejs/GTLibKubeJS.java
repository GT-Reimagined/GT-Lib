package org.gtreimagined.gtlib.integration.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.event.MaterialEvent;
import net.minecraft.resources.ResourceLocation;

public class GTLibKubeJS extends KubeJSPlugin {

    private static final EventGroup GT_LIB = EventGroup.of(Ref.ID);
    public static final EventHandler CREATION = GT_LIB.startup("creation", () -> GTCreationEvent.class);
    public static final EventHandler MATERIAL = GT_LIB.startup("material", () -> GTMaterialEvent.class);
    public static final EventHandler WORLDGEN = GT_LIB.server("worldgen", () -> GTWorldEvent.class);
    public static final EventHandler RECIPE_LOADER = GT_LIB.server("recipes", () -> RecipeLoaderEventKubeJS.class);

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add(Ref.ID, new KubeJSBindings());
    }

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
       event.register(new ResourceLocation(Ref.ID, "machine"), MachineRecipeSchema.SCHEMA);
    }

    public static void loadStartup(){
        GTCreationEvent.init();
        CREATION.post(new GTCreationEvent());
    }

    public static void loadMaterialEvent(MaterialEvent event){
        MATERIAL.post(new GTMaterialEvent(event));
    }
}
