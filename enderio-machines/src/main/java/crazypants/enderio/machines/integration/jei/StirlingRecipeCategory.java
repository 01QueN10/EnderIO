package crazypants.enderio.machines.integration.jei;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.annotation.Nonnull;

import com.enderio.core.client.render.ColorUtil;
import com.enderio.core.common.util.NNList;

import crazypants.enderio.base.EnderIO;
import crazypants.enderio.base.Log;
import crazypants.enderio.base.capacitor.DefaultCapacitorData;
import crazypants.enderio.base.integration.jei.energy.EnergyIngredient;
import crazypants.enderio.base.integration.jei.energy.EnergyIngredientRenderer;
import crazypants.enderio.machines.capacitor.CapacitorKey;
import crazypants.enderio.machines.init.MachineObject;
import crazypants.enderio.machines.lang.Lang;
import crazypants.enderio.machines.machine.generator.stirling.ContainerStirlingGenerator;
import crazypants.enderio.machines.machine.generator.stirling.GuiStirlingGenerator;
import crazypants.enderio.machines.machine.generator.stirling.TileStirlingGenerator;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeCategory;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class StirlingRecipeCategory extends BlankRecipeCategory<StirlingRecipeCategory.StirlingRecipeWrapper> {

  public static final @Nonnull String UID = "StirlingGenerator";

  // ------------ Recipes

  public static class StirlingRecipeWrapper extends BlankRecipeWrapper {

    private final NNList<ItemStack> solidFuel;

    private StirlingRecipeWrapper(NNList<ItemStack> solidFuel) {
      this.solidFuel = solidFuel;
    }

    @Override
    public void getIngredients(@Nonnull IIngredients ingredients) {
      List<List<ItemStack>> list = new ArrayList<>();
      list.add(solidFuel);
      ingredients.setInputLists(ItemStack.class, list);

      int minEnergyProduced = Math
          .round(TileStirlingGenerator.getBurnTimeGeneric(solidFuel.get(0)) * TileStirlingGenerator.getBurnTimeMultiplier(DefaultCapacitorData.BASIC_CAPACITOR)
              * CapacitorKey.SIMPLE_STIRLING_POWER_GEN.getFloat(DefaultCapacitorData.BASIC_CAPACITOR));
      int maxEnergyProduced = Math
          .round(TileStirlingGenerator.getBurnTimeGeneric(solidFuel.get(0)) * TileStirlingGenerator.getBurnTimeMultiplier(DefaultCapacitorData.ENDER_CAPACITOR)
              * CapacitorKey.STIRLING_POWER_GEN.getFloat(DefaultCapacitorData.ENDER_CAPACITOR));
      ingredients.setOutputs(EnergyIngredient.class,
          new NNList<>(new EnergyIngredient(Math.round(CapacitorKey.SIMPLE_STIRLING_POWER_GEN.getFloat(DefaultCapacitorData.BASIC_CAPACITOR)), true),
              new EnergyIngredient(Math.round(CapacitorKey.STIRLING_POWER_GEN.getFloat(DefaultCapacitorData.ENDER_CAPACITOR)), true),
              new EnergyIngredient(minEnergyProduced, false), new EnergyIngredient(maxEnergyProduced, false)));
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
      FontRenderer fr = minecraft.fontRenderer;

      String txt = Lang.GUI_STIRGEN_OUTPUT.get("");
      int sw = fr.getStringWidth(txt);
      fr.drawStringWithShadow(txt, 89 - sw / 2 - xOff, 0 - yOff, ColorUtil.getRGB(Color.WHITE));
      txt = "-";
      sw = fr.getStringWidth(txt);
      fr.drawStringWithShadow(txt, 89 - sw / 2 - xOff, 16 - yOff, ColorUtil.getRGB(Color.WHITE));
      fr.drawStringWithShadow(txt, 89 - sw / 2 - xOff, 71 - yOff, ColorUtil.getRGB(Color.WHITE));

      GlStateManager.color(1, 1, 1, 1);
    }

    @Override
    public @Nonnull List<String> getTooltipStrings(int mouseX, int mouseY) {
      if (mouseY < (20 - yOff) || mouseY > (21 - yOff + 47 + 1)) {
        return Lang.JEI_COMBGEN_RANGE.getLines();
      }
      return super.getTooltipStrings(mouseX, mouseY);
    }

  } // -------------------------------------

  public static void register(IModRegistry registry, IGuiHelper guiHelper) {

    registry.addRecipeCategories(new StirlingRecipeCategory(guiHelper));
    registry.addRecipeCategoryCraftingItem(new ItemStack(MachineObject.block_stirling_generator.getBlockNN(), 1, 0), StirlingRecipeCategory.UID);
    registry.addRecipeCategoryCraftingItem(new ItemStack(MachineObject.block_simple_stirling_generator.getBlockNN(), 1, 0), StirlingRecipeCategory.UID);
    registry.addRecipeClickArea(GuiStirlingGenerator.class, 155, 42, 16, 16, StirlingRecipeCategory.UID);
    registry.getRecipeTransferRegistry().addRecipeTransferHandler(ContainerStirlingGenerator.class, StirlingRecipeCategory.UID, 0, 1, 2, 4 * 9);
    registry.getRecipeTransferRegistry().addRecipeTransferHandler(ContainerStirlingGenerator.class, StirlingRecipeCategory.UID, 0, 1, 1, 4 * 9);

    long start = System.nanoTime();

    // Put valid fuel to "buckets" based on their burn time (energy production)
    HashMap<Integer, NNList<ItemStack>> recipeInputs = new HashMap<>();
    List<ItemStack> validItems = registry.getIngredientRegistry().getIngredients(ItemStack.class);
    int fuelCount = 0;
    for (ItemStack stack : validItems) {
      int burntime = stack == null ? -1 : TileStirlingGenerator.getBurnTimeGeneric(stack);
      if (burntime <= 0)
        continue;
      ++fuelCount;
      if (recipeInputs.containsKey(burntime)) {
        recipeInputs.get(burntime).add(stack);
      } else {
        NNList<ItemStack> list = new NNList<>();
        list.add(stack);
        recipeInputs.put(burntime, list);
      }
    }

    List<StirlingRecipeWrapper> recipeList = new ArrayList<StirlingRecipeWrapper>();
    // Order recipes from best to worst
    TreeSet<Integer> recipeOrder = new TreeSet<Integer>(recipeInputs.keySet());
    Iterator<Integer> it = recipeOrder.descendingIterator();
    while (it.hasNext())
      recipeList.add(new StirlingRecipeWrapper(recipeInputs.get(it.next())));

    registry.addRecipes(recipeList, UID);

    long end = System.nanoTime();
    Log.info(String.format("StirlingRecipeCategory: Added %d stirling generator recipes for %d solid fuel to JEI in %.3f seconds.", recipeList.size(),
        fuelCount, (end - start) / 1000000000d));
  }

  // ------------ Category

  // Offsets from full size gui, makes it much easier to get the location
  // correct
  static int xOff = 25 + 3;
  static int yOff = 7;
  static int xSize = 136 - 3;

  @Nonnull
  private final IDrawable background;

  public StirlingRecipeCategory(IGuiHelper guiHelper) {
    ResourceLocation backgroundLocation = EnderIO.proxy.getGuiTexture("stirling_generator");
    background = guiHelper.createDrawable(backgroundLocation, xOff, yOff, xSize, 70);
  }

  @Override
  public @Nonnull String getUid() {
    return UID;
  }

  @SuppressWarnings("null")
  @Override
  public @Nonnull String getTitle() {
    return MachineObject.block_stirling_generator.getBlock().getLocalizedName();
  }

  @Override
  public @Nonnull IDrawable getBackground() {
    return background;
  }

  @Override
  public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull StirlingRecipeWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
    IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
    IGuiIngredientGroup<EnergyIngredient> group = recipeLayout.getIngredientsGroup(EnergyIngredient.class);

    guiItemStacks.init(0, true, 79 - xOff, 33 - yOff);
    group.init(1, false, EnergyIngredientRenderer.INSTANCE, 37 - xOff, 15 - yOff, 40, 10, 0, 0);
    group.init(2, false, EnergyIngredientRenderer.INSTANCE, 54 + 47 - xOff, 15 - yOff, 40, 10, 0, 0);
    group.init(3, false, EnergyIngredientRenderer.INSTANCE, 32 - xOff, 70 - yOff, 40, 10, 0, 0);
    group.init(4, false, EnergyIngredientRenderer.INSTANCE, 54 + 44 - xOff, 70 - yOff, 40, 10, 0, 0);

    guiItemStacks.set(ingredients);
    group.set(ingredients);
  }

}
