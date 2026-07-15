package jp.kaiz.atsassistmod.block.tileentity;

import jp.kaiz.atsassistmod.ifttt.IFTTTContainer;
import jp.kaiz.atsassistmod.ifttt.IFTTTUtil;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.electric.IProvideElectricity;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TileEntityIFTTT extends TileEntityCustom implements IProvideElectricity {
    //外に出すレッドストーン
    private int redStoneOutput;
    private boolean notFirst, anyMatch;

    private List<IFTTTContainer> thisList = new ArrayList<>();
    private List<IFTTTContainer> thatList = new ArrayList<>();

    @Override
    public final void readFromNBT(NBTTagCompound tag) {
        this.thisList.clear();
        this.thatList.clear();

        super.readFromNBT(tag);
        this.redStoneOutput = tag.getInteger("redStoneOutput");
        this.notFirst = tag.getBoolean("notFirst");
        this.anyMatch = tag.getBoolean("anyMatch");
        if (tag.hasKey("iftttThis")) {
            this.thisList = IFTTTUtil.listFromJson(tag.getString("iftttThis"));
        } else {
            NBTTagList iftttThisList = tag.getTagList("iftttThisList", 10);
            for (int i = 0; i < iftttThisList.tagCount(); i++) {
                NBTTagCompound nbt = iftttThisList.getCompoundTagAt(i);
                IFTTTContainer ifcb = IFTTTUtil.convertClassSafe(nbt.getByteArray("data"));
                if (ifcb != null) {
                    this.thisList.add(ifcb);
                }
            }
        }
        if (tag.hasKey("iftttThat")) {
            this.thatList = IFTTTUtil.listFromJson(tag.getString("iftttThat"));
        } else {
            NBTTagList iftttThatList = tag.getTagList("iftttThatList", 10);
            for (int i = 0; i < iftttThatList.tagCount(); i++) {
                NBTTagCompound nbt = iftttThatList.getCompoundTagAt(i);
                IFTTTContainer ifcb = IFTTTUtil.convertClassSafe(nbt.getByteArray("data"));
                if (ifcb != null) {
                    this.thatList.add(ifcb);
                }
            }
        }
    }

    @Override
    public final NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("redStoneOutput", this.redStoneOutput);
        tag.setBoolean("notFirst", this.notFirst);
        tag.setBoolean("anyMatch", this.anyMatch);
        NBTTagList iftttThisList = new NBTTagList();
        this.thisList
                .stream()
                .map(IFTTTUtil::convertClassSafe)
                .map(Objects::requireNonNull)
                .map(bytes -> {
                    NBTTagCompound nbt = new NBTTagCompound();
                    nbt.setByteArray("data", bytes);
                    return nbt;
                })
                .forEach(iftttThisList::appendTag);
        tag.setTag("iftttThisList", iftttThisList);
        NBTTagList iftttThatList = new NBTTagList();
        this.thatList
                .stream()
                .map(IFTTTUtil::convertClassSafe)
                .map(Objects::requireNonNull)
                .map(bytes -> {
                    NBTTagCompound nbt = new NBTTagCompound();
                    nbt.setByteArray("data", bytes);
                    return nbt;
                })
                .forEach(iftttThatList::appendTag);
        tag.setTag("iftttThatList", iftttThatList);
        return tag;
    }

    private int tick;

    @Override
    public void update() {
        ++this.tick;
        if (this.tick == Integer.MAX_VALUE) {
            this.tick = 0;
        }
        if (this.thisList.isEmpty() || this.thatList.isEmpty()) {
            return;
        }
        if (!this.getWorld().isRemote) {
            AxisAlignedBB detect = new AxisAlignedBB(this.getPos().add(-1, 0, -1), this.getPos().add(2, 4, 2));
            List<?> list = this.getWorld().getEntitiesWithinAABB(EntityTrainBase.class, detect);
            EntityTrainBase train = list.isEmpty() ? null : (EntityTrainBase) list.get(0);
            if ((!this.anyMatch && !this.thisList.isEmpty() && this.thisList.stream().allMatch(iftttContainer -> ((IFTTTContainer.This) iftttContainer).isCondition(this, train)))
                    || (this.anyMatch && this.thisList.stream().anyMatch(iftttContainer -> ((IFTTTContainer.This) iftttContainer).isCondition(this, train)))) {
                this.thatList.forEach(iftttContainer -> ((IFTTTContainer.That) iftttContainer).doThat(this, train, !this.notFirst));
                this.notFirst = true;
            } else if (notFirst) {
                this.thatList.forEach(iftttContainer -> ((IFTTTContainer.That) iftttContainer).finish(this, train));
                this.setRedStoneOutput(0);
                this.notFirst = false;
            }
        }
    }

    public int getTick() {
        return this.tick;
    }

    public int getServerTick() {
        return this.getWorld().isRemote ? this.getTick() : NGTUtil.getServer().getTickCounter();
    }

    public int getRedStoneOutput() {
        return this.redStoneOutput;
    }

    public void setRedStoneOutput(int power) {
        if (this.redStoneOutput != power) {
            this.redStoneOutput = power;
            this.getWorld().updateComparatorOutputLevel(this.getPos(), this.getBlockType());
        }
    }

    public void addIFTTT(IFTTTContainer ifcb) {
        if (ifcb instanceof IFTTTContainer.This) {
            if (this.thisList.size() < 6) {
                this.thisList.add(ifcb);
            }
        } else if (ifcb instanceof IFTTTContainer.That) {
            if (this.thatList.size() < 6) {
                this.thatList.add(ifcb);
            }
        }
    }

    public void setIFTTT(IFTTTContainer ifcb, int ifcbIndex) {
        if (ifcb instanceof IFTTTContainer.This) {
            if (this.thisList.size() > ifcbIndex) {
                this.thisList.set(ifcbIndex, ifcb);
            } else {
                this.addIFTTT(ifcb);
            }
        } else if (ifcb instanceof IFTTTContainer.That) {
            if (this.thatList.size() > ifcbIndex) {
                this.thatList.set(ifcbIndex, ifcb);
            } else {
                this.addIFTTT(ifcb);
            }
        }
    }

    public void removeIFTTT(IFTTTContainer ifcb, int ifcbIndex) {
        if (ifcb instanceof IFTTTContainer.This) {
            this.thisList.remove(ifcbIndex);
        } else if (ifcb instanceof IFTTTContainer.That) {
            this.thatList.remove(ifcbIndex);
        }
    }

    public List<IFTTTContainer> getThisList() {
        return this.thisList;
    }

    public List<IFTTTContainer> getThatList() {
        return this.thatList;
    }

    @Override
    public int getElectricity() {
        return 0;
    }

    @Override
    public void setElectricity(int x, int y, int z, int level) {

    }

    public boolean isAnyMatch() {
        return anyMatch;
    }

    public void setAnyMatch(boolean anyMatch) {
        this.anyMatch = anyMatch;
    }
}
