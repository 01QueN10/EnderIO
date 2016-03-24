package crazypants.enderio.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;

import org.apache.commons.lang3.tuple.Pair;

import crazypants.enderio.render.pipeline.QuadCollector;

import static crazypants.enderio.render.EnumMergingBlockRenderMode.RENDER;

public abstract class ConnectedBlockRenderMapper implements IRenderMapper {

  protected boolean skip_top = false, skip_bottom = false, skip_side = false, skip_top_side = false, skip_bottom_side = false;

  protected abstract List<IBlockState> renderBody(IBlockStateWrapper state, IBlockAccess world, BlockPos pos, EnumWorldBlockLayer blockLayer,
      QuadCollector quadCollector);

  protected abstract boolean isSameKind(IBlockState state, IBlockState other);

  protected abstract IBlockState getMergedBlockstate(IBlockState state);

  protected abstract IBlockState getBorderedBlockstate(IBlockState state);

  private final boolean[][][] neighborKinds = new boolean[3][3][3];

  public ConnectedBlockRenderMapper(IBlockState state, IBlockAccess world, BlockPos pos) {
    for (int dx = -1; dx <= 1; dx++) {
      for (int dy = -1; dy <= 1; dy++) {
        for (int dz = -1; dz <= 1; dz++) {
          BlockPos npos = pos.add(dx, dy, dz);
          setNeighbor(pos, npos, isSameKind(state, world, pos, npos));
        }
      }
    }
  }

  protected boolean isSameKind(IBlockState state, IBlockAccess world, BlockPos pos, BlockPos other) {
    return isSameKind(state, world.getBlockState(other));
  }

  protected boolean getNeighbor(BlockPos pos, BlockPos npos) {
    int x = cmp(pos.getX(), npos.getX());
    int y = cmp(pos.getY(), npos.getY());
    int z = cmp(pos.getZ(), npos.getZ());
    return neighborKinds[x][y][z];
  }

  protected int cmp(int a, int b) {
    return a < b ? 0 : a > b ? 2 : 1;
  }

  protected void setNeighbor(BlockPos pos, BlockPos npos, boolean value) {
    int x = cmp(pos.getX(), npos.getX());
    int y = cmp(pos.getY(), npos.getY());
    int z = cmp(pos.getZ(), npos.getZ());
    neighborKinds[x][y][z] = value;
  }

  @Override
  public int hashCode() {
    // Note: java.util.Arrays.deepHashCode() will NOT give good results here.
    int deepHashCode = 0;
    for (int x = 0; x < 3; x++) {
      for (int y = 0; y < 3; y++) {
        for (int z = 0; z < 3; z++) {
          deepHashCode = (deepHashCode | (neighborKinds[x][y][z] ? 1 : 0)) << 1;
        }
      }
    }
    return deepHashCode;
  }


  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ConnectedBlockRenderMapper other = (ConnectedBlockRenderMapper) obj;
    return Arrays.deepEquals(neighborKinds, other.neighborKinds);
  }

  @Override
  public List<IBlockState> mapBlockRender(IBlockStateWrapper state, IBlockAccess world, BlockPos pos, EnumWorldBlockLayer blockLayer,
      QuadCollector quadCollector) {
    List<IBlockState> states = renderBody(state, world, pos, blockLayer, quadCollector);

    if (states == null) {
      states = new ArrayList<IBlockState>();
    }

    IBlockState stateMerged = getMergedBlockstate(state.getState());
    IBlockState stateBordered = getBorderedBlockstate(state.getState());

    // For each of the 4 sides we add the top, bottom and right edge as well as the two corner between those. That are all
    // edges and corners---a cube has 6 sides, 2*4 corners and 3*4 edges.

    boolean block_up = getNeighbor(pos, pos.offset(EnumFacing.UP));
    boolean block_down = getNeighbor(pos, pos.offset(EnumFacing.DOWN));
    for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
      boolean block_facing = getNeighbor(pos, pos.offset(facing));
      boolean block_right = getNeighbor(pos, pos.offset(facing.rotateYCCW()));
      boolean block_up_facing = getNeighbor(pos, pos.up().offset(facing));
      boolean block_up_right = getNeighbor(pos, pos.up().offset(facing.rotateYCCW()));
      boolean block_right_facing = getNeighbor(pos, pos.offset(facing).offset(facing.rotateYCCW()));
      boolean block_down_facing = getNeighbor(pos, pos.down().offset(facing));
      boolean block_down_right = getNeighbor(pos, pos.down().offset(facing.rotateYCCW()));
      boolean block_up_right_facing = getNeighbor(pos, pos.up().offset(facing.rotateYCCW()).offset(facing));
      boolean block_down_right_facing = getNeighbor(pos, pos.down().offset(facing.rotateYCCW()).offset(facing));

      // Our Edges //
      // ///////// //

      boolean upper_edge = hasEdge(block_up, block_facing);
      boolean lower_edge = hasEdge(block_down, block_facing);
      boolean right_edge = hasEdge(block_right, block_facing);

      add(states, skip_top, upper_edge, stateBordered, stateMerged, RENDER, EnumMergingBlockRenderMode.get(facing, EnumFacing.UP));
      add(states, skip_bottom, lower_edge, stateBordered, stateMerged, RENDER, EnumMergingBlockRenderMode.get(facing, EnumFacing.DOWN));
      add(states, skip_side, right_edge, stateBordered, stateMerged, RENDER, EnumMergingBlockRenderMode.get(facing, facing.rotateYCCW()));

      // Our Corners //
      // /////////// //

      // The 3 edges that connect to the corners and belong to us. Two of them are checked above, this is the third one:

      boolean upper_edge_around_right_corner = hasEdge(block_up, block_right);
      boolean lower_edge_around_right_corner = hasEdge(block_down, block_right);

      // The 3 edges that connect to the corners but belong to our neighbors:

      boolean right_edge_above = hasEdge(block_up, block_up_right, block_up_facing);
      boolean right_edge_below = hasEdge(block_down, block_down_right, block_down_facing);

      boolean top_facing_edge_right = hasEdge(block_right, block_up_right, block_right_facing);
      boolean bottom_facing_edge_right = hasEdge(block_right, block_down_right, block_right_facing);

      boolean facing_top_edge_right = hasEdge(block_facing, block_right_facing, block_up_facing);
      boolean facing_bottom_edge_right = hasEdge(block_facing, block_right_facing, block_down_facing);

      // The pathological case of having blocks over edge connecting to a common base. This adds the corner where the 2 edges meet:

      boolean checker_top_a = hasEdge(block_up_facing, block_facing, block_up_right_facing) //
          && hasEdge(block_right_facing, block_facing, block_up_right_facing);
      boolean checker_top_b = hasEdge(block_up_right, block_right, block_up_right_facing) //
          && hasEdge(block_right_facing, block_right, block_up_right_facing);
      boolean checker_top_c = hasEdge(block_up_facing, block_up, block_up_right_facing) //
          && hasEdge(block_up_right, block_up, block_up_right_facing);
      boolean checker_bottom_a = hasEdge(block_down_facing, block_facing, block_down_right_facing) //
          && hasEdge(block_right_facing, block_facing, block_down_right_facing);
      boolean checker_bottom_b = hasEdge(block_down_right, block_right, block_down_right_facing) //
          && hasEdge(block_right_facing, block_right, block_down_right_facing);
      boolean checker_bottom_c = hasEdge(block_down_facing, block_down, block_down_right_facing) //
          && hasEdge(block_down_right, block_down, block_down_right_facing);

      boolean upper_right_corner = upper_edge || right_edge || upper_edge_around_right_corner || right_edge_above || top_facing_edge_right
          || facing_top_edge_right || checker_top_a || checker_top_b || checker_top_c;
      boolean lower_right_corner = lower_edge || right_edge || lower_edge_around_right_corner || right_edge_below || bottom_facing_edge_right
          || facing_bottom_edge_right || checker_bottom_a || checker_bottom_b || checker_bottom_c;

      add(states, skip_top_side, upper_right_corner, stateBordered, stateMerged, RENDER,
          EnumMergingBlockRenderMode.get(facing, facing.rotateYCCW(), EnumFacing.UP));
      add(states, skip_bottom_side, lower_right_corner, stateBordered, stateMerged, RENDER,
          EnumMergingBlockRenderMode.get(facing, facing.rotateYCCW(), EnumFacing.DOWN));
    }

    return states;
  }

  protected boolean hasEdge(boolean base, boolean dir1, boolean dir2) {
    return base && !dir1 && !dir2;
  }

  protected boolean hasEdge(boolean dir1, boolean dir2) {
    return !dir1 && !dir2;
  }

  protected <T extends Comparable<T>, V extends T> void add(List<IBlockState> states, boolean skip, boolean border, IBlockState stateBordered,
      IBlockState stateMerged, IProperty<T> property, V value) {
    IBlockState state = border ? stateBordered : stateMerged;
    if (!skip && state != null) {
      states.add(state.withProperty(property, value));
    }
  }

  @Override
  public Pair<List<IBlockState>, List<IBakedModel>> mapItemRender(Block block, ItemStack stack) {
    return null;
  }

  @Override
  public Pair<List<IBlockState>, List<IBakedModel>> mapItemPaintOverlayRender(Block block, ItemStack stack) {
    return null;
  }

}
