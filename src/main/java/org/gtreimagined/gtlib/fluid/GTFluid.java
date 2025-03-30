package org.gtreimagined.gtlib.fluid;

import lombok.Getter;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.registration.IRegistryEntryProvider;
import org.gtreimagined.gtlib.registration.ISharedGTObject;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid.Flowing;
import net.minecraftforge.fluids.ForgeFlowingFluid.Properties;
import net.minecraftforge.fluids.ForgeFlowingFluid.Source;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

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
    protected FluidAttributes attributes;
    @Getter
    protected LiquidBlock fluidBlock;
    @Getter
    protected Item containerItem = Items.AIR;

    public GTFluid(String domain, String id, FluidAttributes.Builder builder, Block.Properties blockProperties) {
        this.domain = domain;
        this.id = id;
        this.fluidProperties = new Properties(this::getFluid, this::getFlowingFluid, builder).bucket(this::getContainerItem).block(this::getFluidBlock);
        this.blockProperties = blockProperties;
        this.attributes = builder.translationKey(Util.makeDescriptionId("fluid_type", this.getLoc())).build(this.source);
    }

    public GTFluid(String domain, String id) {
        this(domain, id, getDefaultAttributesBuilder(), getDefaultBlockProperties());
    }

    public GTFluid(String domain, String id, FluidAttributes.Builder builder) {
        this(domain, id, builder, getDefaultBlockProperties());
    }

    public GTFluid(String domain, String id, ResourceLocation stillLoc, ResourceLocation flowLoc) {
        this(domain, id, FluidAttributes.builder(stillLoc, flowLoc), getDefaultBlockProperties());
    }

    public GTFluid(String domain, String id, Block.Properties properties) {
        this(domain, id, getDefaultAttributesBuilder(), properties);
    }

    @Override
    public void onRegistryBuild(IForgeRegistry<?> registry) {
        if (registry == ForgeRegistries.ITEMS) {
            GTAPI.register(Item.class, getId() + "_bucket", getDomain(), containerItem = new BucketItem(this::getFluid, new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET).tab(CreativeModeTab.TAB_MISC)));
        } else if (registry == ForgeRegistries.BLOCKS) {
            this.source = new Source(this.fluidProperties);
            this.flowing = new Flowing(this.fluidProperties);
            this.fluidBlock = new LiquidBlock(this::getFluid, blockProperties);
            GTAPI.register(Block.class, "block_fluid_".concat(getId()), getDomain(), fluidBlock);
        } else if (registry == ForgeRegistries.FLUIDS) {
            GTAPI.register(Fluid.class, getId(), getDomain(), source);
            GTAPI.register(FlowingFluid.class, "flowing_".concat(getId()), getDomain(), flowing);
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
        return Block.Properties.of(Material.WATER).strength(100.0F).noDrops();
    }

    protected static FluidAttributes.Builder getDefaultAttributesBuilder() {
        return getDefaultAttributesBuilder(false);
    }

    protected static FluidAttributes.Builder getDefaultAttributesBuilder(boolean hot) {
        if (hot) {
            return FluidAttributes.builder(LIQUID_HOT_STILL_TEXTURE, LIQUID_HOT_FLOW_TEXTURE).overlay(OVERLAY_TEXTURE).sound(SoundEvents.BUCKET_FILL_LAVA, SoundEvents.BUCKET_EMPTY_LAVA);
        }
        return FluidAttributes.builder(LIQUID_STILL_TEXTURE, LIQUID_FLOW_TEXTURE).overlay(OVERLAY_TEXTURE).sound(SoundEvents.BUCKET_FILL, SoundEvents.BUCKET_EMPTY);
    }
}
