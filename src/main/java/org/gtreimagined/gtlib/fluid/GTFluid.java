package org.gtreimagined.gtlib.fluid;

import lombok.Getter;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.registration.IRegistryEntryProvider;
import org.gtreimagined.gtlib.registration.ISharedGTObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.ForgeFlowingFluid.Flowing;
import net.minecraftforge.fluids.ForgeFlowingFluid.Properties;
import net.minecraftforge.fluids.ForgeFlowingFluid.Source;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;

/**
 * GTFluid is an object that includes all essential information of what a normal fluid would compose of in Minecraft
 * Source + Flowing fluid instances, with a FlowingFluidBlock that handles the in-world block form of them.
 * Item instance is also provided, usually BucketItem or its derivatives.
 * But is generified to an Item instance: {@link net.minecraftforge.fluids.ForgeFlowingFluid#getFilledBucket}
 * <p>
 * TODO: generic getFluidContainer()
 * TODO: Cell Models
 */
public class GTFluid implements ISharedGTObject, IRegistryEntryProvider {

    public static final ResourceLocation OVERLAY_TEXTURE = new ResourceLocation("block/water_overlay");
    public static final ResourceLocation LIQUID_STILL_TEXTURE = new ResourceLocation(Ref.ID, "block/liquid/still");
    public static final ResourceLocation LIQUID_FLOW_TEXTURE = new ResourceLocation(Ref.ID, "block/liquid/flow");
    public static final ResourceLocation LIQUID_HOT_STILL_TEXTURE = new ResourceLocation(Ref.ID, "block/liquid/hot_still");
    public static final ResourceLocation LIQUID_HOT_FLOW_TEXTURE = new ResourceLocation(Ref.ID, "block/liquid/hot_flow");
    public static final ResourceLocation GAS_TEXTURE = new ResourceLocation(Ref.ID, "block/liquid/gas");
    public static final ResourceLocation GAS_FLOW_TEXTURE = new ResourceLocation(Ref.ID, "block/liquid/gas"); // _flow
    public static final ResourceLocation PLASMA_TEXTURE = new ResourceLocation(Ref.ID, "block/liquid/plasma");
    public static final ResourceLocation PLASMA_FLOW_TEXTURE = new ResourceLocation(Ref.ID, "block/liquid/plasma"); // _flow

    private final String domain, id;
    protected Properties fluidProperties;
    protected Source source;
    protected Flowing flowing;
    @Getter
    protected Block.Properties blockProperties;
    @Getter
    protected FluidType fluidType;
    @Getter
    protected IClientFluidTypeExtensions extension;
    @Getter
    protected LiquidBlock fluidBlock;
    @Getter
    protected Item containerItem = Items.AIR;

    public GTFluid(String domain, String id, FluidType.Properties builder, Block.Properties blockProperties, IClientFluidTypeExtensions typeExtensions) {
        this.domain = domain;
        this.id = id;
        this.fluidProperties = new Properties(this::getFluidType, this::getFluid, this::getFlowingFluid).bucket(this::getContainerItem).block(this::getFluidBlock);
        this.blockProperties = blockProperties;
        this.extension = typeExtensions;
        this.fluidType = new FluidType(builder){
            @Override
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                consumer.accept(extension);
            }
        };
    }

    public GTFluid(String domain, String id) {
        this(domain, id, getDefaultFluidTypeProperties(false), getDefaultBlockProperties(), getDefaultFluidTypeClientExtension(false));
    }

    public GTFluid(String domain, String id, FluidType.Properties builder) {
        this(domain, id, builder, getDefaultBlockProperties(), getDefaultFluidTypeClientExtension(false));
    }

    public GTFluid(String domain, String id, ResourceLocation stillLoc, ResourceLocation flowLoc) {
        this(domain, id, getDefaultFluidTypeProperties(false), getDefaultBlockProperties(), GTClientFluidTypeExtension.create(b -> b.stillTexture(stillLoc).flowingTexture(flowLoc)));
    }

    public GTFluid(String domain, String id, Block.Properties properties) {
        this(domain, id, getDefaultFluidTypeProperties(false), properties, getDefaultFluidTypeClientExtension(false));
    }

    @Override
    public void onRegistryBuild(ResourceKey<? extends Registry<?>> registry) {
        if (registry == ForgeRegistries.Keys.ITEMS) {
            GTAPI.register(Item.class, getId() + "_bucket", getDomain(), containerItem = new BucketItem(this::getFluid, new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));
        } else if (registry == ForgeRegistries.Keys.BLOCKS) {
            this.fluidBlock = new LiquidBlock(this::getFluid, blockProperties);
            GTAPI.register(Block.class, "block_fluid_".concat(getId()), getDomain(), fluidBlock);
        } else if (registry == ForgeRegistries.Keys.FLUIDS) {
            this.source = new Source(this.fluidProperties);
            this.flowing = new Flowing(this.fluidProperties);
            GTAPI.register(Fluid.class, getId(), getDomain(), source);
            GTAPI.register(Fluid.class, "flowing_" + getId(), getDomain(), flowing);
            GTAPI.register(FlowingFluid.class, "flowing_".concat(getId()), getDomain(), flowing);
        } else if (registry == ForgeRegistries.Keys.FLUID_TYPES){
            GTAPI.register(FluidType.class, getId(), getDomain(), fluidType);
        }
    }

    public GTFluid source(Source source) {
        this.source = source;
        return this;
    }

    public GTFluid flowing(Flowing flowing) {
        this.flowing = flowing;
        return this;
    }

    public GTFluid flowingBlock(LiquidBlock fluidBlock) {
        this.fluidBlock = fluidBlock;
        return this;
    }

    public GTFluid containerItem(Item item) {
        this.containerItem = item;
        return this;
    }

    @Override
    public String getId() {
        return id;
    }

    public Source getFluid() {
        return source;
    }

    public Flowing getFlowingFluid() {
        return flowing;
    }

    protected static Block.Properties getDefaultBlockProperties() {
        return Block.Properties.of().mapColor(MapColor.WATER).replaceable().pushReaction(PushReaction.DESTROY).liquid().strength(100.0F).noLootTable();
    }

    protected static FluidType.Properties getDefaultFluidTypeProperties(boolean hot){
        if (hot){
            return FluidType.Properties.create().sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA);
        }
        return FluidType.Properties.create().sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL);
    }

    protected static GTClientFluidTypeExtension getDefaultFluidTypeClientExtension(boolean hot){
        if (hot){
            return GTClientFluidTypeExtension.create(b -> b.stillTexture(LIQUID_HOT_STILL_TEXTURE).flowingTexture(LIQUID_HOT_FLOW_TEXTURE).overlayTexture(OVERLAY_TEXTURE));
        }
        return GTClientFluidTypeExtension.create(b -> b.stillTexture(LIQUID_STILL_TEXTURE).flowingTexture(LIQUID_FLOW_TEXTURE).overlayTexture(OVERLAY_TEXTURE));
    }
}
