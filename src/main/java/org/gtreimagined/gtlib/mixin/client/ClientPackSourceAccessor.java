package org.gtreimagined.gtlib.mixin.client;

import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPackSource.class)
public interface ClientPackSourceAccessor {
    @Accessor
    static PackMetadataSection getVERSION_METADATA_SECTION()  {
        throw new AssertionError();
    }
}
