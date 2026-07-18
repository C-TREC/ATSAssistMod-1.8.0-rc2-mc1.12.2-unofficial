package jp.kaiz.atsassistmod.block.tileentity;

import jp.kaiz.atsassistmod.ATSAssistCore;
import jp.ngt.ngtlib.event.TickProcessQueue;
import jp.ngt.ngtlib.network.PacketNBT;
import jp.ngt.rtm.RTMItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityCustom extends TileEntity implements ITickable {
    private static final double MIN_BEAM_RENDER_DISTANCE_BLOCKS = 16.0D;

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        final World world = this.getWorld();
        TickProcessQueue.getInstance(Side.SERVER).add(world1 -> {
            TileEntityCustom.this.sendPacket();
            return true;
        }, 40);//遅延させると届く、下のはレールで使えないため
        return new SPacketUpdateTileEntity(this.pos, -1, this.getUpdateTag());//null返すと届かない？
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

    protected void sendPacket() {
        if (this.world == null || !this.world.isRemote) {
            PacketNBT.sendToClient(this);
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean isRenderBeam() {
        EntityPlayer player = ATSAssistCore.proxy.getPlayer();
        return player != null
                && this.isHoldingCrowbar(player)
                && this.isInVisibleChunkRange(player);
    }

    @SideOnly(Side.CLIENT)
    private boolean isHoldingCrowbar(EntityPlayer player) {
        return this.isCrowbar(player.getHeldItem(EnumHand.MAIN_HAND)) || this.isCrowbar(player.getHeldItem(EnumHand.OFF_HAND));
    }

    @SideOnly(Side.CLIENT)
    private boolean isCrowbar(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem().equals(RTMItem.crowbar);
    }

    @SideOnly(Side.CLIENT)
    private boolean isInVisibleChunkRange(EntityPlayer player) {
        double renderDistance = this.getVisibleRenderDistanceBlocks();
        double x = this.pos.getX() + 0.5D - player.posX;
        double z = this.pos.getZ() + 0.5D - player.posZ;
        return Math.abs(x) <= renderDistance && Math.abs(z) <= renderDistance;
    }

    @SideOnly(Side.CLIENT)
    private double getVisibleRenderDistanceBlocks() {
        if (ATSAssistCore.proxy.getMinecraft() == null || ATSAssistCore.proxy.getMinecraft().gameSettings == null) {
            return MIN_BEAM_RENDER_DISTANCE_BLOCKS;
        }
        return Math.max(MIN_BEAM_RENDER_DISTANCE_BLOCKS, ATSAssistCore.proxy.getMinecraft().gameSettings.renderDistanceChunks * 16.0D);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        double renderDistance = this.getVisibleRenderDistanceBlocks();
        return renderDistance * renderDistance * 2.0D + 65536.0D;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }
}
