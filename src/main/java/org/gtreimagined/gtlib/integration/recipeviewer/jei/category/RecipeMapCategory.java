package org.gtreimagined.gtlib.integration.recipeviewer.jei.category;

import brachy.modularui.drawable.GuiTextures;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableAnimated.StartDirection;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.gtreimagined.gtlib.Data;
import org.gtreimagined.gtlib.integration.recipeviewer.jei.GTLibJEIPlugin;
import org.gtreimagined.gtlib.mui.BarDir;
import org.gtreimagined.gtlib.gui.GuiProperties;
import org.gtreimagined.gtlib.gui.SlotData;
import org.gtreimagined.gtlib.gui.SlotType;
import org.gtreimagined.gtlib.integration.recipeviewer.renderer.IRecipeInfoRenderer;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.recipe.IRecipe;
import org.gtreimagined.gtlib.recipe.ingredient.FluidIngredient;
import org.gtreimagined.gtlib.recipe.ingredient.RecipeIngredient;
import org.gtreimagined.gtlib.recipe.map.IRecipeMap;
import org.gtreimagined.gtlib.recipe.map.RecipeMap;
import org.gtreimagined.gtlib.recipe.map.SubCategory;
import org.gtreimagined.gtlib.util.FluidUtils;
import org.gtreimagined.gtlib.util.RegistryUtils;
import org.gtreimagined.gtlib.util.Utils;
import org.gtreimagined.gtlib.util.int4;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RecipeMapCategory implements IRecipeCategory<IRecipe> {

    protected static int JEI_OFFSET_X = 1, JEI_OFFSET_Y = 1;

    protected String title;
    protected final ResourceLocation loc;
    protected final RecipeType<IRecipe> type;
    protected IDrawable background, icon, progressBackground;
    protected IDrawableAnimated progressBar;
    protected GuiProperties gui;
    protected Tier guiTier;
    private final IRecipeInfoRenderer infoRenderer;

    public RecipeMapCategory(IRecipeMap map, RecipeType<IRecipe> type, GuiProperties gui, Tier defaultTier, ResourceLocation iconId) {
        loc = map.getLoc();
        this.type = type;
        this.guiTier = map.getGuiTier() == null ? defaultTier : map.getGuiTier();
        title = map.getDisplayName().getString();
        int4 area = gui.getArea(), progress = new int4(0, gui.getMachineData().getProgressSize().y, gui.getMachineData().getProgressSize().x, gui.getMachineData().getProgressSize().y);
        background = GTLibJEIPlugin.guiHelper.drawableBuilder(GuiTextures.MC_BACKGROUND.location(), area.x + 1, area.y, area.z, area.w).setTextureSize(195, 136).addPadding(0, (7 + (10 *4)), 0, 0).build();
        progressBar = GTLibJEIPlugin.guiHelper.drawableBuilder(gui.getMachineData().getProgressTexture(this.guiTier).location(), progress.x, progress.y, progress.z, progress.w).setTextureSize(progress.z, progress.w * 2).buildAnimated(50, fromDir(gui.getMachineData().getDir()), !gui.getMachineData().doesBarFill());
        progressBackground = GTLibJEIPlugin.guiHelper.drawableBuilder(gui.getMachineData().getProgressTexture(this.guiTier).location(), 0, 0, progress.z, progress.w).setTextureSize(progress.z, progress.w * 2).build();
        Object icon = map.getIcon();
        if (icon != null) {
            if (icon instanceof ItemStack itemStack) {
                this.icon = GTLibJEIPlugin.guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, itemStack);
            }
            if (icon instanceof ItemLike item) {
                this.icon = GTLibJEIPlugin.guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(item));
            }
        } else {
            Item item = iconId == null ? Data.DEBUG_SCANNER : RegistryUtils.getItemFromID(iconId);
            if (item == Items.AIR) item = Data.DEBUG_SCANNER;
            this.icon = GTLibJEIPlugin.guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(item, 1));
        }
        this.gui = gui;
        this.infoRenderer = map.getInfoRenderer();
    }

    public RecipeMapCategory(IRecipeMap map, RecipeType<IRecipe> type, GuiProperties gui, Tier defaultTier, ResourceLocation subCategoryId, SubCategory subCategory) {
        loc = subCategoryId;
        this.type = type;
        this.guiTier = map.getGuiTier() == null ? defaultTier : map.getGuiTier();
        title = Utils.translatable(subCategory.langKey()).getString();
        int4 area = gui.getArea(), progress = new int4(0, gui.getMachineData().getProgressSize().y, gui.getMachineData().getProgressSize().x, gui.getMachineData().getProgressSize().y);
        background = GTLibJEIPlugin.guiHelper.drawableBuilder(GuiTextures.MC_BACKGROUND.location(), area.x + 1, area.y, area.z, area.w).setTextureSize(195, 136).addPadding(0, (7 + (10 *4)), 0, 0).build();
        progressBar = GTLibJEIPlugin.guiHelper.drawableBuilder(gui.getMachineData().getProgressTexture(this.guiTier).location(), progress.x, progress.y, progress.z, progress.w).setTextureSize(progress.z, progress.w * 2).buildAnimated(50, fromDir(gui.getMachineData().getDir()), !gui.getMachineData().doesBarFill());
        progressBackground = GTLibJEIPlugin.guiHelper.drawableBuilder(gui.getMachineData().getProgressTexture(this.guiTier).location(), 0, 0, progress.z, progress.w).setTextureSize(progress.z, progress.w * 2).build();
        Object icon = subCategory.icon().get();
        if (icon != null) {
            if (icon instanceof ItemStack itemStack) {
                this.icon = GTLibJEIPlugin.guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, itemStack);
            }
            if (icon instanceof ItemLike item) {
                this.icon = GTLibJEIPlugin.guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(item));
            }
            if (icon instanceof ResourceLocation resourceLocation){
                this.icon = GTLibJEIPlugin.guiHelper.drawableBuilder(resourceLocation, 0, 0, 16, 16).setTextureSize(16, 16).build();
            }
        } else {
            Item item = Data.DEBUG_SCANNER;
            this.icon = GTLibJEIPlugin.guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(item, 1));
        }
        this.gui = gui;
        this.infoRenderer = map.getInfoRenderer();
    }

    private IDrawableAnimated.StartDirection fromDir(BarDir dir){
        return switch (dir){
            case UP -> StartDirection.BOTTOM;
            case DOWN -> StartDirection.TOP;
            case LEFT -> StartDirection.RIGHT;
            case RIGHT -> StartDirection.LEFT;
            case CW, CCW -> StartDirection.LEFT;
        };
    }

    @Override
    public RecipeType<IRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Utils.literal(title);
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }


    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, IRecipe recipe, IFocusGroup focuses) {
        List<List<ItemStack>> inputs = recipe.hasInputItems() ? recipe.getInputItems().stream().map(t -> Arrays.asList(t.getItems())).toList() : Collections.emptyList();
        List<ItemStack> outputs = recipe.getOutputItems(false);
        List<SlotData<?>> slots;
        int groupIndex = 0, slotCount;
        int offsetX = gui.getArea().x + JEI_OFFSET_X, offsetY = gui.getArea().y + JEI_OFFSET_Y;
        int inputItems = 0, inputFluids = 0;
        if (recipe.hasInputItems()) {
            slots = gui.getSlots().getSlots(SlotType.IT_IN, guiTier);
            slotCount = slots.size();
            if (slotCount > 0) {
                int s = 0;
                if (!inputs.isEmpty()) {
                    slotCount = Math.min(slotCount, inputs.size());
                    for (; s < slotCount; s++) {
                        IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT, slots.get(s).getJeiX() - (offsetX - 1), slots.get(s).getJeiY() - (offsetY - 1));
                        List<ItemStack> input = inputs.get(s);
                        if (input.isEmpty()) {
                            List<ItemStack> st = new ObjectArrayList<>(1);
                            st.add(new ItemStack(Data.DEBUG_SCANNER, 1).setHoverName(Utils.literal("Empty tag")));
                            slot.addIngredients(VanillaTypes.ITEM_STACK, st);
                        } else {
                            slot.addIngredients(VanillaTypes.ITEM_STACK, input);
                            final int ss = s;
                            slot.addTooltipCallback((ing, list) -> {
                                if (recipe.getInputItems().get(ss) instanceof RecipeIngredient ri) {
                                    if (ri.ignoreConsume()) {
                                        list.add(Utils.literal("Does not get consumed in the process.").withStyle(ChatFormatting.WHITE));
                                    }
                                    if (ri.ignoreNbt()) {
                                        list.add(Utils.literal("Ignores NBT.").withStyle(ChatFormatting.WHITE));
                                    }
                                    Ingredient i = recipe.getInputItems().get(ss);
                                    if (RecipeMap.isIngredientSpecial(i)) {
                                        list.add(Utils.literal("Special ingredient. Class name: ").withStyle(ChatFormatting.GRAY).append(Utils.literal(i.getClass().getSimpleName()).withStyle(ChatFormatting.GOLD)));
                                    }
                                }
                                if (recipe.hasInputChances()) {
                                    if (recipe.getInputChances()[ss] < 10000) {
                                        list.add(Utils.literal("Consumption Chance: " + ((float)recipe.getInputChances()[ss] / 100) + "%").withStyle(ChatFormatting.WHITE));
                                    }
                                }
                            });
                            inputItems++;
                        }
                    }
                }
            }
        }
        if (recipe.hasOutputItems()) {
            slots = gui.getSlots().getSlots(SlotType.IT_OUT, guiTier);
            slotCount = slots.size();
            if (slotCount > 0) {
                slotCount = Math.min(slotCount, outputs.size());
                for (int s = 0; s < slotCount; s++) {
                    IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.OUTPUT, slots.get(s).getJeiX() - (offsetX - 1), slots.get(s).getJeiY() - (offsetY - 1));
                    slot.addIngredient(VanillaTypes.ITEM_STACK, outputs.get(s));
                    final int ss = s;
                    slot.addTooltipCallback((ing, list) -> {
                        if (recipe.hasOutputChances()) {
                            if (recipe.getOutputChances()[ss] < 10000) {
                                list.add(Utils.literal("Output Chance: " + ((float)recipe.getOutputChances()[ss] / 100) + "%").withStyle(ChatFormatting.WHITE));
                            }
                        }
                    });
                }
            }
        }

        if (recipe.hasInputFluids()) {
            slots = gui.getSlots().getSlots(SlotType.FL_IN, guiTier);
            slotCount = slots.size();
            if (slotCount > 0) {
                List<FluidIngredient> fluids = recipe.getInputFluids();
                slotCount = Math.min(slotCount, fluids.size());
                for (int s = 0; s < slotCount; s++) {
                    IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT, slots.get(s).getJeiX() - (offsetX - 1), slots.get(s).getJeiY() - (offsetY - 1));
                    slot.addIngredients(ForgeTypes.FLUID_STACK, Arrays.asList(fluids.get(s).getStacks()));
                    slot.setFluidRenderer((int)fluids.get(s).getAmount(), true, 16, 16);
                    int finalS = s;
                    slot.addTooltipCallback((ing, list) -> {
                        FluidStack stack = fluids.get(finalS).getStacks()[0];
                        createFluidTooltip(ing, list, stack);
                    });
                    inputFluids++;
                }
            }
        }
        if (recipe.hasOutputFluids()) {
            slots = gui.getSlots().getSlots(SlotType.FL_OUT, guiTier);
            slotCount = slots.size();
            if (slotCount > 0) {
                List<FluidStack> fluids = recipe.getOutputFluids();
                slotCount = Math.min(slotCount, fluids.size());
                for (int s = 0; s < slotCount; s++) {
                    IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.OUTPUT, slots.get(s).getJeiX() - (offsetX - 1), slots.get(s).getJeiY() - (offsetY - 1));
                    slot.setFluidRenderer(fluids.get(s).getAmount(), true, 16, 16);
                    slot.addIngredients(ForgeTypes.FLUID_STACK, Collections.singletonList(fluids.get(s)));
                    int finalS = s;
                    slot.addTooltipCallback((ing, list) -> {
                        FluidStack stack = fluids.get(finalS);
                        createFluidTooltip(ing, list, stack);
                    });
                }
            }
        }
    }

    private void createFluidTooltip(IRecipeSlotView ing, List<Component> list, FluidStack stack) {
        Component component = null;
        for (Component comp : list) {
            if (comp.getContents() instanceof TranslatableContents translatable){
                if (translatable.getKey().equals("jei.tooltip.recipe.by")){
                    component = comp;
                    break;
                }
            }
        }
        if (list.size() > 2) list.remove(2);
        if (list.size() > 1) list.remove(1);
        int mb = stack.getAmount();
        list.add(Utils.translatable("gtlib.tooltip.fluid.amount", mb + " L").withStyle(ChatFormatting.BLUE));
        list.add(Utils.translatable("gtlib.tooltip.fluid.temp", FluidUtils.getFluidTemperature(stack.getFluid())).withStyle(ChatFormatting.RED));
        String liquid = !FluidUtils.isFluidGaseous(stack.getFluid()) ? "liquid" : "gas";
        list.add(Utils.translatable("gtlib.tooltip.fluid." + liquid).withStyle(ChatFormatting.GREEN));
        if (Utils.hasNoConsumeTag(ing.getDisplayedIngredient().get().getIngredient(ForgeTypes.FLUID_STACK).get()))
            list.add(Utils.literal("Does not get consumed in the process").withStyle(ChatFormatting.WHITE));
        if (component != null) list.add(component);
    }

    /*
    private static IRecipeSlotTooltipCallback itemCallback(Recipe recipe, boolean input) {
        return (a,b) ->
            if (input) {
                if (recipe.hasInputItems()) {
                    a.getDisplayedIngredient().flatMap(ing -> {
                        Ingredient i = ing.getIngredient();
                    })
                    if (recipe.getInputItems().get(index).ignoreConsume()) {
                        tooltip.add(Utils.literal("Does not get consumed in the process.").withStyle(ChatFormatting.WHITE));
                    }
                    if (recipe.getInputItems().size() >= index && recipe.getInputItems().get(index).ignoreNbt()) {
                        tooltip.add(Utils.literal("Ignores NBT.").withStyle(ChatFormatting.WHITE));
                    }
                    if (recipe.getInputItems().size() >= index) {
                        Ingredient i = recipe.getInputItems().get(index).get();
                        if (RecipeMap.isIngredientSpecial(i)) {
                            tooltip.add(Utils.literal("Special ingredient. Class name: ").withStyle(ChatFormatting.GRAY).append(Utils.literal(i.getClass().getSimpleName()).withStyle(ChatFormatting.GOLD)));
                        }
                    }
                }
            }
            if (recipe.hasChances() && !input) {
                int chanceIndex = index - finalInputItems;
                if (recipe.getChances()[chanceIndex] < 100) {
                    tooltip.add(Utils.literal("Chance: " + recipe.getChances()[chanceIndex] + "%").withStyle(ChatFormatting.WHITE));
                }
            }
        }
    }*/

    @Override
    public void draw(IRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        if (progressBackground != null){
            progressBackground.draw(graphics, gui.getMachineData().getProgressPos().x + gui.getArea().x, gui.getMachineData().getProgressPos().y + gui.getArea().y);
        }
        if (progressBar != null)
            progressBar.draw(graphics, gui.getMachineData().getProgressPos().x + gui.getArea().x, gui.getMachineData().getProgressPos().y + gui.getArea().y);
        gui.getSlots().getRecipeSlots(this.guiTier).forEach(s -> {
            IDrawable drawable = GTLibJEIPlugin.guiHelper.drawableBuilder(s.getBaseTexture().location(), 0, 0, 18, 18).setTextureSize(18, 18).build();
            drawable.draw(graphics, s.getJeiX() - 4,s.getJeiY() - 4);
            if (s.getOverlayTexture() != null){
                drawable = GTLibJEIPlugin.guiHelper.drawableBuilder(s.getOverlayTexture().location(), 0, 0, 18, 18).setTextureSize(18, 18).build();
                drawable.draw(graphics, s.getJeiX() - 4,s.getJeiY() - 4);
            }
        });
        List<Component> components = infoRenderer.getLines(recipe);
        if (!components.isEmpty()){
            for (int i = 0; i < components.size(); i++){
                int yOffset = gui.getArea().y + JEI_OFFSET_Y + gui.getArea().z / 2;
                graphics.drawString(Minecraft.getInstance().font, components.get(i), JEI_OFFSET_X + 5, (i * 10) + yOffset, 0xFFFFFFF);
            }
        }
        //infoRenderer.render(graphics, recipe, Minecraft.getInstance().font, JEI_OFFSET_X, gui.getArea().y + JEI_OFFSET_Y + gui.getArea().z / 2);

        int offsetX = gui.getArea().x + JEI_OFFSET_X, offsetY = gui.getArea().y + JEI_OFFSET_Y;
        //Draw chance overlay.
        if (recipe.hasOutputChances()) {
            List<IRecipeSlotView> views = recipeSlotsView.getSlotViews(RecipeIngredientRole.OUTPUT);
            List<SlotData<?>> slots = gui.getSlots().getSlots(SlotType.IT_OUT, guiTier);
            for (int i = 0; i < recipe.getOutputChances().length; i++) {
                if (recipe.getOutputChances()[i] < 10000) {
                    if (i >= slots.size()) break;
                    RenderSystem.disableBlend();
                    RenderSystem.disableDepthTest();
                    graphics.pose().pushPose();
                    graphics.pose().scale(0.5f, 0.5f, 1);
                    String ch = (recipe.getOutputChances()[i] / 100) + "%";
                    graphics.drawString(Minecraft.getInstance().font, ch, 2*((float)slots.get(i).getJeiX() - (offsetX - 1)), 2*((float) slots.get(i).getJeiY() - (offsetY - 1)), 0xFFFF00, true);

                    graphics.pose().popPose();
                    RenderSystem.enableBlend();
                    RenderSystem.enableDepthTest();
                }
            }
        }
        if (recipe.hasInputChances()) {
            List<IRecipeSlotView> views = recipeSlotsView.getSlotViews(RecipeIngredientRole.INPUT);
            List<SlotData<?>> slots = gui.getSlots().getSlots(SlotType.IT_IN, guiTier);
            for (int i = 0; i < recipe.getInputChances().length; i++) {
                if (recipe.getInputChances()[i] < 10000) {
                    if (i >= slots.size()) break;
                    RenderSystem.disableBlend();
                    RenderSystem.disableDepthTest();
                    graphics.pose().pushPose();
                    graphics.pose().scale(0.5f, 0.5f, 1);
                    String ch = (recipe.getInputChances()[i] / 100) + "%";
                    graphics.drawString(Minecraft.getInstance().font, ch, 2*((float)slots.get(i).getJeiX() - (offsetX - 1)), 2*((float) slots.get(i).getJeiY() - (offsetY - 1)), 0xFFFF00, true);

                    graphics.pose().popPose();
                    RenderSystem.enableBlend();
                    RenderSystem.enableDepthTest();
                }
            }
        }
    }

}
