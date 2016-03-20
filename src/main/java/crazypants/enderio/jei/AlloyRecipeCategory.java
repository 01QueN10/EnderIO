package crazypants.enderio.jei;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import crazypants.enderio.EnderIO;
import crazypants.enderio.machine.alloy.AlloyRecipeManager;
import crazypants.enderio.machine.alloy.GuiAlloySmelter;
import crazypants.enderio.machine.power.PowerDisplayUtil;
import crazypants.enderio.machine.recipe.IRecipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.BlankRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import crazypants.enderio.gui.GuiContainerBaseEIO;

public class AlloyRecipeCategory extends BlankRecipeCategory {

  public static final @Nonnull String UID = "AlloySmelter";

  // ------------ Recipes

  public static class AlloyRecipe extends RecipeWrapper {
    public AlloyRecipe(IRecipe recipe) {
      super(recipe);
    }
  }
 
  public static void register(IModRegistry registry, IGuiHelper guiHelper) {
    
    registry.addRecipeCategories(new AlloyRecipeCategory(guiHelper));
    registry.addRecipeHandlers(new RecipeHandler<AlloyRecipe>(AlloyRecipe.class, AlloyRecipeCategory.UID));
    registry.addRecipeClickArea(GuiAlloySmelter.class, 155, 42, 16, 16, AlloyRecipeCategory.UID);
    
    List<AlloyRecipe> result = new ArrayList<AlloyRecipe>();
    for (IRecipe rec : AlloyRecipeManager.getInstance().getRecipes()) {
      result.add(new AlloyRecipe(rec));
    }
    for (IRecipe rec : AlloyRecipeManager.getInstance().getVanillaRecipe().getAllRecipes()) {
      result.add(new AlloyRecipe(rec));
    }
    registry.addRecipes(result);
  }

  // ------------ Category

  //Offsets from full size gui, makes it much easier to get the location correct
  private int xOff = 45;
  private int yOff = 3;
  
  @Nonnull
  private final IDrawable background;

  @Nonnull
  protected final IDrawableAnimated flame;
  
  private AlloyRecipe currentRecipe;

  public AlloyRecipeCategory(IGuiHelper guiHelper) {
    ResourceLocation backgroundLocation = GuiContainerBaseEIO.getGuiTexture("alloySmelter");
    background = guiHelper.createDrawable(backgroundLocation, xOff, yOff, 82, 78);

    IDrawableStatic flameDrawable = guiHelper.createDrawable(backgroundLocation, 176, 0, 13, 13);
    flame = guiHelper.createAnimatedDrawable(flameDrawable, 200, IDrawableAnimated.StartDirection.BOTTOM, false);
  }

  @Override
  public @Nonnull String getUid() {
    return UID;
  }

  @Override
  public @Nonnull String getTitle() {
    String localizedName = EnderIO.blockAlloySmelter.getLocalizedName();
    return localizedName != null ? localizedName : "ERROR";
  }

  @Override
  public @Nonnull IDrawable getBackground() {
    return background;
  }

  @Override
  public void drawAnimations(@Nonnull Minecraft minecraft) {
    flame.draw(minecraft, 56 - xOff, 36 - yOff);
    flame.draw(minecraft, 103 - xOff, 36 - yOff);
  }
  
  @Override
  public void drawExtras(@Nonnull Minecraft minecraft) {
    if(currentRecipe == null) {
      return;
    }
    String energyString = PowerDisplayUtil.formatPower(currentRecipe.getEnergyRequired()) + " " + PowerDisplayUtil.abrevation();
    minecraft.fontRendererObj.drawString(energyString, 108 - xOff, 62 - yOff, 0x808080, false);    
    GlStateManager.color(1,1,1,1);
  }
  
  @Override
  public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull IRecipeWrapper recipeWrapper) {
    IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

    guiItemStacks.init(0, true, 53 - xOff, 16 - yOff);
    guiItemStacks.init(1, true, 78 - xOff, 6 - yOff);
    guiItemStacks.init(2, true, 102 - xOff, 16 - yOff);
    guiItemStacks.init(3, false, 78 - xOff, 57 - yOff);

    List<?> inputs = recipeWrapper.getInputs();
    for (int index = 0; index < inputs.size(); index++) {
      Object ingredients = inputs.get(index);
      if (ingredients != null) {
        guiItemStacks.setFromRecipe(index, ingredients);
      }
    }
    List<?> outputs = recipeWrapper.getOutputs();
    if (outputs != null) {
      guiItemStacks.setFromRecipe(3, outputs);
    }
    
    if(recipeWrapper instanceof AlloyRecipe) {
      currentRecipe = (AlloyRecipe)recipeWrapper;
    } else {
      currentRecipe = null;
    }

  }

}
