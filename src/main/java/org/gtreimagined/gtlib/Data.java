package org.gtreimagined.gtlib;

import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.blockentity.multi.BlockEntityMultiMachine;
import org.gtreimagined.gtlib.blockentity.single.BlockEntityInfiniteStorage;
import org.gtreimagined.gtlib.capability.IGuiHandler;
import org.gtreimagined.gtlib.cover.CoverDebug;
import org.gtreimagined.gtlib.cover.CoverDynamo;
import org.gtreimagined.gtlib.cover.CoverEnergy;
import org.gtreimagined.gtlib.cover.CoverFactory;
import org.gtreimagined.gtlib.cover.CoverHeat;
import org.gtreimagined.gtlib.cover.CoverInput;
import org.gtreimagined.gtlib.cover.CoverMuffler;
import org.gtreimagined.gtlib.cover.CoverOutput;
import org.gtreimagined.gtlib.cover.ICover;
import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.gui.MenuHandlerCover;
import org.gtreimagined.gtlib.gui.MenuHandlerMachine;
import org.gtreimagined.gtlib.gui.MenuHandlerPipe;
import org.gtreimagined.gtlib.gui.container.ContainerBasicMachine;
import org.gtreimagined.gtlib.gui.container.ContainerCover;
import org.gtreimagined.gtlib.gui.container.ContainerMachine;
import org.gtreimagined.gtlib.gui.container.ContainerMultiMachine;
import org.gtreimagined.gtlib.item.ItemCover;
import org.gtreimagined.gtlib.item.ItemFluidIcon;
import org.gtreimagined.gtlib.item.ScannerItem;
import org.gtreimagined.gtlib.machine.types.BasicMachine;
import org.gtreimagined.gtlib.texture.Texture;
import org.gtreimagined.gtlib.tool.enchantment.ElectricEnchantment;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.api.distmarker.Dist;
import org.jetbrains.annotations.Nullable;

import static org.gtreimagined.gtlib.gui.ButtonOverlay.*;
import static org.gtreimagined.gtlib.machine.MachineFlag.EU;
import static org.gtreimagined.gtlib.machine.MachineFlag.GUI;
import static org.gtreimagined.gtlib.machine.Tier.MAX;

;

public class Data {

    public static final Material WRENCH_MATERIAL = new Material(MaterialColor.METAL, false, true, true, true, false, false, PushReaction.NORMAL);

    public static ScannerItem DEBUG_SCANNER = new ScannerItem(Ref.ID, "debug_scanner", false).tip(ChatFormatting.AQUA + "" + ChatFormatting.ITALIC + "Development Item");

    public static ItemFluidIcon FLUID_ICON = new ItemFluidIcon();
    //public static Machine<?> MACHINE_INVALID = new Machine<>(Ref.ID, "invalid");

    public static final MobType CREEPER = new MobType();

    public static Enchantment ENERGY_EFFICIENCY = GTAPI.register(Enchantment.class, "energy_efficiency", Ref.ID, new ElectricEnchantment(Enchantment.Rarity.UNCOMMON, EnchantmentCategory.BREAKABLE, EquipmentSlot.MAINHAND));
    public static Enchantment IMPLOSION = GTAPI.register(Enchantment.class, "implosion", Ref.ID, new DamageEnchantment(Enchantment.Rarity.UNCOMMON, 2, EquipmentSlot.MAINHAND){
        @Override
        public float getDamageBonus(int level, MobType type) {
            return type == CREEPER ? (float)level * 2.5F : 0.0F;
        }
    });

    public static CoverFactory COVEROUTPUT = CoverFactory.builder(CoverOutput::new).addTextures(new Texture(Ref.ID, "block/cover/output")).build(Ref.ID, "output");
    public static CoverFactory COVERHEAT = CoverFactory.builder(CoverHeat::new).addTextures(new Texture(Ref.ID, "block/cover/output")).build(Ref.ID, "heat");
    public static CoverFactory COVERDEBUG = CoverFactory.builder(CoverDebug::new).addTextures(new Texture(Ref.ID, "block/cover/debug")).build(Ref.ID, "debug_cover");
    public static ItemCover COVERDEBUG_ITEM = new ItemCover(Ref.ID, "debug_cover");

    public static CoverFactory COVERINPUT = CoverFactory.builder(CoverInput::new).addTextures(new Texture(Ref.ID, "block/cover/input")).build(Ref.ID, "input");
    public static CoverFactory COVERMUFFLER = CoverFactory.builder(CoverMuffler::new).addTextures(new Texture(Ref.ID, "block/cover/muffler")).build(Ref.ID, "muffler");
    public static CoverFactory COVERDYNAMO = CoverFactory.builder(CoverDynamo::new).addTextures(new Texture(Ref.ID, "block/cover/dynamo")).build(Ref.ID, "dynamo");
    public static CoverFactory COVERENERGY = CoverFactory.builder(CoverEnergy::new).addTextures(new Texture(Ref.ID, "block/cover/energy")).build(Ref.ID, "energy");


    public static MenuHandlerMachine<? extends BlockEntityMachine, ? extends ContainerBasicMachine> BASIC_MENU_HANDLER = new MenuHandlerMachine(Ref.ID, "container_basic") {
        @Nullable
        @Override
        public ContainerMachine<?> getMenu(IGuiHandler tile, Inventory playerInv, int windowId) {
            return tile instanceof BlockEntityMachine ? new ContainerBasicMachine((BlockEntityMachine<?>) tile, playerInv, this, windowId) : null;
        }

        @Override
        public String screenID() {
            return "machine";
        }
    };

    public static MenuHandlerPipe<?> PIPE_MENU_HANDLER = new MenuHandlerPipe<>(Ref.ID, "container_pipe");

    public static MenuHandlerCover<ContainerCover> COVER_MENU_HANDLER = new MenuHandlerCover<>(Ref.ID, "container_cover") {
        @Override
        public ContainerCover getMenu(IGuiHandler tile, Inventory playerInv, int windowId) {
            return new ContainerCover((ICover) tile, playerInv, this, windowId);
        }
    };

    public static MenuHandlerMachine<? extends BlockEntityMultiMachine, ? extends ContainerMultiMachine> MULTI_MENU_HANDLER = new MenuHandlerMachine(Ref.ID, "container_multi") {
        @Override
        public ContainerMultiMachine getMenu(IGuiHandler tile, Inventory playerInv, int windowId) {
            return tile instanceof BlockEntityMultiMachine ? new ContainerMultiMachine((BlockEntityMultiMachine<?>) tile, playerInv, this, windowId) : null;
        }

        @Override
        public String screenID() {
            return "multi";
        }
    };

    public static final BasicMachine CREATIVE_GENERATOR = new BasicMachine(Ref.ID, "creative_generator").addFlags(EU, GUI).setTiers(MAX).setVerticalFacingAllowed(true).setAllowsFrontIO().setTile(BlockEntityInfiniteStorage::new)
            .setNoOutputCover();

    public static void init(Dist side) {
        CREATIVE_GENERATOR.getGuiData().setBackgroundTexture("creative_generator");
        if (side.isClient()){
            CREATIVE_GENERATOR.addGuiCallback(t -> {
                t.addButton(10, 18, APAD_LEFT, false)
                        .addButton(25, 18, PAD_LEFT, false)
                        .addButton(10, 33, APAD_LEFT, false)
                        .addButton(25, 33, PAD_LEFT, false)
                        .addButton(10, 48, APAD_LEFT, false)
                        .addButton(25, 48, PAD_LEFT, false)
                        .addButton(10, 63, APAD_LEFT, false)
                        .addButton(25, 63, PAD_LEFT, false)
                        .addButton(137, 18, PAD_RIGHT, false)
                        .addButton(152, 18, APAD_RIGHT, false)
                        .addButton(137, 33, PAD_RIGHT, false)
                        .addButton(152, 33, APAD_RIGHT, false)
                        .addButton(137, 48, PAD_RIGHT, false)
                        .addButton(152, 48, APAD_RIGHT, false)
                        .addButton(137, 63, PAD_RIGHT, false)
                        .addButton(152, 63, APAD_RIGHT, false);
            });
        }
    }

    public static void postInit() {
        GTMaterialTypes.postInit();
    }
}
