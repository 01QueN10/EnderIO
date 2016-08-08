package crazypants.enderio.fluid;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import crazypants.enderio.config.Config;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockFluidEio extends BlockFluidClassic {

  public static BlockFluidEio create(Fluid fluid, Material material) {
    BlockFluidEio res = new BlockFluidEio(fluid, material);
    res.init();
    fluid.setBlock(res);
    return res;
  }

  protected Fluid fluid;

  protected BlockFluidEio(Fluid fluid, Material material) {
    super(fluid, material);
    this.fluid = fluid;    
    setUnlocalizedName(fluid.getUnlocalizedName());
    setRegistryName( "block" + StringUtils.capitalize(fluidName));
  }

  protected void init() {
    GameRegistry.register(this);
  }

  @Override
  public boolean canDisplace(IBlockAccess world, BlockPos pos) {
    IBlockState bs = world.getBlockState(pos);
    if(bs.getMaterial().isLiquid()) {
      return false;
    }
    return super.canDisplace(world, pos);
  }

  @Override
  public boolean displaceIfPossible(World world, BlockPos pos) {
    IBlockState bs = world.getBlockState(pos);
    if(bs.getMaterial().isLiquid()) {
      return false;
    }
    return super.displaceIfPossible(world, pos);
  }

  @Override
  public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
    if(entity.worldObj.isRemote) {
      super.onEntityCollidedWithBlock(world, pos, state, entity);
      return;
    }

    if(this == Fluids.blockFireWater) {
      entity.setFire(50);
    } else if(this == Fluids.blockRocketFuel && entity instanceof EntityLivingBase) {
      ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 150, 3, true, true));
    } else if(this == Fluids.blockNutrientDistillation && entity instanceof EntityPlayerMP) {
      long time = entity.worldObj.getTotalWorldTime();
      EntityPlayerMP player = (EntityPlayerMP) entity;
      if(time % Config.nutrientFoodBoostDelay == 0 && player.getEntityData().getLong("eioLastFoodBoost") != time) {
        player.getFoodStats().addStats(1, 0.1f);
        player.getEntityData().setLong("eioLastFoodBoost", time);
      }
    } else if (this == Fluids.blockHootch && entity instanceof EntityLivingBase) {
      ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 150, 0, true, true));
    } else if (this == Fluids.blockLiquidSunshine && entity instanceof EntityLivingBase) {
      ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.LEVITATION, 50, 0, true, true));
      ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.GLOWING, 1200, 0, true, true));
    } else if (this == Fluids.blockCloudSeedConcentrated && entity instanceof EntityLivingBase) {
      ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 40, 0, true, true));
    }

    super.onEntityCollidedWithBlock(world,pos, state, entity);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
    if (tab != null) {
      super.getSubBlocks(itemIn, tab, list);
    }
  }

}