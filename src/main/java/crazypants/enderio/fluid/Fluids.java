package crazypants.enderio.fluid;

import java.lang.reflect.Field;

import crazypants.enderio.EnderIO;
import crazypants.enderio.Log;
import crazypants.enderio.config.Config;
import crazypants.enderio.machine.generator.zombie.PacketNutrientTank;
import crazypants.enderio.network.PacketHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Fluids {

  public static final String NUTRIENT_DISTILLATION_NAME = "nutrient_distillation";
  public static final String HOOTCH_NAME = "hootch";
  public static final String ROCKET_FUEL_NAME = "rocket_fuel";
  public static final String FIRE_WATER_NAME = "fire_water";
  
  public static final String LIQUID_SUNSHINE_NAME = "liquid_sunshine";
  public static final String CLOUD_SEED_NAME = "cloud_seed";
  public static final String CLOUD_SEED_CONCENTRATED_NAME = "cloud_seed_concentrated";

  public static Fluid fluidNutrientDistillation;
  public static BlockFluidEio blockNutrientDistillation;

  public static Fluid fluidHootch;
  public static BlockFluidEio blockHootch;

  public static Fluid fluidRocketFuel;
  public static BlockFluidEio blockRocketFuel;

  public static Fluid fluidFireWater;
  public static BlockFluidEio blockFireWater;
  
  public static Fluid fluidLiquidSunshine;
  public static Fluid fluidCloudSeed;
  public static Fluid fluidCloudSeedConcentrated;
  
  public static BlockFluidEio blockLiquidSunshine;
  public static BlockFluidEio blockCloudSeed;
  public static BlockFluidEio blockCloudSeedConcentrated;

  // Open block compatable liquid XP
  public static Fluid fluidXpJuice;

  public static ResourceLocation getStill(String fluidName) {
    return new ResourceLocation(EnderIO.MODID, "blocks/" + fluidName + "_still");
  }

  public static ResourceLocation getFlowing(String fluidName) {
    return new ResourceLocation(EnderIO.MODID, "blocks/" + fluidName + "_flow");
  }

  public static String toCapactityString(IFluidTank tank) {
    if (tank == null) {
      return "0/0 " + MB();
    }
    return tank.getFluidAmount() + "/" + tank.getCapacity() + " " + MB();
  }

  public static String MB() {
    return EnderIO.lang.localize("fluid.millibucket.abr");
  }

  public void registerFluids() {
    Fluid f = new Fluid(Fluids.NUTRIENT_DISTILLATION_NAME, getStill(Fluids.NUTRIENT_DISTILLATION_NAME), getFlowing(Fluids.NUTRIENT_DISTILLATION_NAME))
        .setDensity(1500).setViscosity(3000);
    FluidRegistry.registerFluid(f);
    fluidNutrientDistillation = FluidRegistry.getFluid(f.getName());
    blockNutrientDistillation = BlockFluidEio.create(fluidNutrientDistillation, Material.water);

    PacketHandler.INSTANCE.registerMessage(PacketNutrientTank.class, PacketNutrientTank.class, PacketHandler.nextID(), Side.CLIENT);

    f = new Fluid(Fluids.HOOTCH_NAME, Fluids.getStill(Fluids.HOOTCH_NAME), Fluids.getFlowing(Fluids.HOOTCH_NAME)).setDensity(900).setViscosity(1000);
    FluidRegistry.registerFluid(f);
    fluidHootch = FluidRegistry.getFluid(f.getName());
    blockHootch = BlockFluidEio.create(fluidHootch, Material.water);
    FluidFuelRegister.instance.addFuel(f, Config.hootchPowerPerCycleRF, Config.hootchPowerTotalBurnTime);
    FMLInterModComms.sendMessage("Railcraft", "boiler-fuel-liquid",
        Fluids.HOOTCH_NAME + "@" + (Config.hootchPowerPerCycleRF / 10 * Config.hootchPowerTotalBurnTime));

    f = new Fluid(Fluids.ROCKET_FUEL_NAME, Fluids.getStill(Fluids.ROCKET_FUEL_NAME), Fluids.getFlowing(Fluids.ROCKET_FUEL_NAME)).setDensity(900)
        .setViscosity(1000);
    FluidRegistry.registerFluid(f);
    fluidRocketFuel = FluidRegistry.getFluid(f.getName());
    blockRocketFuel = BlockFluidEio.create(fluidRocketFuel, Material.water);
    FluidFuelRegister.instance.addFuel(f, Config.rocketFuelPowerPerCycleRF, Config.rocketFuelPowerTotalBurnTime);
    FMLInterModComms.sendMessage("Railcraft", "boiler-fuel-liquid",
        Fluids.ROCKET_FUEL_NAME + "@" + (Config.rocketFuelPowerPerCycleRF / 10 * Config.rocketFuelPowerTotalBurnTime));

    f = new Fluid(Fluids.FIRE_WATER_NAME, Fluids.getStill(Fluids.FIRE_WATER_NAME), Fluids.getFlowing(Fluids.FIRE_WATER_NAME)).setDensity(900)
        .setViscosity(1000);
    FluidRegistry.registerFluid(f);
    fluidFireWater = FluidRegistry.getFluid(f.getName());
    blockFireWater = BlockFluidEio.create(fluidFireWater, Material.lava);
    FluidFuelRegister.instance.addFuel(f, Config.fireWaterPowerPerCycleRF, Config.fireWaterPowerTotalBurnTime);
    FMLInterModComms.sendMessage("Railcraft", "boiler-fuel-liquid",
        Fluids.FIRE_WATER_NAME + "@" + (Config.fireWaterPowerPerCycleRF / 10 * Config.fireWaterPowerTotalBurnTime));

    f = new Fluid(Fluids.LIQUID_SUNSHINE_NAME, getStill(LIQUID_SUNSHINE_NAME), getFlowing(LIQUID_SUNSHINE_NAME)).setDensity(200).setViscosity(400);
    FluidRegistry.registerFluid(f);
    fluidLiquidSunshine = FluidRegistry.getFluid(f.getName());
    blockLiquidSunshine = BlockFluidEio.create(fluidLiquidSunshine, Material.water);

    f = new Fluid(Fluids.CLOUD_SEED_NAME, getStill(CLOUD_SEED_NAME), getFlowing(CLOUD_SEED_NAME)).setDensity(500).setViscosity(800);
    FluidRegistry.registerFluid(f);
    fluidCloudSeed = FluidRegistry.getFluid(f.getName());
    blockCloudSeed = BlockFluidEio.create(fluidCloudSeed, Material.water);
    
    f = new Fluid(Fluids.CLOUD_SEED_CONCENTRATED_NAME, getStill(CLOUD_SEED_CONCENTRATED_NAME), getFlowing(CLOUD_SEED_CONCENTRATED_NAME)).setDensity(1000).setViscosity(1200);
    FluidRegistry.registerFluid(f);
    fluidCloudSeedConcentrated = FluidRegistry.getFluid(f.getName());
    blockCloudSeedConcentrated = BlockFluidEio.create(fluidCloudSeedConcentrated, Material.water);
    
    if (!Loader.isModLoaded("OpenBlocks")) {
      Log.info("XP Juice registered by Ender IO.");
      fluidXpJuice = new Fluid(Config.xpJuiceName, Fluids.getStill(Fluids.FIRE_WATER_NAME), Fluids.getFlowing(Fluids.FIRE_WATER_NAME)).setLuminosity(10)
          .setDensity(800).setViscosity(1500).setUnlocalizedName("eio.xpjuice");
      FluidRegistry.registerFluid(fluidXpJuice);
    } else {
      Log.info("XP Juice regististration left to Open Blocks.");
    }

    Buckets.createBuckets();
  }

  public void forgeRegisterXPJuice() {
    fluidXpJuice = FluidRegistry.getFluid(getXPJuiceName());
    if (fluidXpJuice == null) {
      Log.error("Liquid XP Juice registration left to open blocks but could not be found.");
    }
  }

  @SideOnly(Side.CLIENT)
  public void registerRenderers() {
    MinecraftForge.EVENT_BUS.register(this);
    registerFluidBlockRendering(fluidNutrientDistillation, NUTRIENT_DISTILLATION_NAME);
    registerFluidBlockRendering(fluidHootch, HOOTCH_NAME);
    registerFluidBlockRendering(fluidFireWater, FIRE_WATER_NAME);
    registerFluidBlockRendering(fluidRocketFuel, ROCKET_FUEL_NAME);
    registerFluidBlockRendering(fluidLiquidSunshine, LIQUID_SUNSHINE_NAME);
    registerFluidBlockRendering(fluidCloudSeed, CLOUD_SEED_NAME);
    registerFluidBlockRendering(fluidCloudSeedConcentrated, CLOUD_SEED_CONCENTRATED_NAME);
  }

  @SideOnly(Side.CLIENT)
  public void registerFluidBlockRendering(Fluid fluid, String name) {

    Block block = fluid.getBlock();
    Item item = Item.getItemFromBlock(block);
    FluidStateMapper mapper = new FluidStateMapper(fluid);
    // item-model
    ModelLoader.registerItemVariants(item);
    ModelLoader.setCustomMeshDefinition(item, mapper);
    // block-model
    ModelLoader.setCustomStateMapper(block, mapper);
  }

  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void onIconLoad(TextureStitchEvent.Pre event) {
    if (fluidXpJuice != null) {
      event.map.registerSprite(fluidXpJuice.getStill());
      event.map.registerSprite(fluidXpJuice.getFlowing());
    }

  }

  private static String getXPJuiceName() {
    String openBlocksXPJuiceName = null;

    try {
      Field getField = Class.forName("openblocks.Config").getField("xpFluidId");
      openBlocksXPJuiceName = (String) getField.get(null);
    } catch (Exception e) {
    }

    if (openBlocksXPJuiceName != null && !Config.xpJuiceName.equals(openBlocksXPJuiceName)) {
      Log.info("Overwriting XP Juice name with '" + openBlocksXPJuiceName + "' taken from OpenBlocks' config");
      return openBlocksXPJuiceName;
    }

    return Config.xpJuiceName;
  }

  public static class FluidStateMapper extends StateMapperBase implements ItemMeshDefinition {

    public final Fluid fluid;
    public final ModelResourceLocation location;

    public FluidStateMapper(Fluid fluid) {
      this.fluid = fluid;
      location = new ModelResourceLocation(EnderIO.MODID.toLowerCase() + ":fluids", fluid.getName());
    }

    @Override
    protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
      return location;
    }

    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack) {
      return location;
    }
  }

}
