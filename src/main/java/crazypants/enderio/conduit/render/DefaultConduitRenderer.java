package crazypants.enderio.conduit.render;

import java.util.Collection;
import java.util.List;

import com.enderio.core.api.client.render.VertexTransform;
import com.enderio.core.client.render.BoundingBox;
import com.enderio.core.client.render.IconUtil;
import com.enderio.core.client.render.RenderUtil;
import com.enderio.core.common.vecmath.Vector3d;
import com.enderio.core.common.vecmath.Vector4f;
import com.enderio.core.common.vecmath.Vertex;

import static net.minecraft.util.EnumFacing.DOWN;
import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.NORTH;
import static net.minecraft.util.EnumFacing.SOUTH;
import static net.minecraft.util.EnumFacing.UP;
import static net.minecraft.util.EnumFacing.WEST;

import crazypants.enderio.conduit.ConnectionMode;
import crazypants.enderio.conduit.IConduit;
import crazypants.enderio.conduit.IConduitBundle;
import crazypants.enderio.conduit.geom.CollidableComponent;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;

public class DefaultConduitRenderer implements ConduitRenderer {

  static final Vector3d[] verts = new Vector3d[8];

  static {
    for (int i = 0; i < verts.length; i++) {
      verts[i] = new Vector3d();
    }
  }

  protected float transmissionScaleFactor;

  @Override
  public boolean isRendererForConduit(IConduit conduit) {
    return true;
  }

  protected boolean renderComponent(CollidableComponent component) {
    return true;
  }

  // ------------ Static Model ---------------------------------------------

  @Override
  public void addBakedQuads(ConduitBundleRenderer conduitBundleRenderer, IConduitBundle bundle, IConduit conduit, float brightness, List<BakedQuad> quads) {

    Collection<CollidableComponent> components = conduit.getCollidableComponents();
    transmissionScaleFactor = conduit.getTransmitionGeometryScale();
    for (CollidableComponent component : components) {
      if (renderComponent(component)) {
        float selfIllum = Math.max(brightness, conduit.getSelfIlluminationForState(component));
        if (isNSEWUD(component.dir) && conduit.getTransmitionTextureForState(component) != null) {
          TextureAtlasSprite tex = conduit.getTransmitionTextureForState(component);
          if (tex == null) {
            tex = IconUtil.instance.errorTexture;
          }
          addTransmissionQuads(tex, conduit, component, selfIllum, quads);
        }
        TextureAtlasSprite tex = conduit.getTextureForState(component);
        addConduitQuads(bundle, conduit, tex, component, selfIllum, quads);
      }
    }
  }

  protected void addConduitQuads(IConduitBundle bundle, IConduit conduit, TextureAtlasSprite tex, CollidableComponent component, float selfIllum, List<BakedQuad> quads) {
  
    if (isNSEWUD(component.dir)) {

      float scaleFactor = 0.75f;
      float xLen = Math.abs(component.dir.getFrontOffsetX()) == 1 ? 1 : scaleFactor;
      float yLen = Math.abs(component.dir.getFrontOffsetY()) == 1 ? 1 : scaleFactor;
      float zLen = Math.abs(component.dir.getFrontOffsetZ()) == 1 ? 1 : scaleFactor;

      BoundingBox cube = component.bound;
      BoundingBox bb = cube.scale(xLen, yLen, zLen);
      addQuadsForSection(bb, tex, component.dir, quads);
      if (conduit.getConnectionMode(component.dir) == ConnectionMode.DISABLED) {
        tex = ConduitBundleRenderManager.instance.getConnectorIcon(component.data);
        BakedQuadBuilder.addBakedQuadForFace(quads, bb, tex, component.dir);        
      }
    } else {
      BakedQuadBuilder.addBakedQuads(quads, component.bound, tex);
    }
  }

  protected void addQuadsForSection(BoundingBox bb, TextureAtlasSprite tex, EnumFacing dir, List<BakedQuad> quads) {
    addQuadsForSection(bb, tex, dir, quads, null);
  }

  protected void addQuadsForSection(BoundingBox bb, TextureAtlasSprite tex, EnumFacing dir, List<BakedQuad> quads, Vector4f color) {

    boolean rotateSides = dir == UP || dir == DOWN;
    boolean rotateTopBottom = dir == DOWN || dir == EAST || dir == EnumFacing.SOUTH;

    boolean doRotSides = rotateSides;
    for (EnumFacing face : EnumFacing.VALUES) {
      if (face != dir && face.getOpposite() != dir) {
        if (face == UP || face == DOWN) {
          doRotSides = dir == SOUTH || dir == NORTH;
        } else {
          doRotSides = rotateSides;
        }
        BakedQuadBuilder.addBakedQuadForFace(quads, bb, tex, face, doRotSides, rotateTopBottom, color);
      }
    }
  }

  protected void addTransmissionQuads(TextureAtlasSprite tex, IConduit conduit, CollidableComponent component, float selfIllum, List<BakedQuad> quads) {
        
    float scaleFactor = 0.6f;
    float xLen = Math.abs(component.dir.getFrontOffsetX()) == 1 ? 1 : scaleFactor;
    float yLen = Math.abs(component.dir.getFrontOffsetY()) == 1 ? 1 : scaleFactor;
    float zLen = Math.abs(component.dir.getFrontOffsetZ()) == 1 ? 1 : scaleFactor;

    BoundingBox cube = component.bound;
    BoundingBox bb = cube.scale(xLen, yLen, zLen);
    addQuadsForSection(bb, tex, component.dir, quads);
  }
  
  

  // ------------ Dynamic ---------------------------------------------

  @Override
  public void renderDynamicEntity(ConduitBundleRenderer conduitBundleRenderer, IConduitBundle te, IConduit conduit, double x, double y, double z, float partialTick,
      float worldLight) {
        
    Collection<CollidableComponent> components = conduit.getCollidableComponents();
    transmissionScaleFactor = conduit.getTransmitionGeometryScale();
    for (CollidableComponent component : components) {
      if (renderComponent(component)) {
        float selfIllum = Math.max(worldLight, conduit.getSelfIlluminationForState(component));
        TextureAtlasSprite tex;
        if (isNSEWUD(component.dir) && conduit.getTransmitionTextureForState(component) != null) {
          tex = conduit.getTransmitionTextureForState(component);
          if (tex == null) {
            tex = IconUtil.instance.errorTexture;
          }          
          renderTransmissionDynamic(conduit, tex, component, selfIllum);
        } 
        tex = conduit.getTextureForState(component);        
        renderConduitDynamic(tex, conduit, component, selfIllum);
      }
    }
  }

  protected void renderConduitDynamic(TextureAtlasSprite tex, IConduit conduit, CollidableComponent component, float brightness) {
    GlStateManager.color(1, 1, 1);
    if (isNSEWUD(component.dir)) {
      float scaleFactor = 0.75f;
      float xLen = Math.abs(component.dir.getFrontOffsetX()) == 1 ? 1 : scaleFactor;
      float yLen = Math.abs(component.dir.getFrontOffsetY()) == 1 ? 1 : scaleFactor;
      float zLen = Math.abs(component.dir.getFrontOffsetZ()) == 1 ? 1 : scaleFactor;

      BoundingBox cube = component.bound;
      BoundingBox bb = cube.scale(xLen, yLen, zLen);
      drawDynamicSection(bb, tex.getMinU(), tex.getMaxU(), tex.getMinV(), tex.getMaxV(), component.dir, false, conduit.shouldMirrorTexture());
      if (conduit.getConnectionMode(component.dir) == ConnectionMode.DISABLED) {
        tex = ConduitBundleRenderManager.instance.getConnectorIcon(component.data);
        List<Vertex> corners = component.bound.getCornersWithUvForFace(component.dir, tex.getMinU(), tex.getMaxU(), tex.getMinV(), tex.getMaxV());
        RenderUtil.addVerticesToTessellator(corners, DefaultVertexFormats.POSITION_TEX, false);
      }
    } else {
      drawDynamicSection(component.bound, tex.getMinU(), tex.getMaxU(), tex.getMinV(), tex.getMaxV(), component.dir, true);
    }

  }

  protected void renderTransmissionDynamic(IConduit conduit, TextureAtlasSprite tex, CollidableComponent component, float selfIllum) {
    float scaleFactor = 0.6f;
    float xLen = Math.abs(component.dir.getFrontOffsetX()) == 1 ? 1 : scaleFactor;
    float yLen = Math.abs(component.dir.getFrontOffsetY()) == 1 ? 1 : scaleFactor;
    float zLen = Math.abs(component.dir.getFrontOffsetZ()) == 1 ? 1 : scaleFactor;

    GlStateManager.color(1, 1, 1);
    BoundingBox cube = component.bound;
    BoundingBox bb = cube.scale(xLen, yLen, zLen);
    drawDynamicSection(bb, tex.getMinU(), tex.getMaxU(), tex.getMinV(), tex.getMaxV(), component.dir, false);
  }

  protected boolean isNSEWUD(EnumFacing dir) {
    return dir != null;
  }

  protected void drawDynamicSection(BoundingBox bound, float minU, float maxU, float minV, float maxV, EnumFacing dir, boolean isTransmission) {
    drawDynamicSection(bound, minU, maxU, minV, maxV, dir, isTransmission, true);
  }

  protected void drawDynamicSection(BoundingBox bound, float minU, float maxU, float minV, float maxV, EnumFacing dir, boolean isTransmission,
      boolean mirrorTexture) {

    if (isTransmission) {
      setVerticesForTransmission(bound, dir);
    } else {
      setupVertices(bound);
    }

    if (mirrorTexture && (dir == EnumFacing.NORTH || dir == UP || dir == EAST)) {
      // maintain consistent texture dir relative to the center of the conduit
      float tmp = minU;
      minU = maxU;
      maxU = tmp;
    }

    boolean rotateSides = dir == UP || dir == DOWN;
    boolean rotateTopBottom = dir == NORTH || dir == SOUTH;
    // float cm;
    if (dir != NORTH && dir != SOUTH) {
      // tessellator.setNormal(0, 0, -1);
      if (!isTransmission) {
        // cm = RenderUtil.getColorMultiplierForFace(EnumFacing.NORTH);
        // tessellator.setColorOpaque_F(cm, cm, cm);
      }
      if (rotateSides) {
        addVecWithUV(verts[1], maxU, maxV);
        addVecWithUV(verts[0], maxU, minV);
        addVecWithUV(verts[3], minU, minV);
        addVecWithUV(verts[2], minU, maxV);
      } else {
        addVecWithUV(verts[1], minU, minV);
        addVecWithUV(verts[0], maxU, minV);
        addVecWithUV(verts[3], maxU, maxV);
        addVecWithUV(verts[2], minU, maxV);
      }
      if (dir == WEST || dir == EAST) {
        float tmp = minU;
        minU = maxU;
        maxU = tmp;
      }
      // tessellator.setNormal(0, 0, 1);
      if (!isTransmission) {
        // cm = RenderUtil.getColorMultiplierForFace(EnumFacing.SOUTH);
        // tessellator.setColorOpaque_F(cm, cm, cm);
      }
      if (rotateSides) {
        addVecWithUV(verts[4], maxU, maxV);
        addVecWithUV(verts[5], maxU, minV);
        addVecWithUV(verts[6], minU, minV);
        addVecWithUV(verts[7], minU, maxV);
      } else {
        addVecWithUV(verts[4], minU, minV);
        addVecWithUV(verts[5], maxU, minV);
        addVecWithUV(verts[6], maxU, maxV);
        addVecWithUV(verts[7], minU, maxV);
      }
      if (dir == WEST || dir == EAST) {
        float tmp = minU;
        minU = maxU;
        maxU = tmp;
      }
    }

    if (dir != UP && dir != DOWN) {

      // tessellator.setNormal(0, 1, 0);
      if (!isTransmission) {
        // cm = RenderUtil.getColorMultiplierForFace(EnumFacing.UP);
        // tessellator.setColorOpaque_F(cm, cm, cm);
      }
      if (rotateTopBottom) {
        addVecWithUV(verts[6], maxU, maxV);
        addVecWithUV(verts[2], minU, maxV);
        addVecWithUV(verts[3], minU, minV);
        addVecWithUV(verts[7], maxU, minV);
      } else {
        addVecWithUV(verts[6], minU, minV);
        addVecWithUV(verts[2], minU, maxV);
        addVecWithUV(verts[3], maxU, maxV);
        addVecWithUV(verts[7], maxU, minV);
      }

      // tessellator.setNormal(0, -1, 0);
      if (!isTransmission) {
        // cm = RenderUtil.getColorMultiplierForFace(EnumFacing.DOWN);
        // tessellator.setColorOpaque_F(cm, cm, cm);
      }
      if (rotateTopBottom) {
        addVecWithUV(verts[0], minU, minV);
        addVecWithUV(verts[1], minU, maxV);
        addVecWithUV(verts[5], maxU, maxV);
        addVecWithUV(verts[4], maxU, minV);
      } else {
        addVecWithUV(verts[0], maxU, maxV);
        addVecWithUV(verts[1], minU, maxV);
        addVecWithUV(verts[5], minU, minV);
        addVecWithUV(verts[4], maxU, minV);
      }
    }

    if (dir != EAST && dir != WEST) {

      // tessellator.setNormal(1, 0, 0);
      if (!isTransmission) {
        // cm = RenderUtil.getColorMultiplierForFace(EnumFacing.EAST);
        // tessellator.setColorOpaque_F(cm, cm, cm);
      }
      if (rotateSides) {
        addVecWithUV(verts[2], minU, maxV);
        addVecWithUV(verts[6], minU, minV);
        addVecWithUV(verts[5], maxU, minV);
        addVecWithUV(verts[1], maxU, maxV);
      } else {
        addVecWithUV(verts[2], minU, maxV);
        addVecWithUV(verts[6], maxU, maxV);
        addVecWithUV(verts[5], maxU, minV);
        addVecWithUV(verts[1], minU, minV);
      }

      // tessellator.setNormal(-1, 0, 0);
      if (!isTransmission) {
        // cm = RenderUtil.getColorMultiplierForFace(EnumFacing.WEST);
        // tessellator.setColorOpaque_F(cm, cm, cm);
      }
      if (rotateSides) {
        addVecWithUV(verts[0], maxU, maxV);
        addVecWithUV(verts[4], maxU, minV);
        addVecWithUV(verts[7], minU, minV);
        addVecWithUV(verts[3], minU, maxV);
      } else {
        addVecWithUV(verts[0], minU, minV);
        addVecWithUV(verts[4], maxU, minV);
        addVecWithUV(verts[7], maxU, maxV);
        addVecWithUV(verts[3], minU, maxV);
      }
    }
    // tessellator.setColorOpaque_F(1, 1, 1);
  }

  // TODO: This is a really hacky, imprecise and slow way to do this
  public BoundingBox[] toCubes(BoundingBox bb) {

    // NB This on handles the really simple conduit case!

    float width = bb.maxX - bb.minX;
    float height = bb.maxY - bb.minY;
    float depth = bb.maxZ - bb.minZ;

    if (width > 0 && height > 0 && depth > 0) {
      if (width / depth > 1.5f || depth / width > 1.5f) {
        // split horizontally
        if (width > depth) {
          int numSplits = Math.round(width / depth);
          float newWidth = width / numSplits;
          BoundingBox[] result = new BoundingBox[numSplits];
          float lastMax = bb.minX;
          for (int i = 0; i < numSplits; i++) {
            float max = lastMax + newWidth;
            result[i] = new BoundingBox(lastMax, bb.minY, bb.minZ, max, bb.maxY, bb.maxZ);
            lastMax = max;
          }
          return result;

        } else {

          int numSplits = Math.round(depth / width);
          float newWidth = depth / numSplits;
          BoundingBox[] result = new BoundingBox[numSplits];
          float lastMax = bb.minZ;
          for (int i = 0; i < numSplits; i++) {
            float max = lastMax + newWidth;
            result[i] = new BoundingBox(bb.minX, bb.minY, lastMax, bb.maxX, bb.maxY, max);
            lastMax = max;
          }
          return result;

        }

      } else if (height / width > 1.5) {

        int numSplits = Math.round(height / width);
        float newWidth = height / numSplits;
        BoundingBox[] result = new BoundingBox[numSplits];
        float lastMax = bb.minY;
        for (int i = 0; i < numSplits; i++) {
          float max = lastMax + newWidth;
          result[i] = new BoundingBox(bb.minX, lastMax, bb.minZ, bb.maxX, max, bb.maxZ);
          lastMax = max;
        }
        return result;

      }
    }

    return new BoundingBox[] { bb };
  }

  @Override
  public boolean isDynamic() {
    return false;
  }

  protected void setVerticesForTransmission(BoundingBox bound, EnumFacing dir) {
    float xs = dir.getFrontOffsetX() == 0 ? transmissionScaleFactor : 1;
    float ys = dir.getFrontOffsetY() == 0 ? transmissionScaleFactor : 1;
    float zs = dir.getFrontOffsetZ() == 0 ? transmissionScaleFactor : 1;
    setupVertices(bound.scale(xs, ys, zs));
  }

  protected void addVecWithUV(Vector3d vec, double u, double v) {
    WorldRenderer tes = Tessellator.getInstance().getWorldRenderer();
    tes.pos(vec.x, vec.y, vec.z).tex(u, v).endVertex();
  }

  protected void setupVertices(BoundingBox bound) {
    setupVertices(bound, null);
  }

  protected void setupVertices(BoundingBox bound, VertexTransform xForm) {
    verts[0].set(bound.minX, bound.minY, bound.minZ);
    verts[1].set(bound.maxX, bound.minY, bound.minZ);
    verts[2].set(bound.maxX, bound.maxY, bound.minZ);
    verts[3].set(bound.minX, bound.maxY, bound.minZ);
    verts[4].set(bound.minX, bound.minY, bound.maxZ);
    verts[5].set(bound.maxX, bound.minY, bound.maxZ);
    verts[6].set(bound.maxX, bound.maxY, bound.maxZ);
    verts[7].set(bound.minX, bound.maxY, bound.maxZ);

    if (xForm != null) {
      for (Vector3d vec : verts) {
        xForm.apply(vec);
      }
    }
  }

}
