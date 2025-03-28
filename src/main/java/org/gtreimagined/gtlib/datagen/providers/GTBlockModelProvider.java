package org.gtreimagined.gtlib.datagen.providers;

import org.gtreimagined.gtlib.datagen.builder.GTBlockModelBuilder;
import net.minecraft.MethodsReturnNonnullByDefault;

@MethodsReturnNonnullByDefault
public class GTBlockModelProvider extends GTModelProvider<GTBlockModelBuilder> {

    private final String name;

    public GTBlockModelProvider(String modid, String name) {
        super(modid, BLOCK_FOLDER, GTBlockModelBuilder::new);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
