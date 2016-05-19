package crazypants.enderio.machine.spawner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockMobSpawner;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderHell;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.enderio.core.api.client.gui.IAdvancedTooltipProvider;
import com.enderio.core.client.handlers.SpecialTooltipHandler;
import com.enderio.core.common.util.BlockCoord;

import crazypants.enderio.EnderIO;
import crazypants.enderio.GuiHandler;
import crazypants.enderio.Log;
import crazypants.enderio.ModObject;
import crazypants.enderio.config.Config;
import crazypants.enderio.machine.AbstractMachineBlock;
import crazypants.enderio.machine.MachineRecipeRegistry;
import crazypants.enderio.machine.RenderMappers;
import crazypants.enderio.network.PacketHandler;
import crazypants.enderio.paint.IPaintable;
import crazypants.enderio.render.IBlockStateWrapper;
import crazypants.enderio.render.IRenderMapper;
import crazypants.enderio.render.IRenderMapper.IItemRenderMapper;
import crazypants.enderio.waila.IWailaInfoProvider;
import crazypants.util.CapturedMob;

public class BlockPoweredSpawner extends AbstractMachineBlock<TilePoweredSpawner> implements IAdvancedTooltipProvider, IPaintable.INonSolidBlockPaintableBlock,
    IPaintable.IWrenchHideablePaint {
 
  public static final String KEY_SPAWNED_BY_POWERED_SPAWNER = "spawnedByPoweredSpawner";

  public static BlockPoweredSpawner create() {
    MachineRecipeRegistry.instance.registerRecipe(ModObject.blockPoweredSpawner.getUnlocalisedName(), new DummyRecipe());

    PacketHandler.INSTANCE.registerMessage(PacketMode.class, PacketMode.class, PacketHandler.nextID(), Side.SERVER);

    //Ensure costs are loaded at startup
    PoweredSpawnerConfig.getInstance();

    BlockPoweredSpawner res = new BlockPoweredSpawner();
    MinecraftForge.EVENT_BUS.register(res);    
    res.init();
    return res;
  }

  private final List<ResourceLocation> toolBlackList = new ArrayList<ResourceLocation>();

  private Field fieldpersistenceRequired; 
  private Field entNameField;
  private Field spawnDelayField;

  protected BlockPoweredSpawner() {
    super(ModObject.blockPoweredSpawner, TilePoweredSpawner.class);

    String[] blackListNames = Config.brokenSpawnerToolBlacklist;
    for (String blackListName : blackListNames) {
      toolBlackList.add(new ResourceLocation(blackListName));
    }

    try {
      fieldpersistenceRequired = ReflectionHelper.findField(EntityLiving.class, "field_82179_bU", "persistenceRequired");
    } catch (Exception e) {
      Log.error("BlockPoweredSpawner: Could not find field: persistenceRequired");
    }
    try {
    entNameField = ReflectionHelper.findField(MobSpawnerBaseLogic.class, "mobID", "field_98288_a" );
    } catch (Exception e) {
      Log.error("BlockPoweredSpawner: Could not find field: mobID");
    }
    try {
      spawnDelayField = ReflectionHelper.findField(MobSpawnerBaseLogic.class, "spawnDelay", "field_98286_b");
    } catch (Exception e) {
      Log.error("BlockPoweredSpawner: Could not find field: spawnDelay");
    }
  }

  private final Map<BlockCoord, ItemStack> dropCache = new HashMap<BlockCoord, ItemStack>();

  @SubscribeEvent
  public void onBreakEvent(BlockEvent.BreakEvent evt) {
    if(evt.getState().getBlock() instanceof BlockMobSpawner) {
      if(evt.getPlayer() != null && !evt.getPlayer().capabilities.isCreativeMode && !evt.getPlayer().worldObj.isRemote && !evt.isCanceled()) {
        TileEntity tile = evt.getPlayer().worldObj.getTileEntity(evt.getPos());
        if(tile instanceof TileEntityMobSpawner) {

          if(Math.random() > Config.brokenSpawnerDropChance) {
            return;
          }
          
          ItemStack equipped = evt.getPlayer().getHeldItemMainhand();
          if(equipped != null) {
            for (ResourceLocation uid : toolBlackList) {
              Item blackListItem = GameRegistry.findItem(uid.getResourceDomain(), uid.getResourcePath());
              if(blackListItem == equipped.getItem()) {
                return;
              }
            }
          }

          TileEntityMobSpawner spawner = (TileEntityMobSpawner) tile;
          MobSpawnerBaseLogic logic = spawner.getSpawnerBaseLogic();
          if(logic != null) {
            String entityName = getEntityName(logic);
            if(entityName != null && !isBlackListed(entityName)) {
              ItemStack drop = CapturedMob.create(entityName, tile.getWorld().provider instanceof WorldProviderHell).toStack(EnderIO.itemBrokenSpawner, 0, 1);
              dropCache.put(new BlockCoord(evt.getPos()), drop);

              for (int i = (int) (Math.random() * 7); i > 0; i--) {
                setSpawnDelay(logic);
                logic.updateSpawner();
              }

            }
          }
        }
      } else {
        dropCache.put(new BlockCoord(evt.getPos()), null);
      }
    }
  }

  @SubscribeEvent
  public void onHarvestDropsEvent(BlockEvent.HarvestDropsEvent evt) {
    if (!evt.isCanceled() && evt.getState().getBlock() instanceof BlockMobSpawner) {
      BlockCoord bc = new BlockCoord(evt.getPos());
      if (dropCache.containsKey(bc)) {
        ItemStack stack = dropCache.get(bc);
        if (stack != null) {
          evt.getDrops().add(stack);
        }
      } else {
        // A spawner was broken---but not by a player. The TE has been
        // invalidated already, but we might be able to recover it.
        try {
          for (Object object : evt.getWorld().loadedTileEntityList) {
            if (object instanceof TileEntityMobSpawner) {
              TileEntityMobSpawner spawner = (TileEntityMobSpawner) object;
              BlockPos p = spawner.getPos();
              if (spawner.getWorld() == evt.getWorld() && p.equals(evt.getPos())) {
                // Bingo!
                MobSpawnerBaseLogic logic = spawner.getSpawnerBaseLogic();
                if (logic != null) {
                  String entityName = getEntityName(logic);
                  if (entityName != null && !isBlackListed(entityName)) {
                    evt.getDrops().add(CapturedMob.create(entityName, false).toStack(EnderIO.itemBrokenSpawner, 0, 1));
                  }
                }
              }
            }
          }
        } catch (Exception e) {
          // Risky recovery failed. Happens.
        }
      }
    }
  }

  private String getEntityName(MobSpawnerBaseLogic logic) {    
    if(entNameField != null) {
      try {
        return (String)entNameField.get(logic);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  private void setSpawnDelay(MobSpawnerBaseLogic logic) {
    if (entNameField != null) {
      try {
        spawnDelayField.set(logic, 0);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @SubscribeEvent
  public void onServerTick(TickEvent.ServerTickEvent event) {
    if (event.phase == TickEvent.Phase.END) {
      dropCache.clear();
    }
  }

  @SubscribeEvent
  public void handleAnvilEvent(AnvilUpdateEvent evt) {
    if (evt.getLeft() == null || evt.getLeft().stackSize != 1 || evt.getLeft().getItem() != Item.getItemFromBlock(EnderIO.blockPoweredSpawner)) {
      return;
    }
    if (evt.getRight() == null || evt.getRight().stackSize != 1 || evt.getRight().getItem() != EnderIO.itemBrokenSpawner) {
      return;
    }

    CapturedMob spawnerType = CapturedMob.create(evt.getRight());
    if (spawnerType == null || isBlackListed(spawnerType.getEntityName())) {
      return;
    }

    evt.setCost(Config.powerSpawnerAddSpawnerCost);
    evt.setOutput(evt.getLeft().copy());
    if(evt.getOutput().getTagCompound() == null) {
      evt.getOutput().setTagCompound(new NBTTagCompound());
    }
    evt.getOutput().getTagCompound().setBoolean("eio.abstractMachine", true);
    spawnerType.toNbt(evt.getOutput().getTagCompound());
  }

  @SubscribeEvent
  public void onLivingUpdate(LivingUpdateEvent livingUpdate) {

    Entity ent = livingUpdate.getEntityLiving();
    if(!ent.getEntityData().hasKey(KEY_SPAWNED_BY_POWERED_SPAWNER)) {
      return;
    }
    if(fieldpersistenceRequired == null) {
      ent.getEntityData().removeTag(KEY_SPAWNED_BY_POWERED_SPAWNER);
      return;
    }

    long spawnTime = ent.getEntityData().getLong(KEY_SPAWNED_BY_POWERED_SPAWNER);
    long livedFor = livingUpdate.getEntity().worldObj.getTotalWorldTime() - spawnTime;
    if(livedFor > Config.poweredSpawnerDespawnTimeSeconds*20) {      
      try {
        fieldpersistenceRequired.setBoolean(livingUpdate.getEntityLiving(), false);
        
        ent.getEntityData().removeTag(KEY_SPAWNED_BY_POWERED_SPAWNER);
      } catch (Exception e) {
        Log.warn("BlockPoweredSpawner.onLivingUpdate: Error occured allowing entity to despawn: " + e);
        ent.getEntityData().removeTag(KEY_SPAWNED_BY_POWERED_SPAWNER);
      }
    }
  }

  public boolean isBlackListed(String entityId) {
    return PoweredSpawnerConfig.getInstance().isBlackListed(entityId);
  }

  @Override
  public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
    if(te instanceof TilePoweredSpawner) {
      return new ContainerPoweredSpawner(player.inventory, (TilePoweredSpawner) te);
    }
    return null;
  }

  @Override
  public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
    if(te instanceof TilePoweredSpawner) {
      return new GuiPoweredSpawner(player.inventory, (TilePoweredSpawner) te);
    }
    return null;
  }

  @Override
  protected int getGuiId() {
    return GuiHandler.GUI_ID_POWERED_SPAWNER;
  }

  @Override
  public boolean isOpaqueCube(IBlockState bs) {
    return false;
  }

  @Override
  public void addCommonEntries(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
    CapturedMob mob = CapturedMob.create(itemstack);
    if (mob != null) {
      list.add(mob.getDisplayName());
    } else {
      list.add(EnderIO.lang.localizeExact("tile.blockPoweredSpawner.tooltip.empty"));
    }
  }

  @Override
  public void addBasicEntries(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
  }

  @Override
  public void addDetailedEntries(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
    if (CapturedMob.containsSoul(itemstack)) {
      SpecialTooltipHandler.addDetailedTooltipFromResources(list, "tile.blockPoweredSpawner");
    } else {
      SpecialTooltipHandler.addDetailedTooltipFromResources(list, "tile.blockPoweredSpawner.empty");
    }
  }

  @Override
  public void getWailaInfo(List<String> tooltip, EntityPlayer player, World world, int x, int y, int z) {
    TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
    if (tileEntity instanceof TilePoweredSpawner) {
      CapturedMob capturedMob = ((TilePoweredSpawner) tileEntity).getEntity();
      if (capturedMob != null) {
        tooltip.add(capturedMob.getDisplayName());
      }
    }
  }

  @Override
  public int getDefaultDisplayMask(World world, int x, int y, int z) {
    return IWailaInfoProvider.BIT_DETAILED;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list) {
    super.getSubBlocks(item, tab, list);
    list.add(CapturedMob.create("Enderman", false).toStack(item, 0, 1));
    list.add(CapturedMob.create("Chicken", false).toStack(item, 0, 1));
    list.add(CapturedMob.create("Skeleton", false).toStack(item, 0, 1));
    list.add(CapturedMob.create("Skeleton", true).toStack(item, 0, 1));
  }

  @Override
  @SideOnly(Side.CLIENT)
  public IItemRenderMapper getItemRenderMapper() {
    return RenderMappers.FRONT_MAPPER;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public IRenderMapper.IBlockRenderMapper getBlockRenderMapper() {
    return RenderMappers.FRONT_MAPPER;
  }

  @Override
  protected void setBlockStateWrapperCache(@Nonnull IBlockStateWrapper blockStateWrapper, @Nonnull IBlockAccess world, @Nonnull BlockPos pos,
      @Nonnull TilePoweredSpawner tileEntity) {
    blockStateWrapper.addCacheKey(tileEntity.getFacing()).addCacheKey(tileEntity.isActive());
  }

}
