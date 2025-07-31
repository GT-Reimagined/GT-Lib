package org.gtreimagined.gtlib.ore;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.block.BlockStone;
import org.gtreimagined.gtlib.block.BlockStoneSlab;
import org.gtreimagined.gtlib.block.BlockStoneStair;
import org.gtreimagined.gtlib.block.BlockStoneWall;
import org.gtreimagined.gtlib.cover.CoverFactory;
import org.gtreimagined.gtlib.cover.CoverStone;
import org.gtreimagined.gtlib.item.ItemStoneCover;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.registration.ITextureProvider;
import org.gtreimagined.gtlib.texture.Texture;
import org.gtreimagined.gtlib.util.RegistryUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

public class VanillaStoneType extends CobbleStoneType{
    private final Texture vanillaTexture;
    public VanillaStoneType(String domain, String id, Material material, String beginningPath, Texture vanillaTexture, SoundType soundType, boolean generateBlock) {
        super(domain, id, material, beginningPath, soundType, generateBlock);
        this.vanillaTexture = vanillaTexture;
    }

    @Override
    public void onRegistryBuild(ResourceKey<? extends Registry<?>> registry) {
        if (registry == ForgeRegistries.Keys.BLOCKS) {
            for (int i = 0; i < SUFFIXES.length; i++) {
                int i2 = i - 2;
                Block stone, stair = null, slab = null, wall = null;
                ITextureProvider stoneTextureProvider;
                if (i == 7) {
                    stone = this.getState().getBlock();
                    stoneTextureProvider = this::getTextures;
                    slab = RegistryUtils.getBlockFromId("minecraft", this.getId() + "_slab");
                    stair = RegistryUtils.getBlockFromId("minecraft", this.getId() + "_stairs");
                    wall = RegistryUtils.getBlockFromId("minecraft", this.getId() + "_wall");
                } else if (i == 6){
                    stone = RegistryUtils.getBlockFromId("minecraft", "polished_" + this.getId());
                    slab = RegistryUtils.getBlockFromId("minecraft", "polished_" + this.getId() + "_slab");
                    stair = RegistryUtils.getBlockFromId("minecraft", "polished_" + this.getId() + "_stairs");
                    wall = new BlockStoneWall(this, SUFFIXES[i]);
                    stoneTextureProvider = () -> new Texture[]{new Texture("block/polished_" + getId())};
                } else {
                    BlockStone stone1 = new BlockStone(this, SUFFIXES[i]);
                    stone = stone1;
                    stoneTextureProvider = stone1;
                    if (i >= 2) {
                        slab = new BlockStoneSlab(this, SUFFIXES[i]);
                        stair = new BlockStoneStair(this, SUFFIXES[i], stone);
                        wall = new BlockStoneWall(this, SUFFIXES[i]);
                    }
                }
                String id = i == 7 ? getId() : getId() + "_" + SUFFIXES[i];
                String suffix = i == 7 ? "" : SUFFIXES[i];
                CoverFactory.builder(CoverStone::new).item((coverFactory, tier) ->
                        new ItemStoneCover(Ref.SHARED_ID, getId(), suffix, stoneTextureProvider)).addTextures(stoneTextureProvider.getTextures()).build(Ref.SHARED_ID, id + "_cover");
                blocks.put(SUFFIXES[i], stone);
                if (i < 2){
                    continue;
                }
                blocks.put(SLAB_SUFFIXES[i2], slab);
                blocks.put(STAIR_SUFFIXES[i2], stair);
                blocks.put(WALL_SUFFIXES[i2], wall);
            }
        }
    }

    @Override
    public Texture getTexture() {
        return vanillaTexture;
    }
}
