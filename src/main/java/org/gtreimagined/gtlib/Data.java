package org.gtreimagined.gtlib;

import brachy.modularui.drawable.UITexture;
import brachy.modularui.widgets.ButtonWidget;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.gui.screens.Screen;
import org.gtreimagined.gtlib.blockentity.single.BlockEntityInfiniteStorage;
import org.gtreimagined.gtlib.cover.CoverDebug;
import org.gtreimagined.gtlib.cover.CoverDynamo;
import org.gtreimagined.gtlib.cover.CoverEnergy;
import org.gtreimagined.gtlib.cover.CoverFactory;
import org.gtreimagined.gtlib.cover.CoverHeat;
import org.gtreimagined.gtlib.cover.CoverInput;
import org.gtreimagined.gtlib.cover.CoverMuffler;
import org.gtreimagined.gtlib.cover.CoverOutput;
import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.gui.event.GuiEvents;
import org.gtreimagined.gtlib.mui.IInfoRenderer;
import org.gtreimagined.gtlib.item.ItemCover;
import org.gtreimagined.gtlib.item.ItemFluidIcon;
import org.gtreimagined.gtlib.item.ScannerItem;
import org.gtreimagined.gtlib.machine.types.BasicMachine;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.mui.GTGuiTextures;
import org.gtreimagined.gtlib.mui.widgets.GTInfoRenderWidget;
import org.gtreimagined.gtlib.texture.Texture;
import org.gtreimagined.gtlib.tool.enchantment.ElectricEnchantment;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.api.distmarker.Dist;

import static org.gtreimagined.gtlib.machine.MachineFlag.EU;
import static org.gtreimagined.gtlib.machine.MachineFlag.GUI;
import static org.gtreimagined.gtlib.machine.Tier.MAX;

;

public class Data {


    public static ScannerItem DEBUG_SCANNER = new ScannerItem(Ref.ID, "debug_scanner", false).tip(ChatFormatting.AQUA + "" + ChatFormatting.ITALIC + "Development Item");

    public static ItemFluidIcon FLUID_ICON = new ItemFluidIcon();
    //public static Machine<?> MACHINE_INVALID = new Machine<>(Ref.ID, "invalid");
    private static final Int2ObjectArrayMap<Material> MATERIAL_TIERMAP = new Int2ObjectArrayMap<>();

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



    public static final BasicMachine CREATIVE_GENERATOR = new BasicMachine(Ref.ID, "creative_generator").addFlags(EU, GUI).setTiers(MAX).setVerticalFacingAllowed(true).setAllowsFrontIO().setTile(BlockEntityInfiniteStorage::new)
            .setNoOutputCover();

    public static void init(Dist side) {
        CREATIVE_GENERATOR.getGuiFunctions().add(((modularPanel, machine, guiData, syncManager, settings) -> {
            modularPanel.child(GTGuiTextures.CREATIVE_GENERATOR_OVERLAY.asWidget().size(158, 61).pos(9, 17));
            for (int i = 0; i < 16; i++){
                boolean leftSide = i < 8;
                boolean leftOuter = i % 2 == 0;
                UITexture texture = leftSide ? (leftOuter ? GTGuiTextures.APAD_LEFT : GTGuiTextures.PAD_LEFT) : (leftOuter ? GTGuiTextures.PAD_RIGHT : GTGuiTextures.APAD_RIGHT);
                int x = leftSide ? (leftOuter ? 10 : 25) : (leftOuter ? 137 : 152);
                int y = (i < 8 ? i : i - 8) / 2;
                int finalI = i;
                modularPanel.child(new ButtonWidget<>()
                        .overlay(texture.getSubArea(0f, 0f, 1.0f, 0.5f))
                        .hoverOverlay(texture.getSubArea(0f, 0.5f, 1f, 1f))
                                .onMousePressed((context, mouseButton) -> {
                                    syncManager.callSyncedAction("extra_button_event", packet -> {
                                        packet.writeVarIntArray(new int[]{Screen.hasShiftDown() ? 1 : 0, finalI});
                                    });
                                    return true;
                                })
                        .size(14).pos(x, 18 + (15 * y)));
            }
            if (machine instanceof IInfoRenderer renderer){
                renderer.registerSyncHandlers(syncManager);
                modularPanel.child(new GTInfoRenderWidget(renderer)
                        .pos(renderer.getPos().x, renderer.getPos().y)
                        .size(renderer.getSize().x, renderer.getSize().y));
            }
        }));
    }

    public static void postInit() {
        GTMaterialTypes.postInit();
    }

    public static Int2ObjectArrayMap<Material> getMaterialTiermap() {
        return MATERIAL_TIERMAP;
    }

    public static void setMaterialTier(Material material, int tier){
        MATERIAL_TIERMAP.put(tier, material);
    }
}
