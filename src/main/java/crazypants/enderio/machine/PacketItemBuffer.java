package crazypants.enderio.machine;

import com.enderio.core.common.util.BlockCoord;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketItemBuffer implements IMessage, IMessageHandler<PacketItemBuffer, IMessage> {

  private int x;
  private int y;
  private int z;
  boolean bufferStacks;

  public PacketItemBuffer() {
  }

  public PacketItemBuffer(IItemBuffer buffer) {
    BlockCoord bc = buffer.getLocation();
    x = bc.x;
    y = bc.y;
    z = bc.z;
    bufferStacks = buffer.isBufferStacks();
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeInt(x);
    buf.writeInt(y);
    buf.writeInt(z);
    buf.writeBoolean(bufferStacks);
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    x = buf.readInt();
    y = buf.readInt();
    z = buf.readInt();
    bufferStacks = buf.readBoolean();
  }

  @Override
  public IMessage onMessage(PacketItemBuffer message, MessageContext ctx) {
    EntityPlayer player = ctx.getServerHandler().playerEntity;
    TileEntity te = player.worldObj.getTileEntity(new BlockPos(message.x, message.y, message.z));
    if(te instanceof IItemBuffer) {
      ((IItemBuffer)te).setBufferStacks(message.bufferStacks);
    }    
    return null;
  }

}
