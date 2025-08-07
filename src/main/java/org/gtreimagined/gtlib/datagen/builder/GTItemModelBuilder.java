package org.gtreimagined.gtlib.datagen.builder;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.texture.Texture;
import org.gtreimagined.gtlib.util.FluidUtils;
import org.gtreimagined.gtlib.util.RegistryUtils;
import net.devtech.arrp.json.loot.JCondition;
import net.devtech.arrp.json.models.JOverride;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class GTItemModelBuilder extends GTModelBuilder<GTItemModelBuilder> {

    protected ResourceLocation loader;

    public GTItemModelBuilder(ResourceLocation outputLocation) {
        super(outputLocation);
    }

    public GTItemModelBuilder property(String property, Object element) {
        model.property(property, element);
        return this;
    }

    public GTItemModelBuilder property(String property, String value) {
        model.property(property, value);
        return this;
    }

    public GTItemModelBuilder property(String property, boolean value) {
        model.property(property, value);
        return this;
    }

    public GTItemModelBuilder bucketLoader() {
        super.loader(new ResourceLocation("forge", "bucket"));
        return this;
    }

    public GTItemModelBuilder cell() {
        super.loader(new ResourceLocation(Ref.ID, "cell"));
        return this;
    }

    public GTItemModelBuilder bucketProperties(Fluid fluid) {
        return bucketProperties(fluid, true, FluidUtils.isFluidGaseous(fluid));
    }

    public GTItemModelBuilder bucketProperties(Fluid fluid, boolean tint, boolean islighter) {
        property("fluid", RegistryUtils.getIdFromFluid(fluid).toString());
        property("flip_gas", islighter);
        property("applyTint", tint);
        // property("coverIsMask", false);
        return bucketLoader();
    }

    public GTItemModelBuilder tex(Consumer<Map<String, String>> texer) {
        Map<String, String> textureMap = new Object2ObjectArrayMap<>();
        texer.accept(textureMap);
        textureMap.forEach(this::texture);
        return this;
    }

    public GTItemModelBuilder tex(Map<String, Texture> textureMap) {
        textureMap.forEach(this::texture);
        return this;
    }

    public OverrideBuilder override(){
        return new OverrideBuilder();
    }

    public class OverrideBuilder {

        private ResourceLocation model = null;
        private final Map<ResourceLocation, Float> predicates = new LinkedHashMap<>();

        public OverrideBuilder model(IModelLocation model) {
            this.model = model.getLocation();
            return this;
        }

        public OverrideBuilder model(ResourceLocation model) {
            this.model = model;
            return this;
        }

        public OverrideBuilder predicate(ResourceLocation key, float value) {
            this.predicates.put(key, value);
            return this;
        }

        public GTItemModelBuilder end() {
            JCondition condition = new JCondition();
            predicates.forEach((k, v) -> condition.parameter(k.toString(), v));
            GTItemModelBuilder.this.model.addOverride(new JOverride(condition, model.toString()));
            return GTItemModelBuilder.this;
        }
    }
}
