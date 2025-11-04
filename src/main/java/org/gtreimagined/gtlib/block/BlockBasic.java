package org.gtreimagined.gtlib.block;

import net.minecraft.world.level.material.MapColor;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.registration.IGTObject;
import org.gtreimagined.gtlib.registration.IModelProvider;
import org.gtreimagined.gtlib.registration.ISharedGTObject;
import org.gtreimagined.gtlib.registration.ITextureProvider;
import org.gtreimagined.gtlib.texture.Texture;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

public class BlockBasic extends Block implements IGTObject, ITextureProvider, IModelProvider {

    protected final String domain, id;

    public BlockBasic(String domain, String id, Properties properties) {
        super(properties);
        this.domain = domain;
        this.id = id;
        GTAPI.register(getClass(), this);
    }

    public BlockBasic(String domain, String id) {
        this(domain, id, Properties.of().mapColor(MapColor.METAL).strength(1.0f, 1.0f).sound(SoundType.STONE));
    }

    public String getDomain() {
        return this instanceof ISharedGTObject ? Ref.SHARED_ID : domain;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Texture[] getTextures() {
        return new Texture[0];
    }

}
