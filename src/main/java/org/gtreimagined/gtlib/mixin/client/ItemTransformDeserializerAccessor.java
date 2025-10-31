package org.gtreimagined.gtlib.mixin.client;

import net.minecraft.client.renderer.block.model.ItemTransform;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemTransform.Deserializer.class)
public interface ItemTransformDeserializerAccessor {

    @Accessor
    static Vector3f getDEFAULT_ROTATION(){
        throw new AssertionError();
    }

    @Accessor
    static Vector3f getDEFAULT_TRANSLATION(){
        throw new AssertionError();
    }

    @Accessor
    static Vector3f getDEFAULT_SCALE(){
        throw new AssertionError();
    }
}
