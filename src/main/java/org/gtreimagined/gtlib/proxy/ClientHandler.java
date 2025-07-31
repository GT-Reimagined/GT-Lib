package org.gtreimagined.gtlib.proxy;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.block.BlockFakeTile;
import org.gtreimagined.gtlib.block.BlockFrame;
import org.gtreimagined.gtlib.block.BlockStorage;
import org.gtreimagined.gtlib.block.BlockSurfaceRock;
import org.gtreimagined.gtlib.client.GTTextureStitcher;
import org.gtreimagined.gtlib.client.ModelUtils;
import org.gtreimagined.gtlib.client.tesr.MachineTESR;
import org.gtreimagined.gtlib.cover.CoverFactory;
import org.gtreimagined.gtlib.cover.ICover;
import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.fluid.GTFluid;
import org.gtreimagined.gtlib.gui.MenuHandler;
import org.gtreimagined.gtlib.machine.BlockMachine;
import org.gtreimagined.gtlib.machine.BlockMultiMachine;
import org.gtreimagined.gtlib.machine.types.Machine;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialType;
import org.gtreimagined.gtlib.ore.BlockOre;
import org.gtreimagined.gtlib.pipe.BlockPipe;
import org.gtreimagined.gtlib.registration.IColorHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

public class ClientHandler implements IProxyHandler {

    @SuppressWarnings("ConstantConditions")
    public ClientHandler() {
        GTTextureStitcher.addStitcher(event -> GTAPI.all(CoverFactory.class).forEach(cover -> {
            if (cover == ICover.emptyFactory)
                return;
            for (ResourceLocation r : cover.getTextures()) {
                event.accept(r);
            }
        }));
    }

    public static boolean isLocal() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return true;
        ClientPacketListener listener =  mc.getConnection();
        if (listener == null) return true;
        return listener.getConnection().isMemoryConnection();
    }

    @SuppressWarnings({"unchecked", "unused"})
    public static void setup() {
        MaterialType.buildTooltips();
        GTAPI.all(Material.class, Material::setChemicalFormula);
        /* Register screens. */
        GTAPI.runLaterClient(() -> {
            Set<ResourceLocation> registered = new ObjectOpenHashSet<>();
            GTAPI.all(MenuHandler.class, h -> {
                if (!registered.contains(ForgeRegistries.MENU_TYPES.getKey(h.getContainerType()))) {
                    registered.add(ForgeRegistries.MENU_TYPES.getKey(h.getContainerType()));
                    MenuScreens.register(h.getContainerType(), GTAPI.get(MenuScreens.ScreenConstructor.class, h.screenID(), h.screenDomain()));
                }
            });
        });
        /* Set up render types. */
        GTAPI.runLaterClient(() -> {
            GTAPI.all(BlockMachine.class, b -> ModelUtils.setRenderLayer(b, RenderType.cutout()));
            GTAPI.all(BlockFakeTile.class, b -> ModelUtils.setRenderLayer(b, RenderType.cutout()));
            GTAPI.all(BlockMultiMachine.class, b -> ModelUtils.setRenderLayer(b, RenderType.cutout()));
            GTAPI.all(BlockOre.class, b -> ModelUtils.setRenderLayer(b, RenderType.cutout()));
            GTAPI.all(BlockPipe.class, b -> ModelUtils.setRenderLayer(b, RenderType.cutout()));
            GTAPI.all(BlockStorage.class).stream().filter(b -> b.getType() == GTMaterialTypes.RAW_ORE_BLOCK)
                    .forEach(b -> ModelUtils.setRenderLayer(b, RenderType.cutout()));
            GTAPI.all(BlockFrame.class).stream().filter(b -> b.getType() == GTMaterialTypes.FRAME)
                    .forEach(b -> ModelUtils.setRenderLayer(b, RenderType.cutout()));
            GTAPI.all(BlockSurfaceRock.class).stream().forEach(b -> ModelUtils.setRenderLayer(b, RenderType.cutout()));
            GTAPI.all(GTFluid.class).forEach(f -> {
                ModelUtils.setRenderLayer(f.getFluid(), RenderType.translucent());
                ModelUtils.setRenderLayer(f.getFlowingFluid(), RenderType.translucent());
            });
        });
        GTAPI.all(Machine.class).stream().filter(Machine::renderAsTesr).filter(Machine::rendersContainedLiquids).map(Machine::getTileType).distinct().forEach(i -> BlockEntityRenderers.register(i, MachineTESR::new));
    }

    public static void onItemColorHandler(ItemColors colors) {
        for (Item item : GTAPI.all(Item.class)) {
            if (item instanceof IColorHandler h && h.registerColorHandlers()) {
                colors.register((stack, i) -> h.getItemColor(stack, null, i), item);
            }
        }
        for (Block block : GTAPI.all(Block.class)) {
            if (block instanceof IColorHandler h && h.registerColorHandlers()) {
                colors.register((stack, i) -> h.getItemColor(stack, null, i),
                        block.asItem());
            }
        }
    }

    public static void onBlockColorHandler(BlockColors colors) {
        for (Block block : GTAPI.all(Block.class)) {
            if (block instanceof IColorHandler h && h.registerColorHandlers())
                colors.register(h::getBlockColor, block);
        }
    }

    public static void onModelRegistry() {

    }

    @Override
    public Level getClientWorld() {
        return Minecraft.getInstance().level;
    }

    @Override
    public Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }
}
