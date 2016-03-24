package crazypants.enderio.conduit.render;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import com.enderio.core.client.render.BoundingBox;
import com.enderio.core.client.render.RenderUtil;
import com.enderio.core.common.util.BlockCoord;

import crazypants.enderio.conduit.ConduitDisplayMode;
import crazypants.enderio.conduit.ConduitUtil;
import crazypants.enderio.conduit.ConnectionMode;
import crazypants.enderio.conduit.IConduit;
import crazypants.enderio.conduit.IConduitBundle;
import crazypants.enderio.conduit.TileConduitBundle;
import crazypants.enderio.conduit.geom.CollidableComponent;
import crazypants.enderio.conduit.geom.ConduitConnectorType;
import crazypants.enderio.conduit.geom.ConduitGeometryUtil;
import crazypants.enderio.config.Config;
import crazypants.enderio.render.IBlockStateWrapper;

@SideOnly(Side.CLIENT)
public class ConduitBundleRenderer extends TileEntitySpecialRenderer<TileConduitBundle> {

  private final List<ConduitRenderer> conduitRenderers = new ArrayList<ConduitRenderer>();
  private final DefaultConduitRenderer dcr = new DefaultConduitRenderer();

  public ConduitBundleRenderer() {    
  }
  
  public void registerRenderer(ConduitRenderer renderer) {
    conduitRenderers.add(renderer);
  }

  // TESR rendering

  @Override
  public void renderTileEntityAt(TileConduitBundle te, double x, double y, double z, float partialTick, int b) {
    

    IConduitBundle bundle = te;
    EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
    if (bundle.hasFacade() && bundle.getPaintSource().getBlock().isOpaqueCube() && !ConduitUtil.isFacadeHidden(bundle, player)) {
      return;
    }
    float brightness = -1;
    boolean hasDynamic = false;
    for (IConduit con : bundle.getConduits()) {
      if (ConduitUtil.renderConduit(player, con)) {
        ConduitRenderer renderer = getRendererForConduit(con);
        if (renderer.isDynamic()) {
          if (!hasDynamic) {
            hasDynamic = true;
            BlockCoord loc = bundle.getLocation();
            brightness = bundle.getEntity().getWorld().getLightFor(EnumSkyBlock.SKY, loc.getBlockPos());

            RenderUtil.setupLightmapCoords(te.getPos(), te.getWorld());            
            RenderUtil.bindBlockTexture();
            GlStateManager.enableNormalize();
            GlStateManager.enableBlend();            
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.shadeModel(GL11.GL_SMOOTH);

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);

            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer tes = tessellator.getWorldRenderer();
            tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            
          }
          renderer.renderDynamicEntity(this, bundle, con, x, y, z, partialTick, brightness);

        }
      }
    }

    if (hasDynamic) {
      Tessellator.getInstance().draw();
      GlStateManager.disableNormalize();
      GlStateManager.disableBlend();      
      GlStateManager.shadeModel(GL11.GL_FLAT);
      GlStateManager.popMatrix();
    }
  }

  // ------------ Block Model building

  public List<BakedQuad> getGeneralQuads(IBlockStateWrapper state) {

    EnumWorldBlockLayer layer = MinecraftForgeClient.getRenderLayer();
    if(layer != EnumWorldBlockLayer.CUTOUT) {
      return Collections.emptyList();
    }

    List<BakedQuad> result = new ArrayList<BakedQuad>();
    IConduitBundle bundle = (IConduitBundle) state.getTileEntity();
    float brightness;
    if (!Config.updateLightingWhenHidingFacades && bundle.hasFacade()) {
      brightness = 15 << 20 | 15 << 4;
    } else {
      brightness = bundle.getEntity().getWorld().getLightFor(EnumSkyBlock.SKY, bundle.getLocation().getBlockPos());
    }
    renderConduits(bundle, brightness, result);

    return result;
  }

  public void renderConduits(IConduitBundle bundle, float brightness, List<BakedQuad> quads) {

    // Conduits
    Set<EnumFacing> externals = new HashSet<EnumFacing>();
    EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

    List<BoundingBox> wireBounds = new ArrayList<BoundingBox>();

    for (IConduit con : bundle.getConduits()) {

      if (ConduitUtil.renderConduit(player, con)) {
        ConduitRenderer renderer = getRendererForConduit(con);
        renderer.addBakedQuads(this, bundle, con, brightness, quads);
        Set<EnumFacing> extCons = con.getExternalConnections();
        for (EnumFacing dir : extCons) {
          if (con.getConnectionMode(dir) != ConnectionMode.DISABLED && con.getConnectionMode(dir) != ConnectionMode.NOT_SET) {
            externals.add(dir);
          }
        }
      } else if (con != null) {
        Collection<CollidableComponent> components = con.getCollidableComponents();
        for (CollidableComponent component : components) {
          addWireBounds(wireBounds, component);
        }
      }
    }

    // Internal conectors between conduits
    List<CollidableComponent> connectors = bundle.getConnectors();    
    for (CollidableComponent component : connectors) {
      if (component.conduitType != null) {
        IConduit conduit = bundle.getConduit(component.conduitType);
        if (conduit != null) {
          if (ConduitUtil.renderConduit(player, component.conduitType)) {                   
            BakedQuadBuilder.addBakedQuads(quads, component.bound, conduit.getTextureForState(component));
          } else {
            addWireBounds(wireBounds, component);
          }
        }

      } else if (ConduitUtil.getDisplayMode(player) == ConduitDisplayMode.ALL) {
        TextureAtlasSprite tex = ConduitBundleRenderManager.instance.getConnectorIcon(component.data);
        BakedQuadBuilder.addBakedQuads(quads, component.bound, tex);
      }
    }
    
    // render these after the 'normal' conduits so help with proper blending
    for (BoundingBox wireBound : wireBounds) {
      BakedQuadBuilder.addBakedQuads(quads, wireBound, ConduitBundleRenderManager.instance.getWireFrameIcon());
    }

    // External connection terminations
    for (EnumFacing dir : externals) {
      addQuadsForExternalConnection(dir, quads);
    }

  }

  private void addWireBounds(List<BoundingBox> wireBounds, CollidableComponent component) {
    if(component.dir != null) {              
      double sx = component.dir.getFrontOffsetX() != 0 ? 1 : 0.7;
      double sy = component.dir.getFrontOffsetY() != 0 ? 1 : 0.7;
      double sz = component.dir.getFrontOffsetZ() != 0 ? 1 : 0.7;                            
      wireBounds.add(component.bound.scale(sx, sy, sz));
    } else {
      wireBounds.add(component.bound);
    }
  }

  private void addQuadsForExternalConnection(EnumFacing dir, List<BakedQuad> quads) {
    TextureAtlasSprite tex = ConduitBundleRenderManager.instance.getConnectorIcon(ConduitConnectorType.EXTERNAL);
    BoundingBox[] bbs = ConduitGeometryUtil.instance.getExternalConnectorBoundingBoxes(dir);
    for (BoundingBox bb : bbs) {
      BakedQuadBuilder.addBakedQuads(quads, bb, tex);
    }
  }

  public ConduitRenderer getRendererForConduit(IConduit conduit) {
    for (ConduitRenderer renderer : conduitRenderers) {
      if (renderer.isRendererForConduit(conduit)) {
        return renderer;
      }
    }
    return dcr;
  }

}
