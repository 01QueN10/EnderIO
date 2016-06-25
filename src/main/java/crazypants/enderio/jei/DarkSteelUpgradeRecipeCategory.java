package crazypants.enderio.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Triple;

import crazypants.enderio.EnderIO;
import crazypants.enderio.Log;
import crazypants.enderio.item.darksteel.DarkSteelRecipeManager;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.ISubtypeRegistry.ISubtypeInterpreter;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.BlankRecipeCategory;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class DarkSteelUpgradeRecipeCategory extends BlankRecipeCategory<DarkSteelUpgradeRecipeCategory.DarkSteelUpgradeRecipeWrapper> {

  public static final @Nonnull String UID = "DarkSteelUpgrade";

  // ------------ Recipes

  public static class DarkSteelUpgradeRecipeWrapper extends BlankRecipeWrapper {

    private final Triple<ItemStack, ItemStack, ItemStack> stacks;

    public DarkSteelUpgradeRecipeWrapper(Triple<ItemStack, ItemStack, ItemStack> stacks) {
      this.stacks = stacks;
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
    }

    public void setInfoData(Map<Integer, ? extends IGuiIngredient<ItemStack>> ings) {
    }

    @Override
    public @Nonnull List<?> getInputs() {
      List<ItemStack> itemInputs = new ArrayList<ItemStack>();
      itemInputs.add(stacks.getLeft());
      itemInputs.add(stacks.getMiddle());
      return itemInputs;
    }

    @SuppressWarnings("null")
    @Override
    public @Nonnull List<?> getOutputs() {
      return Collections.singletonList(stacks.getRight());
    }

  } // -------------------------------------

  public static void register(IModRegistry registry, IGuiHelper guiHelper) {

    registry.addRecipeCategories(new DarkSteelUpgradeRecipeCategory(guiHelper));
    registry.addRecipeHandlers(new BaseRecipeHandler<DarkSteelUpgradeRecipeWrapper>(DarkSteelUpgradeRecipeWrapper.class, DarkSteelUpgradeRecipeCategory.UID));
    registry.addRecipeCategoryCraftingItem(new ItemStack(Blocks.ANVIL), DarkSteelUpgradeRecipeCategory.UID);
    registry.addRecipeCategoryCraftingItem(new ItemStack(EnderIO.blockDarkSteelAnvil), DarkSteelUpgradeRecipeCategory.UID);

    long start = System.nanoTime();
    final List<Triple<ItemStack, ItemStack, ItemStack>> allRecipes = DarkSteelRecipeManager.instance.getAllRecipes();
    long end = System.nanoTime();

    Set<Item> items = new HashSet<Item>();
    for (Triple<ItemStack, ItemStack, ItemStack> rec : allRecipes) {
      items.add(rec.getLeft().getItem());
      items.add(rec.getRight().getItem());
    }
    ISubtypeRegistry subtypeRegistry = registry.getJeiHelpers().getSubtypeRegistry();
    DarkSteelUpgradeSubtypeInterpreter dsusi = new DarkSteelUpgradeSubtypeInterpreter();
    for (Item item : items) {
      subtypeRegistry.registerNbtInterpreter(item, dsusi);
    }

    List<DarkSteelUpgradeRecipeWrapper> result = new ArrayList<DarkSteelUpgradeRecipeWrapper>();
    for (Triple<ItemStack, ItemStack, ItemStack> rec : allRecipes) {
      result.add(new DarkSteelUpgradeRecipeWrapper(rec));
    }
    registry.addRecipes(result);

    registry.getRecipeTransferRegistry().addRecipeTransferHandler(ContainerRepair.class, DarkSteelUpgradeRecipeCategory.UID, 0, 2, 3, 4 * 9);

    Log.info(String.format("DarkSteelUpgradeRecipeCategory: Added %d dark steel upgrade recipes to JEI in %.3f seconds.", allRecipes.size(),
        (end - start) / 1000000000d));
  }

  // ------------ Category

  // Offsets from full size gui, makes it much easier to get the location
  // correct
  private int xOff = 15;
  private int yOff = 40;

  @Nonnull
  private final IDrawable background;

  public DarkSteelUpgradeRecipeCategory(IGuiHelper guiHelper) {
    ResourceLocation backgroundLocation = new ResourceLocation("textures/gui/container/anvil.png");
    background = guiHelper.createDrawable(backgroundLocation, xOff, yOff, 146, 24);
  }

  @Override
  public @Nonnull String getUid() {
    return UID;
  }

  @SuppressWarnings("null")
  @Override
  public @Nonnull String getTitle() {
    return Blocks.ANVIL.getLocalizedName();
  }

  @Override
  public @Nonnull IDrawable getBackground() {
    return background;
  }

  @SuppressWarnings("null")
  @Override
  public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull DarkSteelUpgradeRecipeWrapper recipeWrapper) {
    IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

    guiItemStacks.init(0, true, 27 - xOff - 1, 47 - yOff - 1);
    guiItemStacks.init(1, true, 76 - xOff - 1, 47 - yOff - 1);
    guiItemStacks.init(2, false, 134 - xOff - 1, 47 - yOff - 1);

    guiItemStacks.setFromRecipe(0,recipeWrapper.stacks.getLeft());
    guiItemStacks.setFromRecipe(1,recipeWrapper.stacks.getMiddle());
    guiItemStacks.setFromRecipe(2,recipeWrapper.stacks.getRight());
  }

  public static class DarkSteelUpgradeSubtypeInterpreter implements ISubtypeInterpreter {

    @Override
    @Nullable
    public String getSubtypeInfo(@Nonnull ItemStack itemStack) {
      return DarkSteelRecipeManager.instance.getUpgradesAsString(itemStack);
    }

  }

}
