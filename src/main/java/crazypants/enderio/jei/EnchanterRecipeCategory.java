package crazypants.enderio.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.BlankRecipeCategory;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.gui.ingredients.GuiIngredient;
import mezz.jei.gui.ingredients.IGuiIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import crazypants.enderio.EnderIO;
import crazypants.enderio.gui.GuiContainerBaseEIO;
import crazypants.enderio.machine.enchanter.EnchanterRecipe;
import crazypants.enderio.machine.enchanter.EnchanterRecipeManager;
import crazypants.enderio.machine.enchanter.GuiEnchanter;
import crazypants.enderio.machine.enchanter.TileEnchanter;

public class EnchanterRecipeCategory extends BlankRecipeCategory {

  public static final @Nonnull String UID = "Enchanter";

  // ------------ Recipes

  public static class EnchanterRecipeWrapper extends BlankRecipeWrapper {

    private final EnchanterRecipe rec;

    Map<Integer, ? extends IGuiIngredient<ItemStack>> currentIngredients;
    
    public EnchanterRecipeWrapper(EnchanterRecipe rec) {
      this.rec = rec;
    }

    public boolean isValid() {
      return rec != null && rec.isValid();
    }
    
    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
      if(currentIngredients == null) {
        return;
      }
      
      ItemStack stack = null;
      IGuiIngredient<ItemStack> ging = currentIngredients.get(1);            
      if(ging instanceof GuiIngredient) {
        GuiIngredient<ItemStack> gi = (GuiIngredient<ItemStack>)ging;
        stack = gi.getIngredient();
      }
      if(stack == null) {
        return;
      }
      int level = rec.getLevelForStackSize(stack.stackSize);
      int cost = TileEnchanter.getEnchantmentCost(rec, level);
      String str = I18n.format("container.repair.cost", new Object[] { cost });
      minecraft.fontRendererObj.drawString(str, 6, 36, 0x80FF20);      
    }

    public void setInfoData(Map<Integer, ? extends IGuiIngredient<ItemStack>> ings) {
     currentIngredients = ings;      
    }
    
    @Override
    public @Nonnull List<?> getInputs() {
      List<ItemStack> itemInputs = new ArrayList<ItemStack>();        
      List<ItemStack> itemOutputs = new ArrayList<ItemStack>();        
      getItemStacks(rec, itemInputs, itemOutputs);
      itemInputs.add(new ItemStack(Items.writable_book));
      return itemInputs;
    }

    @Override
    public @Nonnull List<?> getOutputs() {
      List<ItemStack> itemInputs = new ArrayList<ItemStack>();        
      List<ItemStack> itemOutputs = new ArrayList<ItemStack>();        
      getItemStacks(rec, itemInputs, itemOutputs);
      return itemOutputs;
    }

  }

  public static void register(IModRegistry registry, IGuiHelper guiHelper) {

    registry.addRecipeCategories(new EnchanterRecipeCategory(guiHelper));
    registry.addRecipeHandlers(new BaseRecipeHandler<EnchanterRecipeWrapper>(EnchanterRecipeWrapper.class, EnchanterRecipeCategory.UID) {

      @Override
      public boolean isRecipeValid(@Nonnull EnchanterRecipeWrapper recipe) {
        return recipe.isValid();
      }

    });
    registry.addRecipeClickArea(GuiEnchanter.class, 155, 8, 16, 16, EnchanterRecipeCategory.UID);

    List<EnchanterRecipeWrapper> result = new ArrayList<EnchanterRecipeWrapper>();

    for (EnchanterRecipe rec : EnchanterRecipeManager.getInstance().getRecipes()) {
      result.add(new EnchanterRecipeWrapper(rec));
    }

    registry.addRecipes(result);
  }

  // ------------ Category

  // Offsets from full size gui, makes it much easier to get the location
  // correct
  private int xOff = 22;
  private int yOff = 24;

  @Nonnull
  private final IDrawable background;

  private EnchanterRecipeWrapper currentRecipe;

  public EnchanterRecipeCategory(IGuiHelper guiHelper) {
    ResourceLocation backgroundLocation = EnderIO.proxy.getGuiTexture("enchanter");
    background = guiHelper.createDrawable(backgroundLocation, xOff, yOff, 134, 48);
  }

  @Override
  public @Nonnull String getUid() {
    return UID;
  }

  @SuppressWarnings("null")
  @Override
  public @Nonnull String getTitle() {
    return EnderIO.blockEnchanter.getLocalizedName();
  }

  @Override
  public @Nonnull IDrawable getBackground() {
    return background;
  }

  @Override
  public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull IRecipeWrapper recipeWrapper) {

    if (recipeWrapper instanceof EnchanterRecipeWrapper) {
      currentRecipe = (EnchanterRecipeWrapper) recipeWrapper;
    } else {
      currentRecipe = null;
      return;
    }

    IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();   
    
    Map<Integer, ? extends IGuiIngredient<ItemStack>> ings = guiItemStacks.getGuiIngredients();
    currentRecipe.setInfoData(ings);
    
    guiItemStacks.init(0, true, 25 - xOff, 34 - yOff);
    guiItemStacks.init(1, true, 75 - xOff, 34 - yOff);
    guiItemStacks.init(2, false, 133 - xOff, 34 - yOff);

    guiItemStacks.setFromRecipe(0, new ItemStack(Items.writable_book));
    
    EnchanterRecipe rec = currentRecipe.rec;
    List<ItemStack> itemInputs = new ArrayList<ItemStack>();        
    List<ItemStack> itemOutputs = new ArrayList<ItemStack>();        
    getItemStacks(rec, itemInputs, itemOutputs);
    guiItemStacks.set(1, itemInputs);
    guiItemStacks.set(2, itemOutputs);
  }

  private static void getItemStacks(EnchanterRecipe rec, List<ItemStack> itemInputs, List<ItemStack> itemOutputs) {
    ItemStack item = rec.getInput().getInput();
    for (int level = 1; level <= rec.getEnchantment().getMaxLevel(); level++) {
      itemInputs.add(new ItemStack(item.getItem(), level  * rec.getItemsPerLevel() ,item.getMetadata()));      
      EnchantmentData enchantment = new EnchantmentData(rec.getEnchantment(), level);
      ItemStack output = new ItemStack(Items.enchanted_book);
      Items.enchanted_book.addEnchantment(output, enchantment);
      itemOutputs.add(output);
    }
  }

}
