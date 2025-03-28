package org.gtreimagined.gtlib.proxy;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.gtreimagined.gtlib.AntimatterAPI;
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
import org.gtreimagined.gtlib.fluid.AntimatterFluid;
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
        GTTextureStitcher.addStitcher(event -> AntimatterAPI.all(CoverFactory.class).forEach(cover -> {
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
        AntimatterAPI.all(Material.class, Material::setChemicalFormula);
        /* Register screens. */
        AntimatterAPI.runLaterClient(() -> {
            Set<ResourceLocation> registered = new ObjectOpenHashSet<>();
            AntimatterAPI.all(MenuHandler.class, h -> {
                if (!registered.contains(ForgeRegistries.CONTAINERS.getKey(h.getContainerType()))) {
                    registered.add(ForgeRegistries.CONTAINERS.getKey(h.getContainerType()));
                    MenuScreens.register(h.getContainerType(), AntimatterAPI.get(MenuScreens.ScreenConstructor.class, h.screenID(), h.screenDomain()));
                }
            });
        });
        /* Set up render types. */
        AntimatterAPI.runLaterClient(() -> {
            AntimatterAPI.all(BlockMachine.class, b -> ModelUtils.setRenderLayer(b, RenderType.cutout()));
            AntimatterAPI.all(BlockFakeTile.class, b -> ModelUtils.setRenderLayer(b, RenderType.cutout()));
            AntimatterAPI.all(BlockMultiMachine.class, b -> ModelUtils.setRenderLayer(b, RenderType.cutout()));
            AntimatterAPI.all(BlockOre.class, b -> ModelUtils.setRenderLayer(b, RenderType.cutout()));
            AntimatterAPI.all(BlockPipe.class, b -> ModelUtils.setRenderLayer(b, RenderType.cutout()));
            AntimatterAPI.all(BlockStorage.class).stream().filter(b -> b.getType() == GTMaterialTypes.RAW_ORE_BLOCK)
                    .forEach(b -> ModelUtils.setRenderLayer(b, RenderType.cutout()));
            AntimatterAPI.all(BlockFrame.class).stream().filter(b -> b.getType() == GTMaterialTypes.FRAME)
                    .forEach(b -> ModelUtils.setRenderLayer(b, RenderType.cutout()));
            AntimatterAPI.all(BlockSurfaceRock.class).stream().forEach(b -> ModelUtils.setRenderLayer(b, RenderType.cutout()));
            AntimatterAPI.all(AntimatterFluid.class).forEach(f -> {
                ModelUtils.setRenderLayer(f.getFluid(), RenderType.translucent());
                ModelUtils.setRenderLayer(f.getFlowingFluid(), RenderType.translucent());
            });
        });
        AntimatterAPI.all(Machine.class).stream().filter(Machine::renderAsTesr).filter(Machine::renderContainerLiquids).map(Machine::getTileType).distinct().forEach(i -> BlockEntityRenderers.register(i, MachineTESR::new));
    }

    public static void onItemColorHandler(ItemColors colors) {
        for (Item item : AntimatterAPI.all(Item.class)) {
            if (item instanceof IColorHandler h && h.registerColorHandlers()) {
                colors.register((stack, i) -> h.getItemColor(stack, null, i), item);
            }
        }
        for (Block block : AntimatterAPI.all(Block.class)) {
            if (block instanceof IColorHandler h && h.registerColorHandlers()) {
                colors.register((stack, i) -> h.getItemColor(stack, null, i),
                        block.asItem());
            }
        }
    }

    public static void onBlockColorHandler(BlockColors colors) {
        for (Block block : AntimatterAPI.all(Block.class)) {
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
