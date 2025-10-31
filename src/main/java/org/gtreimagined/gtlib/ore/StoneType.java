package org.gtreimagined.gtlib.ore;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.block.BlockStone;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.registration.IRegistryEntryProvider;
import org.gtreimagined.gtlib.registration.ISharedGTObject;
import org.gtreimagined.gtlib.texture.Texture;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class StoneType implements ISharedGTObject, IRegistryEntryProvider {

    @Getter
    private final String domain, id;
    //private int harvestLevel;
    private boolean gravity, requiresTool;
    public boolean generateBlock, generateOre = true;
    @Getter
    private final Material material;
    private Texture[] textures;
    protected Texture defaultTexture;
    @Getter
    private final SoundType soundType;
    @Getter
    private BlockState state;
    private Supplier<BlockState> stateSupplier;
    @Getter
    private int harvestLevel, fallingDustColor;
    @Getter
    private float hardness, resistence;
    @Getter
    private TagKey<Block> toolType;
    @Getter
    private boolean sandLike = false;

    public StoneType(String domain, String id, Material material, Texture texture, SoundType soundType, boolean generateBlock) {
        this.domain = domain;
        this.id = id;
        this.material = material;
        this.defaultTexture = texture;
        this.soundType = soundType;
        this.generateBlock = generateBlock;
        this.gravity = false;
        this.harvestLevel = 0;
        this.requiresTool = true;
        this.hardness = 1.5F;
        this.resistence = 6.0F;
        this.toolType = BlockTags.MINEABLE_WITH_PICKAXE;
        this.fallingDustColor = -16777216;
    }

    public StoneType setHarvestLevel(int harvestLevel) {
        this.harvestLevel = harvestLevel;
        return this;
    }

    public StoneType setHardnessAndResistance(float hardnessAndResistence) {
        this.hardness = hardnessAndResistence;
        this.resistence = hardnessAndResistence;
        return this;
    }

    public StoneType setHardnessAndResistance(float hardness, float resistence) {
        this.hardness = hardness;
        this.resistence = resistence;
        return this;
    }

    public StoneType setGravity(boolean gravity) {
        this.gravity = gravity;
        return this;
    }

    public StoneType setRequiresTool(boolean requiresTool) {
        this.requiresTool = requiresTool;
        return this;
    }

    public StoneType setFallingDustColor(int fallingDustColor) {
        this.fallingDustColor = fallingDustColor;
        return this;
    }

    public StoneType setSandLike(boolean sandLike) {
        if (sandLike) this.setHardnessAndResistance(0.5F).setRequiresTool(false).setType(BlockTags.MINEABLE_WITH_SHOVEL).setGravity(true);
        this.sandLike = sandLike;
        return this;
    }

    public StoneType setGenerateOre(boolean generateOre){
        this.generateOre = generateOre;
        return this;
    }

    public StoneType setType(TagKey<Block> type) {
        this.toolType = type;
        return this;
    }

    public StoneType setTextures(Texture... textures){
        this.textures = textures;
        return this;
    }

    @Override
    public void onRegistryBuild(ResourceKey<? extends Registry<?>> registry) {
        if (generateBlock && registry == ForgeRegistries.Keys.BLOCKS) setState(new BlockStone(this));
    }

    @Override
    public String getId() {
        return id;
    }

    public Texture getTexture() {
        return defaultTexture;
    }

    public Texture[] getTextures() {
        return textures != null ? textures : new Texture[]{getTexture()};
    }

    public boolean doesGenerateBlock() {
        return generateBlock;
    }

    public boolean doesGenerateOre(){
        return generateOre;
    }

    public boolean doesRequireTool() {
        return requiresTool;
    }

    public StoneType setState(Block block) {
        this.state = block.defaultBlockState();
        return this;
    }

    public StoneType setState(BlockState blockState) {
        this.state = blockState;
        return this;
    }

    public StoneType setStateSupplier(Supplier<BlockState> blockState) {
        this.stateSupplier = blockState;
        return this;
    }

    public boolean getGravity() {
        return gravity;
    }

    public static Collection<Collection<Texture>> getAllTextures() {
        List<Collection<Texture>> textures = new ObjectArrayList<>();
        for (StoneType type : getAllGeneratingBlock()) {
            textures.add(Arrays.asList(type.getTextures()));
        }
        return textures;
    }

    //TODO collection
    public static StoneType[] getAllGeneratingBlock() {
        return GTAPI.all(StoneType.class).stream().filter(s -> s.generateBlock).toArray(StoneType[]::new);
    }

    public void initSuppliedState() {
        if (state == null && stateSupplier != null) {
            state = stateSupplier.get();
        }
    }

    public static StoneType get(String id) {
        return GTAPI.get(StoneType.class, id);
    }

    public static StoneType fromBlock(Block block) {
        return GTAPI.all(StoneType.class).stream().filter(s -> s.getState().getBlock() == block).findFirst().orElse(null);
    }
}
